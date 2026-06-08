package com.orbitalstrike;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrbitalStrikeMod implements ModInitializer {
	public static final String MOD_ID = "orbitalstrike";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		AutoConfig.register(OrbitalStrikeConfig.class, GsonConfigSerializer::new);
		OrbitalStrikeHandler.registerTickEvent();
		LOGGER.info("Initializing Orbital Strike Mod!");

		UseItemCallback.EVENT.register((player, world, hand) -> {
			ItemStack stack = player.getStackInHand(hand);

			if (stack.getItem() == Items.FISHING_ROD && stack.getDamage() == stack.getMaxDamage() - 1) {
				NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
				if (customData != null) {
					NbtCompound nbt = customData.copyNbt();
					if (nbt.contains("OrbitalStrikeType")) {
						String strikeType = nbt.getString("OrbitalStrikeType").orElse("");

						if (!world.isClient()) {
							HitResult hitResult = player.raycast(200.0D, 0.0F, false);
							BlockPos targetPos;

							if (hitResult.getType() == HitResult.Type.BLOCK) {
								targetPos = ((BlockHitResult) hitResult).getBlockPos();
							} else {
								targetPos = BlockPos.ofFloored(hitResult.getPos());
							}

							OrbitalStrikeHandler.executeStrike(world, targetPos, strikeType);

							player.swingHand(hand);
							if (player.isCreative()) {
								player.setStackInHand(hand, ItemStack.EMPTY);
							} else {
								EquipmentSlot slot = hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
								stack.damage(1, player, slot);
							}
						}

						return ActionResult.SUCCESS;
					}
				}
			}

			return ActionResult.PASS;
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
			OrbitalStrikeCommand.register(dispatcher)
		);
	}
}
