package doomanidus.mods.uncraftingblacklist.events;

import doomanidus.mods.uncraftingblacklist.UncraftingBlacklist;
import doomanidus.mods.uncraftingblacklist.UncraftingBlacklistConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

@Mod.EventBusSubscriber(modid = UncraftingBlacklist.MODID)
public class EventHandler {
  @SubscribeEvent
  public static void onContainerOpen(PlayerContainerEvent.Open event) {
    EntityPlayer player = event.getEntityPlayer();
    if (player.world.isRemote) {
      return;
    }

    if (player instanceof EntityPlayerMP) {
      EntityPlayerMP mpPlayer = (EntityPlayerMP) player;
      UncraftingBlacklistConfig.synchroniseToPlayer(mpPlayer);
    }
  }

  @SubscribeEvent
  public static void onPlayerLoggedIn (PlayerEvent.PlayerLoggedInEvent event) {
    if (!event.player.world.isRemote) {
      UncraftingBlacklistConfig.clearPlayer(event.player);
    }
  }

  @SubscribeEvent
  public static void onPlayerLoggedOut (PlayerEvent.PlayerLoggedOutEvent event) {
    if (!event.player.world.isRemote) {
      UncraftingBlacklistConfig.clearPlayer(event.player);
    }
  }
}
