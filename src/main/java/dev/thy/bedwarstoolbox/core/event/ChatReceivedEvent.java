package dev.thy.bedwarstoolbox.core.event;

import net.minecraft.util.IChatComponent;

public class ChatReceivedEvent extends Event {
    private final IChatComponent message;

    public ChatReceivedEvent(IChatComponent message) {
        this.message = message;
    }

    public IChatComponent getMessage() {
        return message;
    }
}
