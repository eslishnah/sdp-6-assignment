package hotel.listeners;
import hotel.events.HotelEvent;
import hotel.observer.Observer;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
/** In-memory notification outbox; no real messages are sent. */
public class GuestNotifier implements Observer<HotelEvent> {
    private final ConcurrentLinkedQueue<String> messages = new ConcurrentLinkedQueue<>();
    @Override public void onEvent(HotelEvent event) { messages.add("Booking confirmed: " + event.spaceId()); }
    public List<String> messages() { return List.copyOf(messages); }
}
