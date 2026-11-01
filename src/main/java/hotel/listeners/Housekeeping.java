package hotel.listeners;
import hotel.events.*;
import hotel.model.*;
import hotel.observer.Observer;
import hotel.observer.Subject;
import hotel.state.RoomStateStore;
import hotel.visitor.CleaningFilterVisitor;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
public class Housekeeping implements Observer<HotelEvent> {
    private final Map<String, Floor> floors;
    private final RoomStateStore states;
    private final Subject<HotelEvent> reports;
    public Housekeeping(Hotel hotel, RoomStateStore states, Subject<HotelEvent> reports) {
        floors = hotel.children().stream().collect(Collectors.toUnmodifiableMap(Floor::id, floor -> floor));
        this.states = states;
        this.reports = reports;
    }
    @Override public void onEvent(HotelEvent event) {
        Floor floor = floors.get(event.floorId());
        if (floor == null) { throw new IllegalArgumentException("Unknown floor: " + event.floorId()); }
        List<Space> rooms = floor.accept(new CleaningFilterVisitor(states.snapshot()));
        List<String> ids = rooms.stream().map(Space::id).toList();
        reports.publish(new HotelEvent(EventType.CLEANING_REPORT, floor.id(), event.spaceId(), ids, ids.size()));
    }
}
