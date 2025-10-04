import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;

import java.util.EnumSet;
import java.util.Set;

final class GameView extends Pane {
    private final Canvas canvas;
    private final GraphicsContext graphicsContext;
    private final GameWorld world;
    private final Set<GameWorld.Input> activeInputs = EnumSet.noneOf(GameWorld.Input.class);

    private AnimationTimer timer;
    private long lastFrameNanos = 0;

    GameView() {
        this.canvas = new Canvas(960, 640);
        this.graphicsContext = canvas.getGraphicsContext2D();
        this.world = new GameWorld(canvas.getWidth(), canvas.getHeight());

        getChildren().add(canvas);
        setFocusTraversable(true);

        setOnMouseClicked(event -> requestFocus());
        setOnKeyPressed(event -> {
            GameWorld.Input input = mapKey(event.getCode());
            if (input != null) {
                activeInputs.add(input);
            }
        });
        setOnKeyReleased(event -> {
            GameWorld.Input input = mapKey(event.getCode());
            if (input != null) {
                activeInputs.remove(input);
            }
        });

        widthProperty().addListener((obs, oldV, newV) -> resizeWorld());
        heightProperty().addListener((obs, oldV, newV) -> resizeWorld());

        startLoop();
    }

    private void startLoop() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastFrameNanos == 0) {
                    lastFrameNanos = now;
                }
                double dt = (now - lastFrameNanos) / 1_000_000_000.0;
                lastFrameNanos = now;
                world.update(dt, activeInputs, now / 1_000_000_000.0);
                world.render(graphicsContext);
            }
        };
        timer.start();
    }

    private void resizeWorld() {
        double width = Math.max(400, getWidth());
        double height = Math.max(300, getHeight());
        canvas.setWidth(width);
        canvas.setHeight(height);
        world.resize(width, height);
    }

    private GameWorld.Input mapKey(KeyCode code) {
        return switch (code) {
            case W, UP -> GameWorld.Input.UP;
            case S, DOWN -> GameWorld.Input.DOWN;
            case A, LEFT -> GameWorld.Input.LEFT;
            case D, RIGHT -> GameWorld.Input.RIGHT;
            default -> null;
        };
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        canvas.setWidth(getWidth());
        canvas.setHeight(getHeight());
        world.resize(canvas.getWidth(), canvas.getHeight());
    }
}