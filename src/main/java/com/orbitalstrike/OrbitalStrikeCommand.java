package com.orbitalstrike;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

public class OrbitalStrikeCommand {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("orbitalstrike")
			.requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS)))
			.then(Commands.argument("type", StringArgumentType.string())
				.suggests((context, builder) -> {
					builder.suggest("stab");
					builder.suggest("nuke");
					return builder.buildFuture();
				})
				.executes(OrbitalStrikeCommand::giveOrbitalStrikeRod)
				.then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
					.executes(OrbitalStrikeCommand::giveOrbitalStrikeRodWithCount)
				)
			)
		);
	}

	private static int giveOrbitalStrikeRod(CommandContext<CommandSourceStack> context) {
		return giveRods(context, 1);
	}

	private static int giveOrbitalStrikeRodWithCount(CommandContext<CommandSourceStack> context) {
		int count = IntegerArgumentType.getInteger(context, "count");
		return giveRods(context, count);
	}

	private static int giveRods(CommandContext<CommandSourceStack> context, int count) {
		String strikeType = StringArgumentType.getString(context, "type");

		if (!strikeType.equalsIgnoreCase("stab") && !strikeType.equalsIgnoreCase("nuke")) {
			context.getSource().sendFailure(Component.literal("Invalid type! Use: stab or nuke"));
			return 0;
		}

		ServerPlayer player = context.getSource().getPlayer();
		if (player == null) {
			context.getSource().sendFailure(Component.literal("This command must be run by a player!"));
			return 0;
		}

		for (int i = 0; i < count; i++) {
			ItemStack fishingRod = new ItemStack(Items.FISHING_ROD);
			fishingRod.setDamageValue(fishingRod.getMaxDamage() - 1);

			CompoundTag nbt = new CompoundTag();
			nbt.putString("OrbitalStrikeType", strikeType.toLowerCase());
			fishingRod.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));

			String displayName = strikeType.equalsIgnoreCase("stab") ? "stab shot" : "nuke shot";
			fishingRod.set(DataComponents.CUSTOM_NAME, Component.literal(displayName));

			player.addItem(fishingRod);
		}

		String typeName = strikeType.equalsIgnoreCase("stab") ? "Stab Shot" : "Nuke Shot";
		context.getSource().sendSuccess(() ->
			Component.literal("§aGave " + count + "x " + typeName + " to " + player.getName().getString()),
			true
		);

		return 1;
	}
}
