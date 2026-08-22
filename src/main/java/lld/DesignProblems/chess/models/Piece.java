package lld.DesignProblems.chess.models;

import lld.DesignProblems.chess.enums.Color;

public abstract class Piece {
    private final Color color;

    public Piece(Color color){
        this.color = color;
    }

    // LegalMoveValidator guarantees that from and to are valid and distinct.
    public abstract boolean canMove( Board board, Position from, Position to);

    protected abstract char getTypeSymbol();

    public final String getSymbol() {
        String colorSymbol = color == Color.WHITE ? "W" : "B";
        return colorSymbol + getTypeSymbol();
    }

    public Color getColor() {
        return color;
    }
}
