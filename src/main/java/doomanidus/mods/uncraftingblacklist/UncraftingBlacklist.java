package doomanidus.mods.uncraftingblacklist;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber
@Mod(modid = UncraftingBlacklist.MODID, name = UncraftingBlacklist.MODNAME, version = UncraftingBlacklist.VERSION)
@SuppressWarnings("WeakerAccess")
public class UncraftingBlacklist {
  public static final String MODID = "uncrafting_blacklist";
  public static final String MODNAME = "Uncrafting Blacklist";
  public static final String VERSION = "GRADLE:VERSION";

  public static Logger LOG = LogManager.getLogger(MODID);

  @SuppressWarnings("unused")
  @Mod.Instance(UncraftingBlacklist.MODID)
  public static UncraftingBlacklist instance;

  @Mod.EventHandler
  public static void serverAboutToStart(FMLServerAboutToStartEvent event) {
    UncraftingBlacklistConfig.clear();
  }
}