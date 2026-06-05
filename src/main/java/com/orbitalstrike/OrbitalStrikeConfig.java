package com.orbitalstrike;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "orbitalstrike")
public class OrbitalStrikeConfig implements ConfigData {

	@ConfigEntry.BoundedDiscrete(min = 5, max = 100)
	@ConfigEntry.Gui.Tooltip
	public int nukeRadius = 30;

	@ConfigEntry.BoundedDiscrete(min = 1, max = 10)
	@ConfigEntry.Gui.Tooltip
	public int nukeRingStep = 3;

	@ConfigEntry.BoundedDiscrete(min = 1, max = 200)
	@ConfigEntry.Gui.Tooltip
	public int nukeFuse = 80;

	@ConfigEntry.BoundedDiscrete(min = 0, max = 10000)
	@ConfigEntry.Gui.Tooltip
	public int strikeDelay = 1000;

	@ConfigEntry.BoundedDiscrete(min = 0, max = 4)
	@ConfigEntry.Gui.Tooltip
	public int permissionLevel = 2;

	public static OrbitalStrikeConfig get() {
		return AutoConfig.getConfigHolder(OrbitalStrikeConfig.class).getConfig();
	}
}