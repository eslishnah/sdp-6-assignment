package hotel.listeners;
import hotel.events.HotelEvent;
import hotel.observer.Observer;
import java.util.concurrent.atomic.AtomicInteger;
public class FrontDesk implements Observer<HotelEvent> {
    private final AtomicInteger updates = new AtomicInteger();
    @Override public void onEvent(HotelEvent event) { updates.incrementAndGet(); }
    public int updates() { return updates.get(); }
}
