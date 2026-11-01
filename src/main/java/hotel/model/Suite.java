package hotel.model;
import hotel.visitor.Visitor;
import java.util.Objects;
public record Suite(String id, String name, long dailyRate, int cleaningMinutes) implements Space {
    public Suite {
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        if (dailyRate < 0 || cleaningMinutes < 0) {
            throw new IllegalArgumentException("Rate and cleaning time must be nonnegative");
        }
    }
    @Override
    public <R> R accept(Visitor<R> visitor) { return visitor.visit(this); }
}
