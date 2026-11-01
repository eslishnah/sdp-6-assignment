package hotel.visitor;
import hotel.model.Space;
import hotel.state.RoomState;
import java.util.Map;
/** Revenue for one simulated day; integer tenge avoids floating point rounding. */
public class RevenueVisitor extends WalkingVisitor<Long> {
    private final Map<String, RoomState> states;
    private long total;
    public RevenueVisitor(Map<String, RoomState> states) { this.states = Map.copyOf(states); }
    @Override protected void inspect(Space space) {
        if (states.getOrDefault(space.id(), RoomState.AVAILABLE).occupied()) {
            total += space.dailyRate();
        }
    }
    @Override protected Long result() { return total; }
}
