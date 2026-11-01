package hotel.visitor;
import hotel.model.*;
public interface Visitor<R> {
    R visit(Hotel hotel);
    R visit(Floor floor);
    R visit(StandardRoom room);
    R visit(Suite suite);
    R visit(ConferenceHall hall);
    R visit(Restaurant restaurant);
    R visit(Spa spa);
}
