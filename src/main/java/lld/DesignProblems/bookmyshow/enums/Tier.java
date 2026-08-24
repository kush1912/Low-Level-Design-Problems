package lld.DesignProblems.bookmyshow.enums;

public enum Tier {
    DIAMOND, GOLDEN, SILVER;

    public double getPrice() {
        return switch (this) {
            case DIAMOND -> 500;
            case GOLDEN -> 300;
            case SILVER -> 100;
        };
    }
}

