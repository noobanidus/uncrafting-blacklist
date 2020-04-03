package doomanidus.mods.uncraftingblacklist.config;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.oredict.OreIngredient;

import java.util.Arrays;
import java.util.List;

public class SyncInfo {
  private String ore = null;
  private int item = -1;
  private int meta = 0;

  public SyncInfo(int item, int meta) {
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

  public boolean isOre() {
    return ore != null;
  }

  public ItemStack getItem() {
    ItemStack result = RecipeItemHelper.unpack(item);
    result.setItemDamage(meta);
    return result;
  }

  public List<ItemStack> getOre() {
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
