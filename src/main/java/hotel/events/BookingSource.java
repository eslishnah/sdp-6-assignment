package hotel.events;
import hotel.observer.ThreadSafeSubject;
import java.util.function.Consumer;
public class BookingSource extends ThreadSafeSubject<HotelEvent> {
    public BookingSource(Consumer<RuntimeException> errors) { super(errors); }
    public void book(String floorId, String spaceId) {
        publish(HotelEvent.change(EventType.BOOKING_CREATED, floorId, spaceId));
    }
}
