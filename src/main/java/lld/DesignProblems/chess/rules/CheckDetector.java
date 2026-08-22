package lld.DesignProblems.chess.rules;

import lld.DesignProblems.chess.enums.Color;
import lld.DesignProblems.chess.models.Board;
import lld.DesignProblems.chess.models.Piece;
import lld.DesignProblems.chess.models.Position;
import lld.DesignProblems.chess.models.ConcretePiece.King;

public class CheckDetector {
    public boolean isKingCheck(Board board, Color color) {
        Position kingPosition = findKing(board, color);

        for (int row = 0; row < Board.SIZE; row++) {
            for (int column = 0; column < Board.SIZE; column++) {
                Position position = new Position(row, column);
                Piece piece = board.getPiece(position);

                if (piece != null && piece.getColor() != color && piece.canMove(board, position, kingPosition)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Position findKing(Board board, Color color) {
        for (int row = 0; row < Board.SIZE; row++) {
            for (int column = 0; column < Board.SIZE; column++) {
                Position position = new Position(row, column);
                Piece piece = board.getPiece(position);

                if (piece instanceof King && piece.getColor() == color) {
                    return position;
                }
            }
        }

        throw new IllegalStateException(color + " King is missing");
    }
}
