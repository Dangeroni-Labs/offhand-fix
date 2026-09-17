package dev.dangeroni.offhandfix;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(KossmansOffhandFix.MOD_ID)
public final class KossmansOffhandFix {
    public static final String MOD_ID = "offhand_fix";
    public static final Logger LOGGER = LogUtils.getLogger();

    public KossmansOffhandFix(IEventBus modEventBus) {
    }
}
