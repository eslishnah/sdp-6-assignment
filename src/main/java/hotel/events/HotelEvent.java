package hotel.events;
import java.util.List;
import java.util.Objects;
public record HotelEvent(EventType type, String floorId, String spaceId, List<String> details, int count)
        implements TypedEvent {
    public HotelEvent {
        Objects.requireNonNull(type);
        Objects.requireNonNull(floorId);
        Objects.requireNonNull(spaceId);
        details = List.copyOf(details);
    }
    public static HotelEvent change(EventType type, String floorId, String spaceId) {
        return new HotelEvent(type, floorId, spaceId, List.of(), 1);
    }
}
