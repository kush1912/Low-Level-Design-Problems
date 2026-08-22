package lld.DesignProblems.chess.rules;

import lld.DesignProblems.chess.enums.Color;
import lld.DesignProblems.chess.models.Board;
import lld.DesignProblems.chess.models.Move;
import lld.DesignProblems.chess.models.Piece;
import lld.DesignProblems.chess.models.Position;
import lld.DesignProblems.chess.models.ConcretePiece.King;

/*
*
1. Can this piece move there?
2. What would the board look like afterward?
3. Is my King safe on that resulting board?
* */
public class LegalMoveValidator {
    private final CheckDetector checkDetector;

    public LegalMoveValidator(CheckDetector checkDetector) {
        this.checkDetector = checkDetector;
    }

    public boolean isLegalMove(Board board, Color color, Position from, Position to) {
        if (!board.isValid(from) || !board.isValid(to) || from.equals(to)) {
            return false;
        }

        Piece movingPiece = board.getPiece(from);
        if (movingPiece == null || movingPiece.getColor() != color) {
            return false;
        }

        Piece destinationPiece = board.getPiece(to);
        if (destinationPiece instanceof King
                || !movingPiece.canMove(board, from, to)) {
            return false;
        }

        // Simulate the move to verify that it does not expose the moving King.
        Piece capturedPiece = board.movePiece(from, to);
        Move attemptedMove =
                new Move(from, to, movingPiece, capturedPiece);

        boolean exposesOwnKing =
                checkDetector.isKingCheck(board, color);

        board.undoMove(attemptedMove);
        return !exposesOwnKing;
    }

    public boolean hasAnyLegalMove(Board board, Color color) {
        // One legal response is enough to prove that the position is not mate.
        for (int fromRow = 0; fromRow < Board.SIZE; fromRow++) {
            for (int fromColumn = 0;
                    fromColumn < Board.SIZE;
                    fromColumn++) {
                Position from =
                        new Position(fromRow, fromColumn);
                Piece piece = board.getPiece(from);

                if (piece == null || piece.getColor() != color) {
                    continue;
                }

                for (int toRow = 0; toRow < Board.SIZE; toRow++) {
                    for (int toColumn = 0;
                            toColumn < Board.SIZE;
                            toColumn++) {
                        Position to =
                                new Position(toRow, toColumn);

                        // isLegalMove also simulates King safety and restores the board.
                        if (isLegalMove(board, color, from, to)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
