public enum DoorType {
    PASS,
    BACK,
    NORMAL;

    public String label() {
        return switch (this) {
            case PASS -> "Pass";
            case BACK -> "Back";
            case NORMAL -> "Normal";
        };
    }
}