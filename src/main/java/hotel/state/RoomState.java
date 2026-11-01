package hotel.state;
public record RoomState(boolean occupied, boolean dirty) {
    public static final RoomState AVAILABLE = new RoomState(false, false);
}
