package dev.dangeroni.offhandfix;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = KossmansOffhandFix.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = KossmansOffhandFix.MODID, value = Dist.CLIENT)
public class KossmansOffhandFixClient {
    public KossmansOffhandFixClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        KossmansOffhandFix.LOGGER.info("HELLO FROM CLIENT SETUP");
        KossmansOffhandFix.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }
}
