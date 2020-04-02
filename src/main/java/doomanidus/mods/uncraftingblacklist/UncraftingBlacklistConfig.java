package doomanidus.mods.uncraftingblacklist;

import doomanidus.mods.uncraftingblacklist.networking.ClientSyncPacket;
import doomanidus.mods.uncraftingblacklist.networking.PacketHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;

import java.util.*;

@Config(modid = UncraftingBlacklist.MODID)
public class UncraftingBlacklistConfig {
  @Config.Ignore
  public static Set<UUID> synchronisedPlayers = new HashSet<>();

  @Config.Comment({"List of item inputs for recipes which should be blacklisted from being uncrafted.", "Format: modname:itemname:metadata.", "Non-present metadata will be considered 0. Use * to specify any damage.", "Use: ore:oreDictionaryName to specify an ore dictionary output (i.e., planksWood)"})
  @Config.Name("Uncrafting Blacklist by Inputs")
  public static String[] itemInputBlacklist = new String[]{};

  @Config.Ignore
  private static Ingredient inputBlacklist = null;

  @Config.Ignore
  private static Ingredient clientInputBlacklist = null;

  @Config.Comment({"List of item ingredients that indicate a recipe with those in its result should be blacklisted from uncrafting.", "Format: modname:itemname:metadata.", "Non-present metadata will be considered 0. Use * to specify any damage.", "Use: ore:oreDictionaryName to specify an ore dictionary ingredient."})
  @Config.Name("Uncrafting Blacklist by Outputs")
  public static String[] itemOutputBlacklist = new String[]{};

  @Config.Ignore
  private static Ingredient outputsBlacklist = null;

  @Config.Ignore
  private static Ingredient clientOutputsBlacklist = null;

  public static Ingredient getInputBlacklist() {
    if (clientInputBlacklist != null) {
      return clientInputBlacklist;
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

    getInputBlacklist();
    getOutputsBlacklist();

    ClientSyncPacket inputs = new ClientSyncPacket(syncInfoInput, true);
    PacketHandler.sendTo(inputs, player);
    inputs = new ClientSyncPacket(syncInfoOutputs, false);
    PacketHandler.sendTo(inputs, player);
    synchronisedPlayers.add(player.getUniqueID());
  }

  public static void unsynchronisePlayer (List<SyncInfo> info, boolean inputs) {
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

  public static void clearPlayer (EntityPlayer player) {
    synchronisedPlayers.remove(player.getUniqueID());
  }

  public static void clear () {
    syncInfoOutputs = null;
    syncInfoInput = null;
    inputBlacklist = null;
    outputsBlacklist = null;
    clientInputBlacklist = null;
    clientOutputsBlacklist = null;
    synchronisedPlayers.clear();
  }

  @Config.Ignore
  private static List<SyncInfo> syncInfoInput = null;

  @Config.Ignore
  private static List<SyncInfo> syncInfoOutputs = null;

  public static class SyncInfo {
    private String ore = null;
    private int item = -1;
    private int meta = 0;

    public SyncInfo (int item, int meta) {
      this.item = item;
      this.meta = meta;
    }

    public SyncInfo(Item item, int meta) {
      this.item = RecipeItemHelper.pack(new ItemStack(item));
      this.meta = meta;
    }

    public SyncInfo(String ore) {
      this.ore = ore;
    }

    public boolean isOre () {
      return ore != null;
    }

    public ItemStack getItem () {
      ItemStack result = RecipeItemHelper.unpack(item);
      result.setItemDamage(meta);
      return result;
    }

    public List<ItemStack> getOre () {
      return Arrays.asList(new OreIngredient(ore).getMatchingStacks());
    }

    public void writeBuf(ByteBuf buf) {
      if (this.ore == null) {
        buf.writeBoolean(false);
        buf.writeInt(item);
        buf.writeShort((short) meta);
      } else {
        buf.writeBoolean(true);
        ByteBufUtils.writeUTF8String(buf, ore);
      }
    }

    public static SyncInfo readBuf(ByteBuf buf) {
      boolean ore = buf.readBoolean();
      if (ore) {
        String oreValue = ByteBufUtils.readUTF8String(buf);
        return new SyncInfo(oreValue);
      } else {
        int packed = buf.readInt();
        int meta = buf.readShort();
        return new SyncInfo(packed, meta);
      }
    }
  }
}
