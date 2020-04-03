package doomanidus.mods.uncraftingblacklist.networking;

import doomanidus.mods.uncraftingblacklist.config.UBConfig;
import doomanidus.mods.uncraftingblacklist.events.ClientTickHandler;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ClientSyncClearPacket implements IMessage {

  public ClientSyncClearPacket() {
  }

  @Override
  public void fromBytes(ByteBuf buf) {
  }

  @Override
  public void toBytes(ByteBuf buf) {
  }

  public static class Handler implements IMessageHandler<ClientSyncClearPacket, IMessage> {
    @SideOnly(Side.CLIENT)
    @Override
    public IMessage onMessage(ClientSyncClearPacket message, MessageContext ctx) {
      ClientTickHandler.addRunnable(() -> handleConfig(message, ctx));
      return null;
    }

    @SideOnly(Side.CLIENT)
    public void handleConfig(ClientSyncClearPacket message, MessageContext ctx) {
      UBConfig.clearClient();
    }
  }
}
