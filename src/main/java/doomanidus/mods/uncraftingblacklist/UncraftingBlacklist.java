package doomanidus.mods.uncraftingblacklist;

import doomanidus.mods.uncraftingblacklist.command.CommandReloadConfig;
import doomanidus.mods.uncraftingblacklist.config.UBConfig;
import doomanidus.mods.uncraftingblacklist.networking.PacketHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber
@Mod(modid = UncraftingBlacklist.MODID, name = UncraftingBlacklist.MODNAME, version = UncraftingBlacklist.VERSION, dependencies = "required-after:twilightforest;")
@SuppressWarnings("WeakerAccess")
public class UncraftingBlacklist {
  public static final String MODID = "uncrafting_blacklist";
  public static final String MODNAME = "Uncrafting Blacklist";
  public static final String VERSION = "GRADLE:VERSION";

  public static Logger LOG = LogManager.getLogger(MODID);

  public UncraftingBlacklist() {
    UBConfig.load();
  }

  @SuppressWarnings("unused")
  @Mod.Instance(UncraftingBlacklist.MODID)
  public static UncraftingBlacklist instance;

  @Mod.EventHandler
  public static void serverAboutToStart(FMLServerAboutToStartEvent event) {
    UBConfig.clear();
  }

  @Mod.EventHandler
  public static void serverStarting(FMLServerStartingEvent event) {
    event.registerServerCommand(new CommandReloadConfig());
  }

  @Mod.EventHandler
  public static void init(FMLInitializationEvent event) {
    PacketHandler.registerMessages();
  }
}
