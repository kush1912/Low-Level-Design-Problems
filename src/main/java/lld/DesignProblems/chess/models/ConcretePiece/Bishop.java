package lld.DesignProblems.chess.models.ConcretePiece;

import lld.DesignProblems.chess.enums.Color;
import lld.DesignProblems.chess.models.Board;
import lld.DesignProblems.chess.models.Piece;
import lld.DesignProblems.chess.models.Position;

public class Bishop extends Piece {

    public Bishop(Color color) {
        super(color);
    }

    @Override
    protected char getTypeSymbol() {
        return 'B';
    }

    @Override
    public boolean canMove(Board board, Position from, Position to) {
        int rowDifference = Math.abs(to.row() - from.row());
        int columnDifference = Math.abs(to.column() - from.column());

        boolean movesDiagonally = rowDifference == columnDifference;

        if (!movesDiagonally) {
            return false;
        }

        //Get Directions
        int rowStep = Integer.compare(to.row(), from.row());
        int columnStep = Integer.compare(to.column(), from.column());

        int currentRow = from.row() + rowStep;
        int currentColumn = from.column() + columnStep;

        while (currentRow != to.row()) {
            Position current = new Position(currentRow, currentColumn);

            if (board.getPiece(current) != null) {
                return false;
            }

            currentRow += rowStep;
            currentColumn += columnStep;
        }

        Piece destinationPiece = board.getPiece(to);
        return destinationPiece == null
                || destinationPiece.getColor() != getColor();
    }
}
