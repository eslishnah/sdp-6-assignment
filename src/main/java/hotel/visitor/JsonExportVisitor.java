package hotel.visitor;
import hotel.model.*;
import java.util.List;
import java.util.stream.Collectors;
public class JsonExportVisitor implements Visitor<String> {
    private String container(String type, HotelElement element, List<? extends HotelElement> children) {
        String nested = children.stream().map(child -> child.accept(this)).collect(Collectors.joining(","));
        return "{" + fields(type, element) + ",\"children\":[" + nested + "]}";
    }
    private String leaf(String type, Space space) {
        return "{" + fields(type, space) + ",\"dailyRate\":" + space.dailyRate()
                + ",\"cleaningMinutes\":" + space.cleaningMinutes() + "}";
    }
    private String fields(String type, HotelElement element) {
        return "\"type\":" + quote(type) + ",\"id\":" + quote(element.id()) + ",\"name\":" + quote(element.name());
    }
    private String quote(String text) {
        StringBuilder escaped = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (c < 32 || Character.isSurrogate(c)) { escaped.append(String.format("\\u%04x", (int) c)); }
                    else { escaped.append(c); }
                }
            }
        }
        return escaped.append('"').toString();
    }
    @Override public String visit(Hotel hotel) { return container("hotel", hotel, hotel.children()); }
    @Override public String visit(Floor floor) { return container("floor", floor, floor.children()); }
    @Override public String visit(StandardRoom room) { return leaf("standardRoom", room); }
    @Override public String visit(Suite suite) { return leaf("suite", suite); }
    @Override public String visit(ConferenceHall hall) { return leaf("conferenceHall", hall); }
    @Override public String visit(Restaurant restaurant) { return leaf("restaurant", restaurant); }
}
