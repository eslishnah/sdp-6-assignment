package hotel.visitor;
import hotel.model.*;
/** Stateful operations use one visitor per traversal. */
public abstract class WalkingVisitor<R> implements Visitor<R> {
    protected abstract void inspect(Space space);
    protected abstract R result();
    @Override
    public R visit(Hotel hotel) {
        for (Floor floor : hotel.children()) { floor.accept(this); }
        return result();
    }
    @Override
    public R visit(Floor floor) {
        for (Space space : floor.children()) { space.accept(this); }
        return result();
    }
    @Override public R visit(StandardRoom room) { inspect(room); return result(); }
    @Override public R visit(Suite suite) { inspect(suite); return result(); }
    @Override public R visit(ConferenceHall hall) { inspect(hall); return result(); }
    @Override public R visit(Restaurant restaurant) { inspect(restaurant); return result(); }
}
