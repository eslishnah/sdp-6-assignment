package hotel.observer;
import hotel.events.EventType;
import hotel.events.TypedEvent;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

/** Synchronous push delivery. A publish uses a snapshot of its event-type listeners. */
public class ThreadSafeSubject<E extends TypedEvent> implements Subject<E> {
    private final Map<EventType, CopyOnWriteArraySet<Observer<E>>> listeners = new ConcurrentHashMap<>();
    private final Consumer<RuntimeException> errorHandler;

    public ThreadSafeSubject(Consumer<RuntimeException> errorHandler) {
        this.errorHandler = Objects.requireNonNull(errorHandler);
        for (EventType type : EventType.values()) {
            listeners.put(type, new CopyOnWriteArraySet<>());
        }
    }
    @Override
    public void subscribe(EventType type, Observer<E> observer) {
        listeners.get(Objects.requireNonNull(type)).add(Objects.requireNonNull(observer));
    }
    @Override
    public void unsubscribe(EventType type, Observer<E> observer) {
        listeners.get(Objects.requireNonNull(type)).remove(Objects.requireNonNull(observer));
    }
    @Override
    public void publish(E event) {
        Objects.requireNonNull(event);
        for (Observer<E> observer : listeners.get(event.type())) {
            try {
                observer.onEvent(event);
            } catch (RuntimeException error) {
                errorHandler.accept(error);
            }
        }
    }
}
