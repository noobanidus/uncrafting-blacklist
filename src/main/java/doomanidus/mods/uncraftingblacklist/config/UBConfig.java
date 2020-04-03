package doomanidus.mods.uncraftingblacklist.config;

import doomanidus.mods.uncraftingblacklist.UncraftingBlacklist;
import doomanidus.mods.uncraftingblacklist.networking.ClientSyncPacket;
import doomanidus.mods.uncraftingblacklist.networking.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@SuppressWarnings("WeakerAccess")
public class UBConfig {
  public static Configuration config;
  private final static Path configPath = Paths.get("config", UncraftingBlacklist.MODID + ".cfg");

  public final static Set<UUID> synchronisedPlayers = new HashSet<>();
  private static Ingredient inputBlacklist = null;
  private static Ingredient clientInputBlacklist = null;
  private static Ingredient outputsBlacklist = null;
  private static Ingredient clientOutputsBlacklist = null;
  private static List<SyncInfo> syncInfoInput = null;
  private static List<SyncInfo> syncInfoOutputs = null;

  public static String[] itemInputBlacklist = null;
  public static String[] itemOutputBlacklist = null;

  public static void load() {
    config = new Configuration(configPath.toFile());
    itemInputBlacklist = config.getStringList("itemInputBlacklist", "general", new String[]{}, "List of item inputs for recipes which should be blacklisted from being uncrafted. Format: modname:itemname:metadata. Non-present metadata will be considered 0. Use * to specify any damage. Use: ore:oreDictionaryName to specify an ore dictionary output (i.e., planksWood)");
    itemOutputBlacklist = config.getStringList("itemOutputBlacklist", "general", new String[]{}, "List of item ingredients that indicate a recipe with those in its result should be blacklisted from uncrafting. Format: modname:itemname:metadata. Non-present metadata will be considered 0. Use * to specify any damage. Use: ore:oreDictionaryName to specify an ore dictionary ingredient.");
    if (config.hasChanged()) {
      config.save();
    }
    syncInfoInput = new ArrayList<>();
    syncInfoOutputs = new ArrayList<>();
    inputBlacklist = parseIngredients(itemInputBlacklist, syncInfoInput);
    outputsBlacklist = parseIngredients(itemOutputBlacklist, syncInfoOutputs);
  }

  public static Ingredient getInputBlacklist() {
    if (clientInputBlacklist != null) {
      return clientInputBlacklist;
    }
    if (itemInputBlacklist == null) {
      load();
    }
    if (inputBlacklist == null) {
      inputBlacklist = parseIngredients(itemInputBlacklist, syncInfoInput = new ArrayList<>());
    }
    return inputBlacklist;
  }

  public static Ingredient getOutputsBlacklist() {
    if (clientOutputsBlacklist != null) {
      return clientOutputsBlacklist;
    }
    if (itemOutputBlacklist == null) {
      load();
    }
    if (outputsBlacklist == null) {
      outputsBlacklist = parseIngredients(itemOutputBlacklist, syncInfoOutputs = new ArrayList<>());
    }

    return outputsBlacklist;
  }

  private static Ingredient parseIngredients(String[] inputs, List<SyncInfo> syncInfo) {
    List<ItemStack> result = new ArrayList<>();
    for (String s : inputs) {
      String[] split = s.split(":");
      String modname = "";
      String itemname = "";
      int meta = 0;
      boolean ore = false;
      if (split.length == 1) {
        UncraftingBlacklist.LOG.error("Invalid value for configuration string: " + s);
        continue;
      }
      if (split.length >= 2) {
        modname = split[0];
        itemname = split[1];
      }
      if (split.length == 3) {
        String metaString = split[2];
        if (metaString.trim().equals("*")) {
          meta = OreDictionary.WILDCARD_VALUE;
        } else {
          try {
            meta = Integer.valueOf(metaString);
          } catch (NumberFormatException e) {
            UncraftingBlacklist.LOG.error("Invalid meta value for configuration string: " + s);
            continue;
          }
        }
      }
      if (modname.toLowerCase().equals("ore")) {
        ore = true;
      }
      if (!ore) {
        ResourceLocation rl = new ResourceLocation(modname, itemname);
        Item item = ForgeRegistries.ITEMS.getValue(rl);
        if (item == null) {
          UncraftingBlacklist.LOG.error("Unable to find item with registry name '" + rl.toString() + "', invalid value for configuration string: " + s);
          continue;
        }
        syncInfo.add(new SyncInfo(item, meta));
        result.add(new ItemStack(item, 1, meta));
      } else {
        syncInfo.add(new SyncInfo(itemname));
        result.addAll(Arrays.asList(new OreIngredient(itemname).getMatchingStacks()));
      }
    }
    return Ingredient.fromStacks(result.toArray(new ItemStack[0]));
  }

  public static void synchroniseToPlayer(EntityPlayerMP player) {
    if (synchronisedPlayers.contains(player.getUniqueID())) {
      return;
    }

    load();

    ClientSyncPacket inputs = new ClientSyncPacket(syncInfoInput, true);
    PacketHandler.sendTo(inputs, player);
    inputs = new ClientSyncPacket(syncInfoOutputs, false);
    PacketHandler.sendTo(inputs, player);
    synchronisedPlayers.add(player.getUniqueID());
  }

  public static void unsynchronisePlayer(List<SyncInfo> info, boolean inputs) {
    List<ItemStack> result = new ArrayList<>();
    for (SyncInfo i : info) {
      if (i.isOre()) {
        result.addAll(i.getOre());
      } else {
        result.add(i.getItem());
      }
    }
    if (inputs) {
      clientInputBlacklist = Ingredient.fromStacks(result.toArray(new ItemStack[0]));
    } else {
      clientOutputsBlacklist = Ingredient.fromStacks(result.toArray(new ItemStack[0]));
    }
  }

  public static void clearPlayer(EntityPlayer player) {
    synchronisedPlayers.remove(player.getUniqueID());
  }

  public static void clear() {
    Blacklist.clear();
    syncInfoOutputs = null;
    syncInfoInput = null;
    inputBlacklist = null;
    outputsBlacklist = null;
    synchronisedPlayers.clear();
  }

  public static void clearClient () {
    Blacklist.clear();
    clientInputBlacklist = null;
    clientOutputsBlacklist = null;
  }

  public static List<SyncInfo> getSyncInfoInput() {
    if (syncInfoInput == null) {
      load();
    }

    return syncInfoInput;
  }

  public static List<SyncInfo> getSyncInfoOutputs() {
    if (syncInfoOutputs == null) {
      load();
    }

    return syncInfoOutputs;
  }
}
