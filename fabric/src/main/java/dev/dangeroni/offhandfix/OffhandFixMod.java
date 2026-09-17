package dev.dangeroni.offhandfix;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class OffhandFixMod implements ModInitializer {
	public static final String MOD_ID = "offhand_fix";

	@Override
	public void onInitialize() {
		OffhandFixConfig.load(FabricLoader.getInstance().getConfigDir());
	}
}
