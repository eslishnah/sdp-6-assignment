package hotel.visitor;
import hotel.model.Space;
import hotel.state.RoomState;
import java.util.Map;
public class OccupancyVisitor extends WalkingVisitor<OccupancyVisitor.Summary> {
    public record Summary(int total, int occupied, int dirty, int cleaningMinutes) { }
    private final Map<String, RoomState> states;
    private int total;
    private int occupied;
    private int dirty;
    private int cleaningMinutes;
    public OccupancyVisitor(Map<String, RoomState> states) { this.states = Map.copyOf(states); }
    @Override protected void inspect(Space space) {
        RoomState state = states.getOrDefault(space.id(), RoomState.AVAILABLE);
        total++;
        if (state.occupied()) { occupied++; }
        if (state.dirty()) { dirty++; cleaningMinutes += space.cleaningMinutes(); }
    }
    @Override protected Summary result() { return new Summary(total, occupied, dirty, cleaningMinutes); }
}
