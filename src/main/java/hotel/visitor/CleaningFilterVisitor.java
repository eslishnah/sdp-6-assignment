package hotel.visitor;
import hotel.model.Space;
import hotel.state.RoomState;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class CleaningFilterVisitor extends WalkingVisitor<List<Space>> {
    private final Map<String, RoomState> states;
    private final List<Space> matches = new ArrayList<>();
    public CleaningFilterVisitor(Map<String, RoomState> states) { this.states = Map.copyOf(states); }
    @Override protected void inspect(Space space) {
        RoomState state = states.getOrDefault(space.id(), RoomState.AVAILABLE);
        if (state.dirty() && !state.occupied()) { matches.add(space); }
    }
    @Override protected List<Space> result() { return List.copyOf(matches); }
}
