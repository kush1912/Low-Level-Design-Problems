package lld.DesignProblems.chess.models.ConcretePiece;

import lld.DesignProblems.chess.enums.Color;
import lld.DesignProblems.chess.models.Board;
import lld.DesignProblems.chess.models.Piece;
import lld.DesignProblems.chess.models.Position;

public class Pawn extends Piece {

    public Pawn(Color color) { super(color); }

    @Override
    protected char getTypeSymbol() {
        return 'P';
    }

    @Override
    public boolean canMove(Board board, Position from, Position to) {
        int direction = getColor() == Color.WHITE ? -1 : 1;
        int startingRow = getColor() == Color.WHITE ? 6 : 1;

        int rowDifference = to.row() - from.row();
        int columnDifference = Math.abs(to.column() - from.column());
        Piece destinationPiece = board.getPiece(to);

        if (columnDifference == 0) {
            if (destinationPiece != null) {
                return false;
            }
            if (rowDifference == direction) {
                return true;
            }
            if (from.row() == startingRow && rowDifference == 2 * direction) {
                Position intermediatePosition = new Position(from.row() + direction, from.column());
                return board.getPiece(intermediatePosition) == null;
            }
            return false;
        }
        return columnDifference == 1 && rowDifference == direction && destinationPiece != null && destinationPiece.getColor() != getColor();
    }
}
