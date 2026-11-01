package hotel;
import hotel.concurrent.ProducerRunner;
import hotel.events.*;
import hotel.listeners.*;
import hotel.observer.ThreadSafeSubject;
import hotel.state.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
class IntegrationTest {
    @Test void checkoutInspectsOnlyRelatedFloorAndPublishesReport() {
        RoomStateStore states = DemoData.states();
        var reports = new ThreadSafeSubject<HotelEvent>(error -> fail(error));
        AuditLog audit = new AuditLog();
        reports.subscribe(EventType.CLEANING_REPORT, audit);
        var housekeeping = new Housekeeping(DemoData.hotel(), states, reports);
        var source = new RoomStatusSource(states, error -> fail(error));
        source.subscribe(EventType.ROOM_CHANGED, housekeeping);
        source.change("F1", "101", new RoomState(false, true));
        assertEquals(1, audit.entries().size());
        HotelEvent report = audit.entries().get(0);
        assertEquals(List.of("101", "102"), report.details());
        assertEquals(2, report.count()); assertEquals("F1", report.floorId());
        assertFalse(report.details().contains("202"));
    }
    @Test @Timeout(10) void gracefulShutdownDrainsThreeProducers() throws Exception {
        AtomicInteger completed = new AtomicInteger();
        Runnable producer = () -> { for (int i = 0; i < 1000; i++) { completed.incrementAndGet(); } };
        new ProducerRunner(3).run(List.of(producer, producer, producer));
        assertEquals(3000, completed.get());
    }
    @Test void producerFailureIsReportedAfterOtherTasksFinish() {
        AtomicInteger completed = new AtomicInteger();
        assertThrows(ExecutionException.class, () -> new ProducerRunner(2).run(List.of(
            () -> { throw new IllegalStateException("failed producer"); },
            () -> completed.incrementAndGet())));
        assertEquals(1, completed.get());
    }
    @Test void invalidPoolSizeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ProducerRunner(0));
    }
}
