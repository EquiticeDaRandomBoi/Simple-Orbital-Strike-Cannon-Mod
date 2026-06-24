package com.orbitalstrike.mixin;

import com.orbitalstrike.OrbitalStrikeConfig;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.CraftingResultSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingResultSlot.class)
public abstract class CraftingResultSlotMixin {

	@Shadow @Final private RecipeInputInventory input;

	@Inject(method = "onTakeItem", at = @At("HEAD"))
	private void orbitalstrike$consumeExtraTnt(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
		NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
		if (data == null) return;
		if (!data.copyNbt().contains("OrbitalStrikeType")) return;
		int required = OrbitalStrikeConfig.get().craftingTntPerSlot;
		int extra = required - 1;
		if (extra <= 0) return;
		for (int i = 0; i < this.input.size(); i++) {
			ItemStack s = this.input.getStack(i);
			if (s.isOf(Items.TNT) && s.getCount() >= extra) {
				this.input.removeStack(i, extra);
			}
		}
	}
}
