package hotel;
import hotel.model.*;
import hotel.state.RoomState;
import hotel.visitor.*;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
class SpaTest {
    @Test void newElementSupportsExistingOperations() {
        HotelElement element = new Spa("S", "Spa", 30000, 30);
        var states = Map.of("S", new RoomState(true, true));
        assertEquals(30000L, element.accept(new RevenueVisitor(states)));
        assertEquals(new OccupancyVisitor.Summary(1, 1, 1, 30), element.accept(new OccupancyVisitor(states)));
        assertTrue(element.accept(new CleaningFilterVisitor(states)).isEmpty());
        assertEquals(0L, element.accept(new CleaningCostVisitor(states, 100)));
        assertTrue(element.accept(new JsonExportVisitor()).contains("spa"));
    }
}
