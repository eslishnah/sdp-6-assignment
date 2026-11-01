package hotel.visitor;
import hotel.model.Space;
import hotel.state.RoomState;
import java.util.Map;
/** Adds an operation without modifying the element hierarchy. */
public class CleaningCostVisitor extends WalkingVisitor<Long> {
    private final Map<String, RoomState> states;
    private final long costPerMinute;
    private long total;
    public CleaningCostVisitor(Map<String, RoomState> states, long costPerMinute) {
        if (costPerMinute < 0) { throw new IllegalArgumentException("Cost must be nonnegative"); }
        this.states = Map.copyOf(states);
        this.costPerMinute = costPerMinute;
    }
    @Override protected void inspect(Space space) {
        RoomState state = states.getOrDefault(space.id(), RoomState.AVAILABLE);
        if (state.dirty() && !state.occupied()) { total += space.cleaningMinutes() * costPerMinute; }
    }
    @Override protected Long result() { return total; }
}
