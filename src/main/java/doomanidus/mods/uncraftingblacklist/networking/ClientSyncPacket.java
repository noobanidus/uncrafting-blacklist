package doomanidus.mods.uncraftingblacklist.networking;

import doomanidus.mods.uncraftingblacklist.UncraftingBlacklistConfig;
import doomanidus.mods.uncraftingblacklist.events.ClientTickHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class ClientSyncPacket implements IMessage {
  private List<UncraftingBlacklistConfig.SyncInfo> info;
  private boolean inputs = false;

  public ClientSyncPacket() {
  }

  public ClientSyncPacket(List<UncraftingBlacklistConfig.SyncInfo> info, boolean inputs) {
    this.info = info;
    this.inputs = inputs;
  }

  public List<UncraftingBlacklistConfig.SyncInfo> getInfo() {
    return info;
  }

  public boolean isInputs() {
    return inputs;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    this.inputs = buf.readBoolean();
    this.info = new ArrayList<>();
    int length = buf.readInt();
    for (int i = 0; i < length; i++) {
      this.info.add(UncraftingBlacklistConfig.SyncInfo.readBuf(buf));
    }
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeBoolean(inputs);
    buf.writeInt(info.size());
    for (UncraftingBlacklistConfig.SyncInfo i : info) {
      i.writeBuf(buf);
    }
  }

  public static class Handler implements IMessageHandler<ClientSyncPacket, IMessage> {
    @SideOnly(Side.CLIENT)
    @Override
    public IMessage onMessage(ClientSyncPacket message, MessageContext ctx) {
      ClientTickHandler.addRunnable(() -> handleConfig(message, ctx));
      return null;
    }

    @SideOnly(Side.CLIENT)
    public void handleConfig (ClientSyncPacket message, MessageContext ctx) {
      UncraftingBlacklistConfig.unsynchronisePlayer(message.getInfo(), message.isInputs());
    }
  }
}
