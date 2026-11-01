package hotel;
import hotel.visitor.CleaningCostVisitor;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CleaningCostTest {
    @Test void costMatchesHandCalculation() {
        // Only vacant dirty spaces: 102 (40 min) and 202 (50 min). 90 * 100 = 9000.
        assertEquals(9000L, DemoData.hotel().accept(new CleaningCostVisitor(DemoData.states().snapshot(), 100)));
    }
    @Test void negativeCostIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new CleaningCostVisitor(java.util.Map.of(), -1));
    }
}
