package dev.thy.bedwarstoolbox.core.command;

import dev.thy.bedwarstoolbox.feature.render.BedwarsOverlay;
import dev.thy.bedwarstoolbox.feature.render.NametagsStats;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

public class HypixelCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "bwthypixel";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("bwthypixelkey", "hypixelkey");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/bwthypixel <clear|key>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length != 1) {
            sendUsage(sender);
            return;
        }

        String value = args[0].trim();
        if (value.equalsIgnoreCase("clear")) {
            setKey("");
            sender.addChatMessage(message(EnumChatFormatting.YELLOW + "Hypixel API key cleared."));
            return;
        }

        setKey(value);
        sender.addChatMessage(message(EnumChatFormatting.GREEN + "Hypixel API key saved."));
    }

    private void setKey(String key) {
        BedwarsOverlay.setHypixelApiKey(key);
        NametagsStats.setHypixelApiKey(key);
    }

    private void sendUsage(ICommandSender sender) {
        sender.addChatMessage(message(EnumChatFormatting.RED + "Usage: " + getCommandUsage(sender)));
    }

    private ChatComponentText message(String text) {
        return new ChatComponentText(EnumChatFormatting.GRAY + "[" + EnumChatFormatting.AQUA + "BWT" + EnumChatFormatting.GRAY + "] "
                + EnumChatFormatting.RESET + text);
    }
}
