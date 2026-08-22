package lld.DesignProblems.chess.models.ConcretePiece;

import lld.DesignProblems.chess.enums.Color;
import lld.DesignProblems.chess.models.Board;
import lld.DesignProblems.chess.models.Piece;
import lld.DesignProblems.chess.models.Position;

public class Queen extends Piece {

    public Queen(Color color) { super(color); }

    @Override
    protected char getTypeSymbol() {
        return 'Q';
    }

    @Override
    public boolean canMove(Board board, Position from, Position to) {
        int rowDifference = Math.abs(to.row() - from.row());
        int columnDifference = Math.abs(to.column() - from.column());

        boolean movesStraight =
                from.row() == to.row()
                        || from.column() == to.column();

        boolean movesDiagonally =
                rowDifference == columnDifference;

        if (!movesStraight && !movesDiagonally) {
            return false;
        }

        int rowStep =  Integer.compare(to.row(), from.row());
        int columnStep =  Integer.compare(to.column(), from.column());

        int currentRow = from.row() + rowStep;
        int currentColumn = from.column() + columnStep;

        while(currentRow != to.row() || currentColumn != to.column()) {
            Position currentPosition = new Position(currentRow, currentColumn);
            if(board.getPiece(currentPosition) != null) {
                return false;
            }
            currentRow += rowStep;
            currentColumn += columnStep;
        }
        Piece destinationPiece = board.getPiece(to);

        return destinationPiece == null || destinationPiece.getColor()!=getColor();
    }
}
