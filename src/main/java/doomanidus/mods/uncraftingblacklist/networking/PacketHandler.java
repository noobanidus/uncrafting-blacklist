package doomanidus.mods.uncraftingblacklist.networking;

import doomanidus.mods.uncraftingblacklist.UncraftingBlacklist;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
  public static SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(UncraftingBlacklist.MODID);

  private static int id = 0;

  public static void registerMessages () {
    registerMessage(ClientSyncPacket.Handler.class, ClientSyncPacket.class, Side.CLIENT);
    registerMessage(ClientSyncClearPacket.Handler.class, ClientSyncClearPacket.class, Side.CLIENT);
  }

  public static <REQ extends IMessage, REPLY extends IMessage> void registerMessage(Class<? extends IMessageHandler<REQ, REPLY>> handler, Class<REQ> message, Side side) {
    INSTANCE.registerMessage(handler, message, id++, side);
  }

  public static void sendTo(IMessage message, EntityPlayerMP player) {
    INSTANCE.sendTo(message, player);
  }

  public static void sendToAll(IMessage message) {
    INSTANCE.sendToAll(message);
  }
}
