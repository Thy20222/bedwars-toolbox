package dev.thy.bedwarstoolbox.core.event;

public class Event {
    private final boolean cancellable;
    private boolean cancelled;

    public Event() {
        this(false);
    }

    public Event(boolean cancellable) {
        this.cancellable = cancellable;
    }

    public boolean isCancellable() {
        return cancellable;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        if (cancellable) {
            this.cancelled = cancelled;
        }
    }
}
