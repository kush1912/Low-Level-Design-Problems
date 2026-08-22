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
        // Validate outside Board and no change in Move
        if (!board.isValid(from) || !board.isValid(to) || from.equals(to)) {
            return false;
        }

        // Get Moving piece, it should exist to the player whose turn it is
        Piece movingPiece = board.getPiece(from);
        if (movingPiece == null || movingPiece.getColor() != color) {
            return false;
        }

        //Get Destination piece if that is King,
        Piece destinationPiece = board.getPiece(to);

        // A King is never captured; checkmate must end the game before a piece
        // can move onto the King's square. Otherwise, validate piece movement.
        if (destinationPiece instanceof King
                || !movingPiece.canMove(board, from, to)) {
            return false;
        }

        // Simulate the move to verify that it does not expose the moving King.
        Piece capturedPiece = board.movePiece(from, to);
        Move attemptedMove = new Move(from, to, movingPiece, capturedPiece);

        // Does our move expose our own King?
        boolean exposesOwnKing = checkDetector.isKingCheck(board, color);

        board.undoMove(attemptedMove);
        return !exposesOwnKing;
    }

    // Checks whether the given player has at least one completely legal move.
    public boolean hasAnyLegalMove(Board board, Color color) {

        // Treat every board square as a possible source row.
        for (int fromRow = 0; fromRow < Board.SIZE; fromRow++) {
            for (int fromColumn = 0; fromColumn < Board.SIZE; fromColumn++) {

                Position from = new Position(fromRow, fromColumn);
                Piece piece = board.getPiece(from);
                if (piece == null || piece.getColor() != color) {
                    continue;
                }

                // For this player-owned piece(source), try every board row as a possible destination.
                for (int toRow = 0; toRow < Board.SIZE; toRow++) {
                    for (int toColumn = 0; toColumn < Board.SIZE; toColumn++) {

                        Position to = new Position(toRow, toColumn);
                        // One legal response proves this is not checkmate or
                        // stalemate, so no further searching is necessary.
                        if (isLegalMove(board, color, from, to)) {
                            return true;
                        }
                    }
                }
            }
        }

        // Every destination for every player-owned piece was illegal.
        return false;
    }
}
