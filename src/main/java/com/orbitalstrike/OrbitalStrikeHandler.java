package com.orbitalstrike;

import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;

public class OrbitalStrikeHandler {

	public static void executeStrike(World world, BlockPos targetPos, PlayerEntity player, String strikeType) {
		if (!(world instanceof ServerWorld serverWorld)) {
			return;
		}

		new Thread(() -> {
			try {
				Thread.sleep(OrbitalStrikeConfig.get().strikeDelay);

				serverWorld.getServer().execute(() -> {
					switch (strikeType.toLowerCase()) {
						case "stab":
							spawnStabStrike(serverWorld, targetPos);
							break;
						case "nuke":
							spawnNukeStrike(serverWorld, targetPos);
							break;
					}
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
	}

	private static void spawnStabStrike(ServerWorld world, BlockPos targetPos) {
		int minY = world.getDimension().minY();
		int maxY = minY + world.getDimension().height() - 1;
		int bedrockY = minY + 1;

		for (int y = maxY; y >= bedrockY; y--) {
			BlockPos tntPos = new BlockPos(targetPos.getX(), y, targetPos.getZ());
			spawnTNT(world, tntPos, 1);
		}
	}

	private static void spawnNukeStrike(ServerWorld world, BlockPos targetPos) {
		int spawnHeight = targetPos.getY() + 60;
		int centerX = targetPos.getX();
		int centerZ = targetPos.getZ();

		OrbitalStrikeConfig cfg = OrbitalStrikeConfig.get();

		TntEntity centerTnt = new TntEntity(world, centerX + 0.5, spawnHeight, centerZ + 0.5, null);
		centerTnt.setFuse(cfg.nukeFuse);
		world.spawnEntity(centerTnt);

		for (int radius = 3; radius <= cfg.nukeRadius; radius += cfg.nukeRingStep) {
			spawnRing(world, centerX, centerZ, spawnHeight, radius, cfg.nukeFuse);
		}
	}

	private static void spawnRing(ServerWorld world, int centerX, int centerZ, int spawnHeight, int radius, int fuse) {
		int tntPerRing = (int)(radius * 2 * Math.PI);
		double velocityMultiplier = radius * 0.05;
		double fragmentationFactor = radius / 10.0;

		for (int i = 0; i < tntPerRing; i++) {
			double angle = (2 * Math.PI * i) / tntPerRing;

			double randomAngleOffset = (Math.random() - 0.5) * fragmentationFactor * 0.3;
			double randomSpeedOffset = (Math.random() - 0.5) * fragmentationFactor * 0.02;

			double velocityX = Math.cos(angle + randomAngleOffset) * (velocityMultiplier + randomSpeedOffset);
			double velocityZ = Math.sin(angle + randomAngleOffset) * (velocityMultiplier + randomSpeedOffset);

			TntEntity tnt = new TntEntity(world, centerX + 0.5, spawnHeight, centerZ + 0.5, null);
			tnt.setFuse(fuse);
			tnt.setVelocity(velocityX, 0, velocityZ);
			world.spawnEntity(tnt);
		}
	}

	private static void spawnTNT(ServerWorld world, BlockPos pos, int fuseTicks) {
		TntEntity tnt = new TntEntity(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, null);
		tnt.setFuse(fuseTicks);
		world.spawnEntity(tnt);
	}
}