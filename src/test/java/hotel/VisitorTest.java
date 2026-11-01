package hotel;
import hotel.model.*;
import hotel.state.*;
import hotel.visitor.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class VisitorTest {
    @Test void revenueMatchesHandCalculation() {
        // Only 101 and 201 are occupied: 20,000 + 60,000 = 80,000 KZT.
        assertEquals(80000L, DemoData.hotel().accept(new RevenueVisitor(DemoData.states().snapshot())));
    }
    @Test void occupancyAccumulatesAcrossBothFloors() {
        // 4 spaces, 2 occupied, 3 dirty; 40 + 60 + 50 = 150 cleaning minutes.
        var expected = new OccupancyVisitor.Summary(4, 2, 3, 150);
        assertEquals(expected, DemoData.hotel().accept(new OccupancyVisitor(DemoData.states().snapshot())));
    }
    @Test void filterExcludesOccupiedDirtyHall() {
        var result = DemoData.hotel().accept(new CleaningFilterVisitor(DemoData.states().snapshot()));
        assertEquals(List.of("102", "202"), result.stream().map(Space::id).toList());
    }
    @Test void visitorCanStartAtFloor() {
        var floor = DemoData.hotel().children().get(0);
        assertEquals(20000L, floor.accept(new RevenueVisitor(DemoData.states().snapshot())));
    }
    @Test void emptyTreeHasZeroRevenueAndNoMatches() {
        var empty = new Hotel("H", "Empty", List.of());
        assertEquals(0L, empty.accept(new RevenueVisitor(Map.of())));
        assertEquals(List.of(), empty.accept(new CleaningFilterVisitor(Map.of())));
    }
    @Test void missingStateMeansAvailable() {
        assertEquals(new OccupancyVisitor.Summary(4, 0, 0, 0),
            DemoData.hotel().accept(new OccupancyVisitor(Map.of())));
    }
    @Test void treeDefensivelyCopiesChildren() {
        List<Space> children = new ArrayList<>();
        children.add(new StandardRoom("1", "Room", 10, 5));
        Floor floor = new Floor("F", "Floor", children);
        children.clear(); assertEquals(1, floor.children().size());
        assertThrows(UnsupportedOperationException.class, () -> floor.children().clear());
    }
    @Test void stateSnapshotIsIndependentOfLaterUpdates() {
        RoomStateStore store = DemoData.states();
        var snapshot = store.snapshot();
        store.update("101", RoomState.AVAILABLE);
        assertTrue(snapshot.get("101").occupied());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.clear());
    }
    @Test void jsonHasExactNestingAndEscaping() {
        Hotel hotel = new Hotel("H", "Hotel", List.of(new Floor("F", "Floor", List.of(
            new StandardRoom("1", "A\"\\\n\t\u0001", 10, 5)))));
        String expected = "{\"type\":\"hotel\",\"id\":\"H\",\"name\":\"Hotel\",\"children\":["
            + "{\"type\":\"floor\",\"id\":\"F\",\"name\":\"Floor\",\"children\":["
            + "{\"type\":\"standardRoom\",\"id\":\"1\",\"name\":\"A\\\"\\\\\\n\\t\\u0001\",\"dailyRate\":10,\"cleaningMinutes\":5}]}]}";
        assertEquals(expected, hotel.accept(new JsonExportVisitor()));
    }
    @Test void jsonContainsEveryLeafType() {
        String json = DemoData.hotel().accept(new JsonExportVisitor());
        for (String type : List.of("standardRoom", "suite", "conferenceHall", "restaurant")) {
            assertTrue(json.contains("\"type\":\"" + type + "\""));
        }
        assertEquals("{\"type\":\"floor\",\"id\":\"F\",\"name\":\"Empty\",\"children\":[]}",
            new Floor("F", "Empty", List.of()).accept(new JsonExportVisitor()));
    }
    @Test void negativeRatesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Suite("1", "Suite", -1, 20));
    }
}
