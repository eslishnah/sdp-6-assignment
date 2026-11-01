package hotel.model;
import hotel.visitor.Visitor;
public interface HotelElement {
    String id();
    String name();
    <R> R accept(Visitor<R> visitor);
}
