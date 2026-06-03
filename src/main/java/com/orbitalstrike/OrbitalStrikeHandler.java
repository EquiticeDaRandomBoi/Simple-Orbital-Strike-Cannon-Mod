package com.orbitalstrike;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class OrbitalStrikeHandler {

	public static void executeStrike(Level level, BlockPos targetPos, Player player, String strikeType) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}

		new Thread(() -> {
			try {
				Thread.sleep(1000);

				serverLevel.getServer().execute(() -> {
					switch (strikeType.toLowerCase()) {
						case "stab":
							spawnStabStrike(serverLevel, targetPos);
							break;
						case "nuke":
							spawnNukeStrike(serverLevel, targetPos);
							break;
					}
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
	}

	private static void spawnStabStrike(ServerLevel level, BlockPos targetPos) {
		int minY = level.getMinY();
		int maxY = level.getMaxY();
		int bedrockY = minY + 1;

		for (int y = maxY; y >= bedrockY; y--) {
			BlockPos tntPos = new BlockPos(targetPos.getX(), y, targetPos.getZ());
			spawnTNT(level, tntPos, 1);
		}
	}

	private static void spawnNukeStrike(ServerLevel level, BlockPos targetPos) {
		int spawnHeight = targetPos.getY() + 60;
		int centerX = targetPos.getX();
		int centerZ = targetPos.getZ();

		PrimedTnt centerTnt = new PrimedTnt(level, centerX + 0.5, spawnHeight, centerZ + 0.5, null);
		centerTnt.setFuse(80);
		level.addFreshEntity(centerTnt);

		// Spawn each ring on a separate tick to avoid a single-tick entity spike.
		// 10 rings spread over 10 ticks (0.5s) is imperceptible before the 4s fuse.
		new Thread(() -> {
			try {
				for (int radius = 3; radius <= 30; radius += 3) {
					final int r = radius;
					level.getServer().execute(() -> spawnRing(level, centerX, centerZ, spawnHeight, r));
					Thread.sleep(50);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
	}

	private static void spawnRing(ServerLevel level, int centerX, int centerZ, int spawnHeight, int radius) {
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

	private static void spawnTNT(ServerLevel level, BlockPos pos, int fuseTicks) {
		PrimedTnt tnt = new PrimedTnt(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, null);
		tnt.setFuse(fuseTicks);
		level.addFreshEntity(tnt);
	}
}
