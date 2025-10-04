import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GameWorld {
    private static final int MAX_STAGE = 6;
    private static final double DOOR_SIZE = 30;
    private static final double RESPAWN_DELAY = 1.0;
    private static final double MESSAGE_DURATION = 2.5;

    private final Random random = new Random();
    private final Player player;
    private final List<Door> doors = new ArrayList<>();
    private final List<Monster> monsters = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();

    private double width;
    private double height;
    private int stage = 1;
    private double currentTime;
    private boolean dead = false;
    private double deathStartedAt = -10;
    private double messageUntil = -1;
    private String message = null;

    GameWorld(double width, double height) {
        this.width = Math.max(400, width);
        this.height = Math.max(300, height);
        double startX = this.width / 2.0 - 10;
        double startY = this.height / 2.0 - 10;
        this.player = new Player(startX, startY, 20, 8.0);
        spawnDoors();
    }

    double getWidth() {
        return width;
    }

    double getHeight() {
        return height;
    }

    Player getPlayer() {
        return player;
    }

    List<Projectile> getProjectiles() {
        return projectiles;
    }

    double getCurrentTime() {
        return currentTime;
    }

    void resize(double newWidth, double newHeight) {
        this.width = Math.max(400, newWidth);
        this.height = Math.max(300, newHeight);
        centerPlayer();
        spawnDoors();
    }

    private void centerPlayer() {
        player.x = width / 2.0 - player.size / 2.0;
        player.y = height / 2.0 - player.size / 2.0;
    }

    void update(double dt, Set<Input> inputs, double nowSeconds) {
        this.currentTime = nowSeconds;
        updatePlayerMovement(dt, inputs);
        updateMonsters(dt);
        updateProjectiles();
        handleDoorInteractions();
        handleRespawn();
    }

    private void updatePlayerMovement(double dt, Set<Input> inputs) {
        boolean stunned = currentTime < player.stunnedUntil;
        boolean canMove = !dead && !stunned;
        player.speed = canMove ? player.baseSpeed : 0;

        double dx = 0;
        double dy = 0;

        if (canMove) {
            if (inputs.contains(Input.UP)) {
                dy -= 1;
            }
            if (inputs.contains(Input.DOWN)) {
                dy += 1;
            }
            if (inputs.contains(Input.LEFT)) {
                dx -= 1;
            }
            if (inputs.contains(Input.RIGHT)) {
                dx += 1;
            }
        }

        if (dx != 0 || dy != 0) {
            double length = Math.hypot(dx, dy);
            dx = (dx / length) * player.speed;
            dy = (dy / length) * player.speed;
        }

        player.x = GameMath.clamp(player.x + dx, 0, width - player.size);
        player.y = GameMath.clamp(player.y + dy, 0, height - player.size);
    }

    private void updateMonsters(double dt) {
        for (Monster monster : monsters) {
            monster.update(this, dt);
            if (!dead && monster.collidesWithPlayer(player)) {
                triggerDeath();
            }
        }
    }

    private void updateProjectiles() {
        boolean hit = false;
        for (Projectile projectile : projectiles) {
            projectile.update();
            if (!dead && projectile.hitsPlayer(player)) {
                hit = true;
            }
        }
        if (hit) {
            triggerDeath();
        }
        projectiles.removeIf(p -> p.outOfBounds(width, height));
    }

    private void handleDoorInteractions() {
        if (dead) {
            return;
        }

        for (Door door : doors) {
            if (intersects(player, door.getBounds())) {
                switch (door.getType()) {
                    case PASS -> advanceStage();
                    case BACK -> regressStage();
                    case NORMAL -> {
                        // no-op
                    }
                }
                break;
            }
        }
    }

    private void handleRespawn() {
        if (!dead) {
            return;
        }
        if (currentTime - deathStartedAt >= RESPAWN_DELAY) {
            dead = false;
            stage = 1;
            projectiles.clear();
            message = "Respawned";
            messageUntil = currentTime + MESSAGE_DURATION;
            centerPlayer();
            spawnDoors();
        }
    }

    private void triggerDeath() {
        if (dead) {
            return;
        }
        dead = true;
        deathStartedAt = currentTime;
        projectiles.clear();
        message = "You Died";
        messageUntil = currentTime + MESSAGE_DURATION;
    }

    private void advanceStage() {
        if (stage < MAX_STAGE) {
            stage++;
            message = "Stage " + stage;
            messageUntil = currentTime + MESSAGE_DURATION;
            centerPlayer();
            spawnDoors();
        } else {
            message = "You Win!";
            messageUntil = currentTime + MESSAGE_DURATION;
            stage = 1;
            projectiles.clear();
            centerPlayer();
            spawnDoors();
        }
    }

    private void regressStage() {
        if (stage > 1) {
            stage--;
            message = "Stage " + stage;
            messageUntil = currentTime + MESSAGE_DURATION;
        } else {
            message = "Stage 1";
            messageUntil = currentTime + MESSAGE_DURATION;
        }
        centerPlayer();
        projectiles.clear();
        spawnDoors();
    }

    private void spawnDoors() {
        doors.clear();
        monsters.clear();
        projectiles.clear();

        double minDist = DOOR_SIZE + 10;
        int attempts = 0;
        while (doors.size() < 6 && attempts < 2000) {
            double x = random.nextDouble() * (width - DOOR_SIZE);
            double y = random.nextDouble() * (height - DOOR_SIZE);
            boolean valid = true;
            for (Door door : doors) {
                double dx = (x + DOOR_SIZE / 2.0) - door.centerX();
                double dy = (y + DOOR_SIZE / 2.0) - door.centerY();
                if (Math.hypot(dx, dy) < minDist) {
                    valid = false;
                    break;
                }
            }
            attempts++;
            if (!valid) {
                continue;
            }

            DoorType type;
            if (doors.isEmpty()) {
                type = DoorType.PASS;
            } else if (doors.size() == 1) {
                type = DoorType.BACK;
            } else {
                type = DoorType.NORMAL;
            }
            doors.add(new Door(x, y, DOOR_SIZE, type));
        }

        monsters.addAll(spawnMonsters());
    }

    private List<Monster> spawnMonsters() {
        double[][] corners = {
                {50, 50},
                {width - 50, 50},
                {50, height - 50},
                {width - 50, height - 50}
        };
        double[] corner = corners[random.nextInt(corners.length)];
        double mx = corner[0];
        double my = corner[1];

        return switch (stage) {
            case 2, 4 -> List.of(new StunMonster(mx, my));
            case 1, 5 -> List.of(new WarpMonster(mx, my));
            case 3, 6 -> List.of(new ShooterMonster(mx, my));
            default -> List.of(new StunMonster(mx, my));
        };
    }

    void render(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, width, height);

        drawBackground(gc);
        drawPlayer(gc);
        drawDoors(gc);
        drawMonsters(gc);
        drawProjectiles(gc);
        drawUi(gc);
    }

    private void drawBackground(GraphicsContext gc) {
        gc.setFill(Color.rgb(30, 30, 35));
        gc.fillRect(0, 0, width, height);
    }

    private void drawPlayer(GraphicsContext gc) {
        boolean stunned = currentTime < player.stunnedUntil;
        gc.setFill(stunned ? Color.rgb(220, 220, 255, 0.7) : Color.WHITESMOKE);
        gc.fillRect(player.x, player.y, player.size, player.size);
    }

    private void drawDoors(GraphicsContext gc) {
        double radius = 150;
        double px = player.centerX();
        double py = player.centerY();
        for (Door door : doors) {
            double dist = GameMath.distance(px, py, door.centerX(), door.centerY());
            if (dist > radius) {
                continue;
            }
            switch (door.getType()) {
                case PASS -> gc.setFill(Color.LIMEGREEN);
                case BACK -> gc.setFill(Color.SALMON);
                case NORMAL -> gc.setFill(Color.DIMGRAY);
            }
            RectangleRenderer.fill(gc, door.getBounds());
        }
    }

    private void drawMonsters(GraphicsContext gc) {
        for (Monster monster : monsters) {
            monster.draw(gc);
        }
    }

    private void drawProjectiles(GraphicsContext gc) {
        for (Projectile projectile : projectiles) {
            projectile.draw(gc);
        }
    }

    private void drawUi(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.fillText("Stage: " + stage, 10, 20);
        if (currentTime < player.stunnedUntil) {
            gc.fillText("Stunned", 10, 40);
        }
        if (dead) {
            gc.fillText("You Died", 10, 60);
        }
        if (message != null && currentTime < messageUntil) {
            gc.fillText(message, 10, 80);
        }
    }

    private static boolean intersects(Player player, javafx.geometry.Rectangle2D rect) {
        return player.x < rect.getMaxX()
                && player.x + player.size > rect.getMinX()
                && player.y < rect.getMaxY()
                && player.y + player.size > rect.getMinY();
    }

    public enum Input {
        UP, DOWN, LEFT, RIGHT
    }
}