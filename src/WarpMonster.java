import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

final class WarpMonster extends Monster {
    private static final double WARP_COOLDOWN = 5.0;
    private static final double TELEGRAPH_TIME = 0.6;
    private static final double SKILL_DELAY = 1.0;
    private static final double WARP_OFFSET = 140;

    private double lastWarpTime = -10;
    private double delayStartedAt = -10;
    private boolean delaying = false;
    private boolean telegraphing = false;
    private double telegraphStartedAt = -10;
    private double targetX;
    private double targetY;
    private double lastUpdateTime;

    WarpMonster(double x, double y) {
        super(x, y);
        this.bodyColor = Color.rgb(190, 0, 255, 0.95);
        this.shadowColor = Color.MAGENTA;
        this.speed = 1.1;
    }

    @Override
    void update(GameWorld world, double dt) {
        Player player = world.getPlayer();
        double now = world.getCurrentTime();
        lastUpdateTime = now;

        if (telegraphing) {
            if (now - telegraphStartedAt >= TELEGRAPH_TIME) {
                x = targetX;
                y = targetY;
                telegraphing = false;
                lastWarpTime = now;
            }
            return;
        }

        if (delaying) {
            if (now - delayStartedAt >= SKILL_DELAY) {
                delaying = false;
                prepareTelegraph(world, player, now);
            }
            return;
        }

        if (now - lastWarpTime >= WARP_COOLDOWN) {
            delaying = true;
            delayStartedAt = now;
            return;
        }

        chase(player.centerX(), player.centerY());
    }

    private void prepareTelegraph(GameWorld world, Player player, double now) {
        double px = player.centerX();
        double py = player.centerY();
        double tx = GameMath.clamp(px + (Math.random() * 2 - 1) * WARP_OFFSET, 40, world.getWidth() - 40);
        double ty = GameMath.clamp(py + (Math.random() * 2 - 1) * WARP_OFFSET, 40, world.getHeight() - 40);
        targetX = tx;
        targetY = ty;
        telegraphStartedAt = now;
        telegraphing = true;
    }

    @Override
    void draw(GraphicsContext gc) {
        super.draw(gc);
        if (telegraphing) {
            double elapsed = lastUpdateTime - telegraphStartedAt;
            double progress = Math.min(1.0, Math.max(0, elapsed / TELEGRAPH_TIME));
            double radius = 18 + 40 * progress;
            gc.setStroke(Color.rgb(190, 0, 255, 0.7));
            gc.setLineWidth(3);
            gc.setLineDashes(6, 8);
            gc.strokeOval(targetX - radius, targetY - radius, radius * 2, radius * 2);
            gc.setLineDashes();
        }
    }
}