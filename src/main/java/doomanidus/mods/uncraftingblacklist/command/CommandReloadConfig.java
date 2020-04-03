package doomanidus.mods.uncraftingblacklist.command;

import doomanidus.mods.uncraftingblacklist.UncraftingBlacklist;
import doomanidus.mods.uncraftingblacklist.config.UBConfig;
import doomanidus.mods.uncraftingblacklist.networking.ClientSyncClearPacket;
import doomanidus.mods.uncraftingblacklist.networking.ClientSyncPacket;
import doomanidus.mods.uncraftingblacklist.networking.PacketHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

public class CommandReloadConfig extends CommandBase {
  public CommandReloadConfig() {
  }

  @Override
  public String getName() {
    return "ubreload";
  }

  @Override
  public String getUsage(ICommandSender sender) {
    return "/ubreload";
  }

  @Override
  public List<String> getAliases() {
    return Collections.singletonList("ubreload");
  }

  @Override
  public int getRequiredPermissionLevel() {
    return 2;
  }

  @Override
  public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
    UBConfig.clear();
    StringJoiner join = new StringJoiner(", ");
    for (String s : UBConfig.itemInputBlacklist) {
      join.add(s);
    }
    UncraftingBlacklist.LOG.info(join.toString());
    UBConfig.load();
    join = new StringJoiner(", ");
    for (String s : UBConfig.itemInputBlacklist) {
      join.add(s);
    }
    UncraftingBlacklist.LOG.info(join.toString());

    ClientSyncClearPacket packet = new ClientSyncClearPacket();
    PacketHandler.sendToAll(packet);
    ClientSyncPacket info = new ClientSyncPacket(UBConfig.getSyncInfoInput(), true);
    PacketHandler.sendToAll(info);
    info = new ClientSyncPacket(UBConfig.getSyncInfoOutputs(), false);
    PacketHandler.sendToAll(info);

    for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
      UBConfig.synchronisedPlayers.add(player.getUniqueID());
    }
    sender.sendMessage(new TextComponentTranslation("ub.reload_success"));
  }
}
