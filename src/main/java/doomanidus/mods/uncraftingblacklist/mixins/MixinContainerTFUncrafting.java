package doomanidus.mods.uncraftingblacklist.mixins;

import doomanidus.mods.uncraftingblacklist.config.Blacklist;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import twilightforest.inventory.ContainerTFUncrafting;

import java.util.ArrayList;
import java.util.List;

@Mixin(ContainerTFUncrafting.class)
@SuppressWarnings("unused")
public class MixinContainerTFUncrafting {
  @Inject(method = "getRecipesFor(Lnet/minecraft/item/ItemStack;)[Lnet/minecraft/item/crafting/IRecipe;", at = @At(value = "RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT, remap = false)
  private static void getRecipesItemStack(ItemStack stack, CallbackInfoReturnable<IRecipe[]> callbackInfo, List<IRecipe> recipes) {
    List<IRecipe> result = new ArrayList<>();
    for (IRecipe recipe : recipes) {
      if (!Blacklist.isBlacklisted(recipe, stack)) {
        result.add(recipe);
      }
    }
    callbackInfo.setReturnValue(result.toArray(new IRecipe[0]));
  }

  @Inject(method = "getRecipesFor(Lnet/minecraft/inventory/InventoryCrafting;Lnet/minecraft/world/World;)[Lnet/minecraft/item/crafting/IRecipe;", at = @At(value = "RETURN"), cancellable = true, remap = false, locals = LocalCapture.CAPTURE_FAILSOFT)
  private static void getRecipesInventory(InventoryCrafting inventory, World world, CallbackInfoReturnable<IRecipe[]> callbackInfo, List<IRecipe> recipes) {
    List<IRecipe> result = new ArrayList<>();
    for (IRecipe recipe : recipes) {
      if (!Blacklist.isBlacklisted(recipe, inventory, world)) {
        result.add(recipe);
      }
    }
    callbackInfo.setReturnValue(result.toArray(new IRecipe[0]));
  }
}
