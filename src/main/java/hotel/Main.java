package hotel;
import hotel.concurrent.ProducerRunner;
import hotel.events.*;
import hotel.listeners.*;
import hotel.model.*;
import hotel.observer.ThreadSafeSubject;
import hotel.state.*;
import hotel.visitor.*;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/** Composition root: constructs and connects all application dependencies. */
public class Main {
    public static void main(String[] args) throws Exception {
        int threads = 3;
        if (args.length == 2 && args[0].equals("--threads")) { threads = Integer.parseInt(args[1]); }
        else if (args.length != 0) { throw new IllegalArgumentException("Usage: Main [--threads N]"); }
        ProducerRunner runner = new ProducerRunner(threads);
        Hotel hotel = DemoData.hotel();
        RoomStateStore states = DemoData.states();
        ConcurrentLinkedQueue<RuntimeException> failures = new ConcurrentLinkedQueue<>();
        Consumer<RuntimeException> errors = failures::add;
        BookingSource bookings = new BookingSource(errors);
        RoomStatusSource status = new RoomStatusSource(states, errors);
        ThreadSafeSubject<HotelEvent> reports = new ThreadSafeSubject<>(errors);
        FrontDesk desk = new FrontDesk();
        GuestNotifier guests = new GuestNotifier();
        AuditLog audit = new AuditLog();
        ActivityAggregator activity = new ActivityAggregator(2, errors);
        Housekeeping housekeeping = new Housekeeping(hotel, states, reports);
        bookings.subscribe(EventType.BOOKING_CREATED, desk);
        bookings.subscribe(EventType.BOOKING_CREATED, guests);
        bookings.subscribe(EventType.BOOKING_CREATED, audit);
        bookings.subscribe(EventType.BOOKING_CREATED, activity);
        status.subscribe(EventType.ROOM_CHANGED, desk);
        status.subscribe(EventType.ROOM_CHANGED, audit);
        status.subscribe(EventType.ROOM_CHANGED, activity);
        status.subscribe(EventType.ROOM_CHANGED, housekeeping);
        activity.subscribe(EventType.ACTIVITY_SUMMARY, audit);
        activity.subscribe(EventType.ACTIVITY_SUMMARY, desk);
        reports.subscribe(EventType.CLEANING_REPORT, audit);
        boolean guestSubscribed = true;
        try (Scanner input = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n1 Reports | 2 Checkout 101 | 3 Toggle guest listener | 4 Book 101");
                System.out.println("5 Concurrent demo | 6 JSON | 7 Event log | 0 Exit");
                if (!input.hasNextLine()) { break; }
                String choice = input.nextLine().trim();
                switch (choice) {
                    case "1" -> {
                        var snapshot = states.snapshot();
                        System.out.println("Daily revenue: " + hotel.accept(new RevenueVisitor(snapshot)) + " KZT");
                        System.out.println("Occupancy: " + hotel.accept(new OccupancyVisitor(snapshot)));
                        System.out.println("Cleaning: " + hotel.accept(new CleaningFilterVisitor(snapshot)).stream().map(Space::id).toList());
                    }
                    case "2" -> { status.change("F1", "101", new RoomState(false, true)); System.out.println("Checkout recorded; floor F1 inspected."); }
                    case "3" -> {
                        if (guestSubscribed) { bookings.unsubscribe(EventType.BOOKING_CREATED, guests); }
                        else { bookings.subscribe(EventType.BOOKING_CREATED, guests); }
                        guestSubscribed = !guestSubscribed;
                        System.out.println("Guest listener subscribed: " + guestSubscribed);
                    }
                    case "4" -> { bookings.book("F1", "101"); System.out.println("Guest notifications: " + guests.messages().size()); }
                    case "5" -> {
                        int before = audit.entries().size();
                        runner.run(List.of(
                            () -> bookings.book("F1", "101"),
                            () -> status.change("F1", "102", new RoomState(false, true)),
                            () -> status.change("F2", "202", new RoomState(false, true))));
                        System.out.println("3 producers completed; pool threads: " + threads + "; new log entries: " + (audit.entries().size() - before));
                    }
                    case "6" -> System.out.println(hotel.accept(new JsonExportVisitor()));
                    case "7" -> { audit.entries().forEach(System.out::println); System.out.println("Front desk updates: " + desk.updates()); }
                    case "0" -> { System.out.println("Goodbye. Listener failures: " + failures.size()); return; }
                    default -> System.out.println("Choose 0 to 7.");
                }
                if (!failures.isEmpty()) { System.out.println("Listener errors: " + failures); }
            }
        }
    }
}
