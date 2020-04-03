package doomanidus.mods.uncraftingblacklist.config;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("Duplicates")
public class Blacklist {
  private static final Set<IRecipe> blacklistedRecipes = new HashSet<>();
  private static final Set<IRecipe> allowedRecipes = new HashSet<>();

  public static void clear () {
    blacklistedRecipes.clear();
    allowedRecipes.clear();
  }

  private static boolean calculateBlacklisted(IRecipe recipe, ItemStack stack) {
    final Ingredient ingredientBlacklist = UBConfig.getInputBlacklist();
    if (ingredientBlacklist.apply(stack)) {
      return true;
    }

    final Ingredient outputBlacklist = UBConfig.getOutputsBlacklist();
    for (Ingredient input : recipe.getIngredients()) {
      for (ItemStack matching : input.getMatchingStacks()) {
        if (outputBlacklist.apply(matching)) {
          return true;
        }
      }
    }

    return false;
  }

  private static boolean calculateBlacklisted(IRecipe recipe, InventoryCrafting inventory, World world) {
    final Ingredient ingredientBlacklist = UBConfig.getInputBlacklist();
    if (ingredientBlacklist.apply(recipe.getRecipeOutput())) {
      return true;
    }

    final Ingredient outputBlacklist = UBConfig.getOutputsBlacklist();
    for (int i = 0; i < inventory.getSizeInventory(); i++) {
      ItemStack inSlot = inventory.getStackInSlot(i);
      if (outputBlacklist.apply(inSlot)) {
        return true;
      }
    }

    for (Ingredient input : recipe.getIngredients()) {
      for (ItemStack matching : input.getMatchingStacks()) {
        if (outputBlacklist.apply(matching)) {
          return true;
        }
      }
    }

    return false;
  }

  public static boolean isBlacklisted(IRecipe recipe, ItemStack stack) {
    if (blacklistedRecipes.contains(recipe)) {
      return true;
    }

    if (allowedRecipes.contains(recipe)) {
      return false;
    }

    if (calculateBlacklisted(recipe, stack)) {
      blacklistedRecipes.add(recipe);
      return true;
    } else {
      allowedRecipes.add(recipe);
      return false;
    }
  }

  public static boolean isBlacklisted(IRecipe recipe, InventoryCrafting inventory, World world) {
    if (blacklistedRecipes.contains(recipe)) {
      return true;
    }

    if (allowedRecipes.contains(recipe)) {
      return false;
    }

    if (calculateBlacklisted(recipe, inventory, world)) {
      blacklistedRecipes.add(recipe);
      return true;
    } else {
      allowedRecipes.add(recipe);
      return false;
    }
  }
}
