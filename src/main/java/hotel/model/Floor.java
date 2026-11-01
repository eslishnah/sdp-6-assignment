package hotel.model;
import hotel.visitor.Visitor;
import java.util.List;
import java.util.Objects;
public record Floor(String id, String name, List<Space> children) implements HotelElement {
    public Floor {
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        children = List.copyOf(children);
    }
    @Override
    public <R> R accept(Visitor<R> visitor) { return visitor.visit(this); }
}
