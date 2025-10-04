import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

final class Projectile {
    private double x;
    private double y;
    private final double vx;
    private final double vy;
    private final double radius = 4;
    private final Color color = Color.rgb(255, 80, 80, 0.95);

    Projectile(double x, double y, double vx, double vy, double speed) {
        this.x = x;
        this.y = y;
        this.vx = vx * speed;
        this.vy = vy * speed;
    }

    void update() {
        x += vx;
        y += vy;
    }

    void draw(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    boolean hitsPlayer(Player player) {
        return GameMath.distance(x, y, player.centerX(), player.centerY()) < radius + player.size / 2.0;
    }

    boolean outOfBounds(double width, double height) {
        return x < -10 || x > width + 10 || y < -10 || y > height + 10;
    }
}