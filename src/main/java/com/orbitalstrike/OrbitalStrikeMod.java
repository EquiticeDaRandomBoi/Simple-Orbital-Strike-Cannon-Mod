package com.orbitalstrike;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrbitalStrikeMod implements ModInitializer {
	public static final String MOD_ID = "orbitalstrike";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Orbital Strike Mod!");

		UseItemCallback.EVENT.register((player, level, hand) -> {
			ItemStack stack = player.getItemInHand(hand);

			if (stack.getItem() == Items.FISHING_ROD && stack.getDamageValue() == stack.getMaxDamage() - 1) {
				CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
				if (customData != null) {
					CompoundTag nbt = customData.copyTag();
					if (nbt.contains("OrbitalStrikeType")) {
						String strikeType = nbt.getString("OrbitalStrikeType").orElse("");

						if (!level.isClientSide()) {
							HitResult hitResult = player.pick(200.0D, 0.0F, false);
							BlockPos targetPos;

							if (hitResult.getType() == HitResult.Type.BLOCK) {
								targetPos = ((BlockHitResult) hitResult).getBlockPos();
							} else {
								targetPos = BlockPos.containing(hitResult.getLocation());
							}

							OrbitalStrikeHandler.executeStrike(level, targetPos, player, strikeType);
						}

						EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
						stack.hurtAndBreak(1, player, slot);

						return InteractionResult.SUCCESS;
					}
				}
			}

			return InteractionResult.PASS;
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) ->
			OrbitalStrikeCommand.register(dispatcher)
		);
	}
}
