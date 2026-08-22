package lld.DesignProblems.chess.models.ConcretePiece;

import lld.DesignProblems.chess.enums.Color;
import lld.DesignProblems.chess.models.Board;
import lld.DesignProblems.chess.models.Piece;
import lld.DesignProblems.chess.models.Position;

public class Rook extends Piece {

    public Rook(Color color){
        super(color);
    }

    @Override
    protected char getTypeSymbol() {
        return 'R';
    }

    @Override
    public boolean canMove(Board board, Position from, Position to) {
        boolean movesStraight = from.row() == to.row() || from.column() == to.column();

        if (!movesStraight) {
            return false;
        }

        //Get Directions
        int rowStep = Integer.compare(to.row(), from.row()); // -1, 1, 0;
        int columnStep = Integer.compare(to.column(), from.column());

        int currentRow = from.row() + rowStep;
        int currentColumn = from.column() + columnStep;

        // Check every square before the destination.
        while (currentRow != to.row() || currentColumn != to.column()) {
            Position current = new Position(currentRow, currentColumn);

            if (board.getPiece(current) != null) {
                return false;
            }
            currentRow += rowStep;
            currentColumn += columnStep;
        }
        Piece destinationPiece = board.getPiece(to);

        // An empty destination or an opponent piece is allowed.
        return destinationPiece == null
                || destinationPiece.getColor() != getColor();
    }


}
