import javafx.geometry.Rectangle2D;

final class Door {
    private final Rectangle2D bounds;
    private final DoorType type;

    Door(double x, double y, double size, DoorType type) {
        this.bounds = new Rectangle2D(x, y, size, size);
        this.type = type;
    }

    Rectangle2D getBounds() {
        return bounds;
    }

    DoorType getType() {
        return type;
    }

    double centerX() {
        return bounds.getMinX() + bounds.getWidth() / 2.0;
    }

    double centerY() {
        return bounds.getMinY() + bounds.getHeight() / 2.0;
    }
}