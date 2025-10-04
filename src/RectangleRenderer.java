import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

final class RectangleRenderer {
    private RectangleRenderer() {
    }

    static void fill(GraphicsContext gc, Rectangle2D rect) {
        gc.fillRect(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight());
    }
}