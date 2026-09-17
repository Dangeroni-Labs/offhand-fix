package dev.dangeroni.offhandfix;

import com.mojang.authlib.GameProfile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.stats.Stats;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ScopeValidation {
    private static int checks;

    public static void run(MinecraftServer server) {
        try {
            Path config = Path.of("validation-config");
            Files.createDirectories(config);
            for (String invalid : new String[] {"", "shiftClickScope=INVALID", "shiftClickScope=\\uZZZZ"}) {
                Files.writeString(config.resolve("offhand_fix.properties"), invalid);
                OffhandFixConfig.load(config);
                check(OffhandFixConfig.shiftClickScope() == ShiftClickScope.PLAYER_INVENTORY_ONLY, "invalid config fallback");
            }
            Files.writeString(config.resolve("offhand_fix.properties"), "shiftClickScope=PLAYER_INVENTORY_ONLY");
            OffhandFixConfig.load(config);
            for (ShiftClickScope scope : ShiftClickScope.values()) {
                if (!OffhandFixConfig.setShiftClickScope(scope)) throw new AssertionError("Could not save scope");
                boolean enabled = scope != ShiftClickScope.DISABLED;
                boolean external = scope == ShiftClickScope.ALL_CONTAINERS;
                ServerPlayer player = player(server);
                inventory(player, 10, Items.BREAD, 32, enabled ? 42 : 10);
                inventory(player, 60, Items.BREAD, 32, enabled ? 64 : 60);
                inventory(player, 64, Items.BREAD, 32, 64);
                inventory(player, 10, Items.APPLE, 32, 10);
                chest(player, true, 10, 32, 10);
                chest(player, false, 10, 32, external ? 42 : 10);
                chest(player, false, 60, 32, external ? 64 : 60);
                furnace(player, true, external);
                furnace(player, false, external);
                craftingOutgoing(player);
                crafting(player, 10, 1, external ? 17 : 10, Items.WHEAT, Items.BREAD, 7);
                crafting(player, 60, 1, external ? 64 : 60, Items.WHEAT, Items.BREAD, 7);
                crafting(player, 10, 1, external ? 14 : 10, Items.OAK_LOG, Items.OAK_PLANKS, 4);
                crafting(player, 62, 1, external ? 64 : 62, Items.OAK_LOG, Items.OAK_PLANKS, 4);
                crafting(player, 10, 5, external ? 30 : 10, Items.OAK_LOG, Items.OAK_PLANKS, 4);
                crafting(player, 10, 1, external ? 13 : 10, Items.HONEY_BOTTLE, Items.SUGAR, 3);
                hoveredF(player);
                gameplayF(player);
                System.out.println("SCOPE VALIDATION: " + scope + " passed");
            }
            System.out.println("SCOPE VALIDATION PASS: " + checks + " assertions");
            Files.writeString(Path.of("validation-passed.txt"), "SCOPE VALIDATION PASS: " + checks + " assertions\n");
        } catch (Exception exception) {
            throw new AssertionError("Scope runtime validation failed", exception);
        }
    }

    private static ServerPlayer player(MinecraftServer server) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "ScopeTest");
        ServerPlayer player = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
        new ServerGamePacketListenerImpl(server, new Connection(PacketFlow.SERVERBOUND), player, CommonListenerCookie.createInitial(profile, false));
        player.connection.handleAcceptPlayerLoad(new ServerboundPlayerLoadedPacket());
        return player;
    }

    private static void reset(ServerPlayer player, Item item, int offhand) {
        player.getInventory().clearContent();
        player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(item, offhand));
    }

    private static void click(ServerPlayer player, AbstractContainerMenu menu, int slot) {
        player.containerMenu = menu;
        menu.clicked(slot, 0, ContainerInput.QUICK_MOVE, player);
    }

    private static int inventoryCount(ServerPlayer player, Item item) {
        int total = 0;
        for (int slot = 0; slot < Inventory.SLOT_OFFHAND; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(item)) total += stack.getCount();
        }
        return total;
    }

    private static void inventory(ServerPlayer player, int offhand, Item source, int count, int expected) {
        reset(player, Items.BREAD, offhand);
        player.getInventory().setItem(9, new ItemStack(source, count));
        click(player, player.inventoryMenu, 9);
        check(player.getOffhandItem().getCount() == expected, "standalone refill/fallback");
        check(inventoryCount(player, source) == count - (source == Items.BREAD ? expected - offhand : 0), "standalone remainder");
    }

    private static void chest(ServerPlayer player, boolean outgoing, int offhand, int count, int expected) {
        reset(player, Items.BREAD, offhand);
        SimpleContainer chest = new SimpleContainer(27);
        ChestMenu menu = ChestMenu.threeRows(1, player.getInventory(), chest);
        if (outgoing) player.getInventory().setItem(9, new ItemStack(Items.BREAD, count));
        else chest.setItem(0, new ItemStack(Items.BREAD, count));
        click(player, menu, outgoing ? 27 : 0);
        check(player.getOffhandItem().getCount() == expected, "chest direction/scope");
        int containerCount = 0;
        for (int index = 0; index < 27; index++) containerCount += chest.getItem(index).getCount();
        check(containerCount + inventoryCount(player, Items.BREAD) + expected == count + offhand, "chest conservation");
        check(outgoing ? containerCount == count : containerCount == 0, "chest vanilla destination");
    }

    private static void furnace(ServerPlayer player, boolean outgoing, boolean external) {
        reset(player, Items.IRON_INGOT, 10);
        SimpleContainer furnace = new SimpleContainer(3);
        FurnaceMenu menu = new FurnaceMenu(2, player.getInventory(), furnace, new SimpleContainerData(4));
        if (outgoing) player.getInventory().setItem(9, new ItemStack(Items.RAW_IRON, 32));
        else furnace.setItem(2, new ItemStack(Items.IRON_INGOT, 32));
        click(player, menu, outgoing ? 3 : 2);
        check(player.getOffhandItem().getCount() == (!outgoing && external ? 42 : 10), "furnace scope");
        check(outgoing ? furnace.getItem(0).getCount() == 32 : furnace.getItem(2).isEmpty(), "furnace vanilla transfer");
    }

    private static void crafting(ServerPlayer player, int offhand, int inputs, int expected, Item input, Item output, int resultCount) {
        reset(player, output, offhand);
        CraftingMenu menu = new CraftingMenu(3, player.getInventory(), ContainerLevelAccess.create(player.level(), BlockPos.ZERO));
        player.containerMenu = menu;
        menu.slots.get(1).set(new ItemStack(input, inputs));
        check(menu.slots.get(0).getItem().getCount() == resultCount, "multi-item recipe result");
        int craftedBefore = player.getStats().getValue(Stats.ITEM_CRAFTED.get(output));
        click(player, menu, 0);
        check(player.getOffhandItem().getCount() == expected, "crafting refill scope/capacity");
        check(menu.slots.get(1).getItem().isEmpty() || input == Items.HONEY_BOTTLE && menu.slots.get(1).getItem().is(Items.GLASS_BOTTLE), "ingredients consumed");
        check(menu.slots.get(0).getItem().isEmpty(), "no free crafting result");
        check(inventoryCount(player, output) + expected == offhand + inputs * resultCount, "crafting conservation");
        check(player.getStats().getValue(Stats.ITEM_CRAFTED.get(output)) - craftedBefore == inputs * resultCount, "vanilla crafting statistics hooks");
        if (input == Items.HONEY_BOTTLE) {
            check(menu.slots.get(1).getItem().is(Items.GLASS_BOTTLE) || inventoryCount(player, Items.GLASS_BOTTLE) == 1, "crafting remainder");
        }
        click(player, menu, 0);
        check(inventoryCount(player, output) + player.getOffhandItem().getCount() == offhand + inputs * resultCount, "repeat cannot duplicate");
    }

    private static void hoveredF(ServerPlayer player) {
        reset(player, Items.BREAD, 10);
        SimpleContainer chest = new SimpleContainer(27);
        chest.setItem(0, new ItemStack(Items.BREAD, 32));
        ChestMenu menu = ChestMenu.threeRows(4, player.getInventory(), chest);
        player.containerMenu = menu;
        menu.clicked(0, Inventory.SLOT_OFFHAND, ContainerInput.SWAP, player);
        check(player.getOffhandItem().getCount() == 42 && chest.getItem(0).isEmpty(), "F unaffected by scope");
        chest.setItem(0, new ItemStack(Items.APPLE, 5));
        menu.clicked(0, Inventory.SLOT_OFFHAND, ContainerInput.SWAP, player);
        check(player.getOffhandItem().is(Items.APPLE) && chest.getItem(0).is(Items.BREAD), "hovered F vanilla fallback");
    }

    private static void craftingOutgoing(ServerPlayer player) {
        reset(player, Items.BREAD, 10);
        CraftingMenu menu = new CraftingMenu(5, player.getInventory(), ContainerLevelAccess.create(player.level(), BlockPos.ZERO));
        player.getInventory().setItem(9, new ItemStack(Items.BREAD, 32));
        click(player, menu, 10);
        check(player.getOffhandItem().getCount() == 10, "crafting player slots remain vanilla");
        int total = inventoryCount(player, Items.BREAD);
        for (int index = 1; index < 10; index++) total += menu.slots.get(index).getItem().getCount();
        check(total == 32, "crafting outgoing conservation");
    }

    private static void gameplayF(ServerPlayer player) {
        reset(player, Items.BREAD, 10);
        player.containerMenu = player.inventoryMenu;
        player.getInventory().setSelectedItem(new ItemStack(Items.BREAD, 32));
        ServerboundPlayerActionPacket packet = new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN);
        player.connection.handlePlayerAction(packet);
        check(player.getOffhandItem().getCount() == 42 && player.getMainHandItem().isEmpty(), "gameplay F refill unaffected");
        player.getInventory().setSelectedItem(new ItemStack(Items.APPLE, 5));
        player.connection.handlePlayerAction(packet);
        check(player.getOffhandItem().is(Items.APPLE) && player.getMainHandItem().is(Items.BREAD), "gameplay F vanilla fallback");
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
}
