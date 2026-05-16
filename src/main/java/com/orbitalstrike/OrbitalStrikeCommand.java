package com.orbitalstrike;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class OrbitalStrikeCommand {

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("orbitalstrike")
			.requires(source -> source.hasPermissionLevel(2))
			.then(CommandManager.argument("type", StringArgumentType.string())
				.suggests((context, builder) -> {
					builder.suggest("stab");
					builder.suggest("nuke");
					return builder.buildFuture();
				})
				.executes(OrbitalStrikeCommand::giveOrbitalStrikeRod)
				.then(CommandManager.argument("count", IntegerArgumentType.integer(1, 64))
					.executes(OrbitalStrikeCommand::giveOrbitalStrikeRodWithCount)
				)
			)
		);
	}

	private static int giveOrbitalStrikeRod(CommandContext<ServerCommandSource> context) {
		return giveRods(context, 1);
	}

	private static int giveOrbitalStrikeRodWithCount(CommandContext<ServerCommandSource> context) {
		int count = IntegerArgumentType.getInteger(context, "count");
		return giveRods(context, count);
	}

	private static int giveRods(CommandContext<ServerCommandSource> context, int count) {
		String strikeType = StringArgumentType.getString(context, "type");

		if (!strikeType.equalsIgnoreCase("stab") && !strikeType.equalsIgnoreCase("nuke")) {
			context.getSource().sendError(Text.literal("Invalid type! Use: stab or nuke"));
			return 0;
		}

		ServerPlayerEntity player = context.getSource().getPlayer();
		if (player == null) {
			context.getSource().sendError(Text.literal("This command must be run by a player!"));
			return 0;
		}

		for (int i = 0; i < count; i++) {
			ItemStack fishingRod = new ItemStack(Items.FISHING_ROD);
			fishingRod.setDamage(fishingRod.getMaxDamage() - 1);

			NbtCompound nbt = new NbtCompound();
			nbt.putString("OrbitalStrikeType", strikeType.toLowerCase());
			fishingRod.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

			String displayName = strikeType.equalsIgnoreCase("stab") ? "stab shot" : "nuke shot";
			fishingRod.set(DataComponentTypes.CUSTOM_NAME, Text.literal(displayName));

			player.giveItemStack(fishingRod);
		}

		String typeName = strikeType.equalsIgnoreCase("stab") ? "Stab Shot" : "Nuke Shot";
		context.getSource().sendFeedback(() ->
			Text.literal("§aGave " + count + "x " + typeName + " to " + player.getName().getString()),
			true
		);

		return 1;
	}
}
