final class Player {
    double x;
    double y;
    final double size;
    final double baseSpeed;
    double speed;
    double stunnedUntil;

    Player(double x, double y, double size, double speed) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.baseSpeed = speed;
        this.speed = speed;
        this.stunnedUntil = 0;
    }

    double centerX() {
        return x + size / 2.0;
    }

    double centerY() {
        return y + size / 2.0;
    }
}