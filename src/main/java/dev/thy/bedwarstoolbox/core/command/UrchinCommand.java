package dev.thy.bedwarstoolbox.core.command;

import dev.thy.bedwarstoolbox.feature.render.BedwarsOverlay;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.List;

public class UrchinCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "bwturchin";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("bwturchinkey", "urchinkey");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/bwturchin <on|off|clear|key>";
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
        if (value.equalsIgnoreCase("on")) {
            BedwarsOverlay.setUrchinEnabled(true);
            sender.addChatMessage(message(EnumChatFormatting.GREEN + "Urchin tags enabled."));
            return;
        }

        if (value.equalsIgnoreCase("off")) {
            BedwarsOverlay.setUrchinEnabled(false);
            sender.addChatMessage(message(EnumChatFormatting.YELLOW + "Urchin tags disabled."));
            return;
        }

        if (value.equalsIgnoreCase("clear")) {
            BedwarsOverlay.setUrchinKey("");
            sender.addChatMessage(message(EnumChatFormatting.YELLOW + "Urchin API key cleared."));
            return;
        }

        BedwarsOverlay.setUrchinKey(value);
        sender.addChatMessage(message(EnumChatFormatting.GREEN + "Urchin API key saved."));
    }

    private void sendUsage(ICommandSender sender) {
        sender.addChatMessage(message(EnumChatFormatting.RED + "Usage: " + getCommandUsage(sender)));
    }

    private ChatComponentText message(String text) {
        return new ChatComponentText(EnumChatFormatting.GRAY + "[" + EnumChatFormatting.AQUA + "BWT" + EnumChatFormatting.GRAY + "] "
                + EnumChatFormatting.RESET + text);
    }
}
