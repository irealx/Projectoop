import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

abstract class Monster {
    protected double x;
    protected double y;
    protected final double radius = 18;
    protected double speed = 0.7;
    protected double lightRadius = 180;
    protected Color bodyColor = Color.rgb(255, 0, 0, 0.9);
    protected Color shadowColor = Color.RED;

    protected Monster(double x, double y) {
        this.x = x;
        this.y = y;
    }

    protected void chase(double targetX, double targetY) {
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.hypot(dx, dy);
        if (distance < 1e-6) {
            return;
        }
        x += (dx / distance) * speed;
        y += (dy / distance) * speed;
    }

    abstract void update(GameWorld world, double dt);

    void draw(GraphicsContext gc) {
        gc.setFill(bodyColor);
        gc.setStroke(shadowColor);
        gc.setLineWidth(2);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    boolean collidesWithPlayer(Player player) {
        return GameMath.distance(x, y, player.centerX(), player.centerY()) < radius + player.size / 2.0;
    }

    double getX() {
        return x;
    }

    double getY() {
        return y;
    }

    double getLightRadius() {
        return lightRadius;
    }
}