package dev.thy.bedwarstoolbox.core.stats;

import dev.thy.bedwarstoolbox.core.Global;
import dev.thy.bedwarstoolbox.core.event.ChatReceivedEvent;
import dev.thy.bedwarstoolbox.core.event.Subscribe;
import dev.thy.bedwarstoolbox.feature.render.BedwarsOverlay;
import dev.thy.bedwarstoolbox.feature.render.NametagsStats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BedwarsStatsController implements Global {
    private boolean waitingForAutoWho;

    @Subscribe
    public void onChat(ChatReceivedEvent event) {
        if (mc.thePlayer == null || !isStatsFeatureActive()) {
            return;
        }

        syncActiveConfigs();

        String message = event.getMessage().getUnformattedText();
        if (isLobbyOrServerTransferMessage(message)) {
            waitingForAutoWho = false;
            BedwarsStatsService.setBedwarsGameActive(false);
            return;
        }

        if (shouldAutoWho() && isBedwarsStartMessage(message)) {
            waitingForAutoWho = true;
            BedwarsStatsService.setBedwarsGameActive(true);
            mc.thePlayer.sendChatMessage("/who");
            return;
        }

        if (waitingForAutoWho && message.startsWith("ONLINE:")) {
            waitingForAutoWho = false;
            BedwarsStatsService.setBedwarsGameActive(true);
            List<String> players = parseOnlinePlayers(message);
            BedwarsStatsService.setBedwarsPlayers(players);
            BedwarsStatsService.requestAll(players, BedwarsOverlay::showThreatIfNeeded);
        }
    }

    private boolean isStatsFeatureActive() {
        return BedwarsOverlay.isActive() || NametagsStats.isActive();
    }

    private boolean shouldAutoWho() {
        return (BedwarsOverlay.isActive() && BedwarsOverlay.shouldAutoWho())
                || (NametagsStats.isActive() && NametagsStats.shouldAutoWho());
    }

    private void syncActiveConfigs() {
        if (BedwarsOverlay.isActive()) {
            BedwarsOverlay.syncConfig();
        }
        if (NametagsStats.isActive()) {
            NametagsStats.syncConfig();
        }
    }

    private boolean isBedwarsStartMessage(String message) {
        return message.contains("Protect your bed and destroy the enemy beds.")
                && !message.contains(":")
                && !message.contains("SHOUT");
    }

    private boolean isLobbyOrServerTransferMessage(String message) {
        return message.contains("joined the lobby!")
                || message.startsWith("Sending you to ")
                || message.contains("You are currently connected to server");
    }

    private List<String> parseOnlinePlayers(String message) {
        String playersString = message.substring("ONLINE:".length()).trim();
        if (playersString.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(playersString.split(",\\s*")));
    }
}
