package dev.dangeroni.offhandfix.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "offhand_fix", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class OffhandFixClient {
    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        ModList.get().getModContainerById("offhand_fix").orElseThrow().registerExtensionPoint(
            ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new OffhandFixConfigScreen(parent)));
    }
}
