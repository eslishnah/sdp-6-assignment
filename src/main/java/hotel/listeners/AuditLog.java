package hotel.listeners;
import hotel.events.HotelEvent;
import hotel.observer.Observer;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
public class AuditLog implements Observer<HotelEvent> {
    private final ConcurrentLinkedQueue<HotelEvent> events = new ConcurrentLinkedQueue<>();
    @Override public void onEvent(HotelEvent event) { events.add(event); }
    public List<HotelEvent> entries() { return List.copyOf(events); }
}
