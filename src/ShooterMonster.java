import java.util.List;

final class ShooterMonster extends Monster {
    private static final double FIRE_COOLDOWN = 1.5;
    private static final double SKILL_DELAY = 1.0;
    private static final double PROJECTILE_SPEED = 3.2;

    private double lastFireTime = 0;
    private double delayStartedAt = -10;
    private boolean delaying = false;

    ShooterMonster(double x, double y) {
        super(x, y);
        this.bodyColor = javafx.scene.paint.Color.rgb(255, 120, 0, 0.95);
        this.shadowColor = javafx.scene.paint.Color.DARKORANGE;
        this.speed = 0.9;
    }

    @Override
    void update(GameWorld world, double dt) {
        Player player = world.getPlayer();
        double now = world.getCurrentTime();

        if (delaying) {
            if (now - delayStartedAt >= SKILL_DELAY) {
                delaying = false;
                lastFireTime = now;
                fireVolley(world.getProjectiles(), player);
            }
            return;
        }

        if (now - lastFireTime >= FIRE_COOLDOWN) {
            delaying = true;
            delayStartedAt = now;
            return;
        }

        chase(player.centerX(), player.centerY());
    }

    private void fireVolley(List<Projectile> projectiles, Player player) {
        double centerAngle = Math.atan2(player.centerY() - y, player.centerX() - x);
        double spread = Math.toRadians(20);
        double[] angles = {centerAngle - spread, centerAngle, centerAngle + spread};
        for (double angle : angles) {
            double vx = Math.cos(angle);
            double vy = Math.sin(angle);
            projectiles.add(new Projectile(x, y, vx, vy, PROJECTILE_SPEED));
        }
    }
}