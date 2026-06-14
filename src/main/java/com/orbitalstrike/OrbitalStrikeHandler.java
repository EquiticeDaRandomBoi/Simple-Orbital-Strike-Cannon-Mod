package com.orbitalstrike;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;

public class OrbitalStrikeHandler {

	private record PendingStrike(long tick, ServerLevel level, BlockPos pos, String type) {}
	private static final List<PendingStrike> PENDING = new ArrayList<>();

	public static void register() {
		NeoForge.EVENT_BUS.addListener(OrbitalStrikeHandler::onServerTick);
	}

	private static void onServerTick(ServerTickEvent.Post event) {
		long now = event.getServer().getTickCount();
		PENDING.removeIf(s -> {
			if (now >= s.tick()) {
				switch (s.type().toLowerCase()) {
					case "stab" -> spawnStabStrike(s.level(), s.pos());
					case "nuke" -> spawnNukeStrike(s.level(), s.pos());
				}
				return true;
			}
			return false;
		});
	}

	public static void executeStrike(Level level, BlockPos targetPos, String strikeType) {
		if (!(level instanceof ServerLevel serverLevel)) return;
		long fireTick = serverLevel.getServer().getTickCount() + 20L;
		PENDING.add(new PendingStrike(fireTick, serverLevel, targetPos, strikeType));
	}

	private static void spawnStabStrike(ServerLevel level, BlockPos targetPos) {
		int minY = level.getMinY();
		int maxY = level.getMaxY();
		double x = targetPos.getX() + 0.5;
		double z = targetPos.getZ() + 0.5;
		int targetX = targetPos.getX();
		int targetZ = targetPos.getZ();

		// Pass 1: record all solid Y positions before any explosions alter the terrain
		BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
		List<Integer> solidYs = new ArrayList<>();
		for (int y = maxY; y >= minY + 1; y--) {
			mpos.set(targetX, y, targetZ);
			if (!level.isEmptyBlock(mpos)) {
				solidYs.add(y);
			}
		}

		// Pass 2: explode at every cached position
		for (int y : solidYs) {
			level.explode(null, x, y, z, 4.0f, false, Level.ExplosionInteraction.TNT);
		}
	}

	private static void spawnNukeStrike(ServerLevel level, BlockPos targetPos) {
		int spawnHeight = targetPos.getY() + 60;
		int centerX = targetPos.getX();
		int centerZ = targetPos.getZ();

		PrimedTnt centerTnt = new PrimedTnt(level, centerX + 0.5, spawnHeight, centerZ + 0.5, null);
		centerTnt.setFuse(80);
		level.addFreshEntity(centerTnt);

		for (int radius = 3; radius <= 30; radius += 3) {
			int tntPerRing = (int)(radius * 2 * Math.PI);
			double velocityMultiplier = radius * 0.05;
			double fragmentationFactor = radius / 10.0;
			for (int i = 0; i < tntPerRing; i++) {
				double angle = (2 * Math.PI * i) / tntPerRing;
				double randomAngleOffset = (Math.random() - 0.5) * fragmentationFactor * 0.3;
				double randomSpeedOffset = (Math.random() - 0.5) * fragmentationFactor * 0.02;
				double velocityX = Math.cos(angle + randomAngleOffset) * (velocityMultiplier + randomSpeedOffset);
				double velocityZ = Math.sin(angle + randomAngleOffset) * (velocityMultiplier + randomSpeedOffset);
				PrimedTnt tnt = new PrimedTnt(level, centerX + 0.5, spawnHeight, centerZ + 0.5, null);
				tnt.setFuse(80);
				tnt.setDeltaMovement(velocityX, 0, velocityZ);
				level.addFreshEntity(tnt);
			}
		}
	}
}