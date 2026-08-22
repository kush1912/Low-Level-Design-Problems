package lld.DesignProblems.chess.enums;

public enum GameStatus {
    ACTIVE(false),
    CHECK(false),
    CHECKMATE(true),
    STALEMATE(true),
    DRAW(true);

    private final boolean terminal;

    GameStatus(boolean terminal) {
        this.terminal = terminal;
    }

    public boolean isTerminal() {
        return terminal;
    }
}
