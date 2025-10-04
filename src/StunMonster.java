import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

final class StunMonster extends Monster {
    private static final double PULSE_COOLDOWN = 2.5;
    private static final double PULSE_DURATION = 0.5;
    private static final double PULSE_RADIUS = 150;
    private static final double STUN_DURATION = 0.9;
    private static final double SKILL_DELAY = 1.0;

    private double lastPulse = -10;
    private double lastDelayStart = -10;
    private boolean delaying = false;
    private double lastUpdateTime = 0;

    StunMonster(double x, double y) {
        super(x, y);
        this.bodyColor = Color.CYAN;
        this.shadowColor = Color.DARKCYAN;
        this.speed = 1.0;
    }

    @Override
    void update(GameWorld world, double dt) {
        Player player = world.getPlayer();
        double now = world.getCurrentTime();
        lastUpdateTime = now;

        if (delaying) {
            if (now - lastDelayStart >= SKILL_DELAY) {
                delaying = false;
                lastPulse = now;
            }
            return;
        }

        if (now - lastPulse >= PULSE_COOLDOWN) {
            delaying = true;
            lastDelayStart = now;
            return;
        }

        chase(player.centerX(), player.centerY());

        if (now - lastPulse <= PULSE_DURATION) {
            double dist = GameMath.distance(x, y, player.centerX(), player.centerY());
            if (dist <= PULSE_RADIUS) {
                player.stunnedUntil = Math.max(player.stunnedUntil, now + STUN_DURATION);
            }
        }
    }

    @Override
    void draw(GraphicsContext gc) {
        super.draw(gc);
        double elapsed = lastUpdateTime - lastPulse;
        if (elapsed >= 0 && elapsed <= PULSE_DURATION) {
            double progress = Math.min(1.0, elapsed / PULSE_DURATION);
            double radius = PULSE_RADIUS * (0.5 + 0.5 * progress);
            gc.setStroke(Color.CYAN);
            gc.setLineWidth(2);
            gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
        }
    }
}