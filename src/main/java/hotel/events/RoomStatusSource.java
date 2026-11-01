package hotel.events;
import hotel.observer.ThreadSafeSubject;
import hotel.state.RoomState;
import hotel.state.RoomStateStore;
import java.util.function.Consumer;
public class RoomStatusSource extends ThreadSafeSubject<HotelEvent> {
    private final RoomStateStore states;
    public RoomStatusSource(RoomStateStore states, Consumer<RuntimeException> errors) {
        super(errors);
        this.states = states;
    }
    public void change(String floorId, String spaceId, RoomState state) {
        states.update(spaceId, state);
        publish(HotelEvent.change(EventType.ROOM_CHANGED, floorId, spaceId));
    }
}
