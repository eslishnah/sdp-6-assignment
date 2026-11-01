package hotel;
import hotel.model.*;
import hotel.state.*;
import java.util.List;
public final class DemoData {
    private DemoData() { }
    public static Hotel hotel() {
        return new Hotel("hotel", "Astana Hotel", List.of(
            new Floor("F1", "First floor", List.of(
                new StandardRoom("101", "Room 101", 20000, 20),
                new Suite("102", "Suite 102", 40000, 40))),
            new Floor("F2", "Second floor", List.of(
                new ConferenceHall("201", "Conference hall", 60000, 60),
                new Restaurant("202", "Restaurant", 80000, 50)))));
    }
    public static RoomStateStore states() {
        RoomStateStore states = new RoomStateStore();
        states.update("101", new RoomState(true, false));
        states.update("102", new RoomState(false, true));
        states.update("201", new RoomState(true, true));
        states.update("202", new RoomState(false, true));
        return states;
    }
}
