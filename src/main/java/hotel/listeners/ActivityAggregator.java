package hotel.listeners;
import hotel.events.*;
import hotel.observer.Observer;
import hotel.observer.ThreadSafeSubject;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
/** Counts activity, not current occupancy. Publishes every batch of events for each floor. */
public class ActivityAggregator extends ThreadSafeSubject<HotelEvent> implements Observer<HotelEvent> {
    private final ConcurrentHashMap<String, AtomicInteger> counts = new ConcurrentHashMap<>();
    private final int batchSize;
    public ActivityAggregator(int batchSize, Consumer<RuntimeException> errors) {
        super(errors);
        if (batchSize < 1) { throw new IllegalArgumentException("Batch must be positive"); }
        this.batchSize = batchSize;
    }
    @Override public void onEvent(HotelEvent event) {
        int count = counts.computeIfAbsent(event.floorId(), key -> new AtomicInteger()).incrementAndGet();
        if (count % batchSize == 0) {
            publish(new HotelEvent(EventType.ACTIVITY_SUMMARY, event.floorId(), "", List.of(), count));
        }
    }
}
