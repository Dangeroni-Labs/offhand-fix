package dev.dangeroni.offhandfix.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = "offhand_fix", dist = Dist.CLIENT)
public final class OffhandFixClient {
    public OffhandFixClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
            (mod, parent) -> new OffhandFixConfigScreen(parent));
    }
}
