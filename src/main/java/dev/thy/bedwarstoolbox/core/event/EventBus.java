package dev.thy.bedwarstoolbox.core.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class EventBus {
    private final List<Subscriber> subscribers = new ArrayList<>();

    public void register(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Subscribe.class)) {
                continue;
            }

            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != 1 || !Event.class.isAssignableFrom(parameterTypes[0])) {
                throw new IllegalArgumentException("Event subscriber must have exactly one Event parameter: " + method);
            }

            method.setAccessible(true);
            subscribers.add(new Subscriber(listener, method, parameterTypes[0]));
        }
    }

    public <T extends Event> T post(T event) {
        for (Subscriber subscriber : subscribers) {
            if (subscriber.eventType.isAssignableFrom(event.getClass())) {
                subscriber.invoke(event);
            }
        }

        return event;
    }

    private static class Subscriber {
        private final Object listener;
        private final Method method;
        private final Class<?> eventType;

        private Subscriber(Object listener, Method method, Class<?> eventType) {
            this.listener = listener;
            this.method = method;
            this.eventType = eventType;
        }

        private void invoke(Event event) {
            try {
                method.invoke(listener, event);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            }
        }
    }
}
