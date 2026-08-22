package lld.DesignProblems.chess.models.ConcretePiece;

import lld.DesignProblems.chess.enums.Color;
import lld.DesignProblems.chess.models.Board;
import lld.DesignProblems.chess.models.Piece;
import lld.DesignProblems.chess.models.Position;

public class King extends Piece {
    public King(Color color) { super(color); }

    @Override
    protected char getTypeSymbol() {
        return 'K';
    }

    @Override
    public boolean canMove(Board board, Position from, Position to) {
        int rowDifference = Math.abs(to.row() - from.row());
        int colDifference = Math.abs(to.column() - from.column());

        boolean movesStraight = from.row() == to.row() || from.column() == to.column();
        boolean moviesDiagonal = rowDifference == colDifference;

        if(!movesStraight && !moviesDiagonal) return false;

        if(rowDifference >1 || colDifference >1) return false;

        Piece destinationPiece = board.getPiece(to);
        return destinationPiece == null || destinationPiece.getColor() != getColor();
    }

}
