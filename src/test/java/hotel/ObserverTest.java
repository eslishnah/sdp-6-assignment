package hotel;
import hotel.concurrent.ProducerRunner;
import hotel.events.*;
import hotel.listeners.*;
import hotel.observer.*;
import hotel.state.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class ObserverTest {
    private ThreadSafeSubject<HotelEvent> subject() {
        return new ThreadSafeSubject<>(error -> { throw new AssertionError(error); });
    }
    private HotelEvent booking() { return HotelEvent.change(EventType.BOOKING_CREATED, "F1", "101"); }
    @Test void onlyRequestedEventTypeIsDelivered() {
        var subject = subject();
        AtomicInteger count = new AtomicInteger();
        subject.subscribe(EventType.ROOM_CHANGED, event -> count.incrementAndGet());
        subject.publish(booking());
        assertEquals(0, count.get());
    }
    @Test void allListenersReceiveEvent() {
        var subject = subject();
        AtomicInteger first = new AtomicInteger();
        AtomicInteger second = new AtomicInteger();
        subject.subscribe(EventType.BOOKING_CREATED, event -> first.incrementAndGet());
        subject.subscribe(EventType.BOOKING_CREATED, event -> second.incrementAndGet());
        subject.publish(booking());
        assertEquals(1, first.get()); assertEquals(1, second.get());
    }
    @Test void duplicateSubscriptionIsIgnored() {
        var subject = subject();
        AuditLog log = new AuditLog();
        subject.subscribe(EventType.BOOKING_CREATED, log);
        subject.subscribe(EventType.BOOKING_CREATED, log);
        subject.publish(booking());
        assertEquals(1, log.entries().size());
    }
    @Test void listenerCanUnsubscribeItself() {
        var subject = subject();
        AtomicInteger calls = new AtomicInteger();
        hotel.observer.Observer<HotelEvent> once = new hotel.observer.Observer<>() {
            public void onEvent(HotelEvent event) {
                calls.incrementAndGet(); subject.unsubscribe(event.type(), this);
            }
        };
        subject.subscribe(EventType.BOOKING_CREATED, once);
        subject.publish(booking()); subject.publish(booking());
        assertEquals(1, calls.get());
    }
    @Test void listenerCanSubscribeAnotherDuringDelivery() {
        var subject = subject();
        AuditLog late = new AuditLog();
        subject.subscribe(EventType.BOOKING_CREATED, event -> subject.subscribe(event.type(), late));
        subject.publish(booking());
        assertTrue(late.entries().isEmpty());
        subject.publish(booking()); assertEquals(1, late.entries().size());
    }
    @Test void eventDefensivelyCopiesDetails() {
        List<String> details = new ArrayList<>(List.of("101"));
        var event = new HotelEvent(EventType.CLEANING_REPORT, "F1", "101", details, 1);
        details.clear();
        assertEquals(List.of("101"), event.details());
        assertThrows(UnsupportedOperationException.class, () -> event.details().add("102"));
    }
    @Test void listenerFailureDoesNotSkipOthers() {
        List<RuntimeException> errors = new ArrayList<>();
        var subject = new ThreadSafeSubject<HotelEvent>(errors::add);
        AuditLog audit = new AuditLog();
        subject.subscribe(EventType.BOOKING_CREATED, event -> { throw new IllegalStateException("test"); });
        subject.subscribe(EventType.BOOKING_CREATED, audit);
        subject.publish(booking());
        assertEquals(1, errors.size()); assertEquals(1, audit.entries().size());
    }
    @Test void aggregatorRepublishesEverySecondEventPerFloor() {
        var aggregator = new ActivityAggregator(2, error -> fail(error));
        AuditLog summaries = new AuditLog();
        aggregator.subscribe(EventType.ACTIVITY_SUMMARY, summaries);
        aggregator.onEvent(booking());
        assertTrue(summaries.entries().isEmpty());
        aggregator.onEvent(booking());
        aggregator.onEvent(HotelEvent.change(EventType.BOOKING_CREATED, "F2", "201"));
        assertEquals(1, summaries.entries().size());
        assertEquals(2, summaries.entries().get(0).count());
    }
    @Test void roomSourceUpdatesStateBeforeNotifying() {
        RoomStateStore states = new RoomStateStore();
        var source = new RoomStatusSource(states, error -> fail(error));
        source.subscribe(EventType.ROOM_CHANGED, event -> assertEquals(new RoomState(false,true), states.snapshot().get("101")));
        source.change("F1", "101", new RoomState(false, true));
    }
    @Test void removedListenerCanJoinAgain() {
        var subject = subject(); AuditLog audit = new AuditLog();
        subject.subscribe(EventType.BOOKING_CREATED, audit);
        subject.unsubscribe(EventType.BOOKING_CREATED, audit);
        subject.publish(booking()); assertTrue(audit.entries().isEmpty());
        subject.subscribe(EventType.BOOKING_CREATED, audit);
        subject.publish(booking()); assertEquals(1, audit.entries().size());
    }
    @Test @Timeout(10) void unsubscribeDuringPublishAffectsNextSnapshot() throws Exception {
        var subject = subject();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        hotel.observer.Observer<HotelEvent> blocked = event -> {
            calls.incrementAndGet(); entered.countDown();
            try { assertTrue(release.await(3, TimeUnit.SECONDS)); }
            catch (InterruptedException error) { Thread.currentThread().interrupt(); throw new AssertionError(error); }
        };
        subject.subscribe(EventType.BOOKING_CREATED, blocked);
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            Future<?> pending = pool.submit(() -> subject.publish(booking()));
            assertTrue(entered.await(3, TimeUnit.SECONDS));
            subject.unsubscribe(EventType.BOOKING_CREATED, blocked);
            release.countDown(); pending.get(3, TimeUnit.SECONDS);
            subject.publish(booking()); assertEquals(1, calls.get());
        } finally { release.countDown(); pool.shutdownNow(); assertTrue(pool.awaitTermination(3, TimeUnit.SECONDS)); }
    }
    @Test @Timeout(15) void fourThreadsTimesThousandEventsHasExactCounts() throws Exception {
        var subject = subject();
        AuditLog audit = new AuditLog();
        FrontDesk desk = new FrontDesk();
        var activity = new ActivityAggregator(100, error -> fail(error));
        AuditLog summaries = new AuditLog();
        activity.subscribe(EventType.ACTIVITY_SUMMARY, summaries);
        subject.subscribe(EventType.BOOKING_CREATED, audit);
        subject.subscribe(EventType.BOOKING_CREATED, desk);
        subject.subscribe(EventType.BOOKING_CREATED, activity);
        CyclicBarrier start = new CyclicBarrier(4);
        List<Runnable> producers = new ArrayList<>();
        for (int worker = 0; worker < 4; worker++) {
            producers.add(() -> {
                try { start.await(3, TimeUnit.SECONDS); }
                catch (Exception error) { throw new AssertionError(error); }
                for (int i = 0; i < 1000; i++) { subject.publish(booking()); }
            });
        }
        new ProducerRunner(4).run(producers);
        assertEquals(4000, audit.entries().size());
        assertEquals(4000, desk.updates());
        assertEquals(40, summaries.entries().size());
        assertEquals(4000, summaries.entries().stream().mapToInt(HotelEvent::count).max().orElseThrow());
    }
}
