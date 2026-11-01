package hotel.state;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
public class RoomStateStore {
    private final Map<String, RoomState> states = new ConcurrentHashMap<>();
    public void update(String id, RoomState state) {
        states.put(Objects.requireNonNull(id), Objects.requireNonNull(state));
    }
    public Map<String, RoomState> snapshot() { return Map.copyOf(states); }
}
