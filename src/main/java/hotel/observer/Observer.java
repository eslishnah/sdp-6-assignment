package hotel.observer;
@FunctionalInterface
public interface Observer<E> { void onEvent(E event); }
