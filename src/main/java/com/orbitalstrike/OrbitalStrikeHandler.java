package com.orbitalstrike;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class OrbitalStrikeHandler {

	private record PendingStrike(long tick, ServerWorld world, BlockPos pos, String type) {}
	private static final List<PendingStrike> PENDING = new ArrayList<>();

	public static void registerTickEvent() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			long now = server.getTicks();
			PENDING.removeIf(s -> {
				if (now >= s.tick()) {
					switch (s.type().toLowerCase()) {
						case "stab" -> spawnStabStrike(s.world(), s.pos());
						case "nuke" -> spawnNukeStrike(s.world(), s.pos());
					}
					return true;
				}
				return false;
			});
		});
	}

	public static void executeStrike(World world, BlockPos targetPos, String strikeType) {
		if (!(world instanceof ServerWorld serverWorld)) return;
		long delayTicks = Math.max(1, OrbitalStrikeConfig.get().strikeDelay / 50L);
		long fireTick = serverWorld.getServer().getTicks() + delayTicks;
		PENDING.add(new PendingStrike(fireTick, serverWorld, targetPos, strikeType));
	}

	private static void spawnStabStrike(ServerWorld world, BlockPos targetPos) {
		int minY = world.getDimension().minY();
		int maxY = minY + world.getDimension().height() - 1;
		double x = targetPos.getX() + 0.5;
		double z = targetPos.getZ() + 0.5;
		int targetX = targetPos.getX();
		int targetZ = targetPos.getZ();
		for (int y = maxY; y >= minY; y--) {
			BlockPos checkPos = new BlockPos(targetX, y, targetZ);
			if (!world.isAir(checkPos) && world.getFluidState(checkPos).isEmpty()) {
				world.removeBlock(checkPos, false);
			}
		}
		world.createExplosion(null, x, targetPos.getY(), z, 4.0f, false, World.ExplosionSourceType.TNT);
	}

	private static void spawnNukeStrike(ServerWorld world, BlockPos targetPos) {
		OrbitalStrikeConfig cfg = OrbitalStrikeConfig.get();
		int spawnHeight = targetPos.getY() + 60;
		int centerX = targetPos.getX();
		int centerZ = targetPos.getZ();

		TntEntity centerTnt = new TntEntity(world, centerX + 0.5, spawnHeight, centerZ + 0.5, null);
		centerTnt.setFuse(cfg.nukeFuse);
		world.spawnEntity(centerTnt);

		for (int radius = cfg.nukeRingStep; radius <= cfg.nukeRadius; radius += cfg.nukeRingStep) {
			int tntPerRing = (int) (radius * 2 * Math.PI);
			double velocityMultiplier = radius * 0.05;
			double fragmentationFactor = radius / 10.0;

			for (int i = 0; i < tntPerRing; i++) {
				double angle = (2 * Math.PI * i) / tntPerRing;
				double randomAngleOffset = (Math.random() - 0.5) * fragmentationFactor * 0.3;
				double randomSpeedOffset = (Math.random() - 0.5) * fragmentationFactor * 0.02;
				double velocityX = Math.cos(angle + randomAngleOffset) * (velocityMultiplier + randomSpeedOffset);
				double velocityZ = Math.sin(angle + randomAngleOffset) * (velocityMultiplier + randomSpeedOffset);

				TntEntity tnt = new TntEntity(world, centerX + 0.5, spawnHeight, centerZ + 0.5, null);
				tnt.setFuse(cfg.nukeFuse);
				tnt.setVelocity(velocityX, 0, velocityZ);
				world.spawnEntity(tnt);
			}
		}
	}
}