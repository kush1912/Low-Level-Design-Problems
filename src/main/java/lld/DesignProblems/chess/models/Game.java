package lld.DesignProblems.chess.models;

import lld.DesignProblems.chess.enums.Color;
import lld.DesignProblems.chess.enums.GameStatus;
import lld.DesignProblems.chess.rules.CheckDetector;
import lld.DesignProblems.chess.rules.LegalMoveValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Game {
    private final Board board;
    private final Player whitePlayer;
    private final Player blackPlayer;
    private final CheckDetector checkDetector;
    private final LegalMoveValidator legalMoveValidator;

    private Color currentTurn;
    private GameStatus status;
    private Player winner;

    private final List<Move> moveHistory;

    public Game(
            Board board,
            Player whitePlayer,
            Player blackPlayer,
            CheckDetector checkDetector,
            LegalMoveValidator legalMoveValidator
    ) {
        this.board = board;
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
        this.checkDetector = checkDetector;
        this.legalMoveValidator = legalMoveValidator;
        this.currentTurn = Color.WHITE;
        this.status = GameStatus.ACTIVE;
        this.moveHistory = new ArrayList<>();
    }

    public boolean move(Position from, Position to) {

        //Game over? or It is a legal move?
        if (status.isTerminal() || !legalMoveValidator.isLegalMove(board, currentTurn, from, to)) {
            return false;
        }

        //Get moving and Captured piece and make Move
        Piece movingPiece = board.getPiece(from);
        Piece capturedPiece = board.movePiece(from, to);
        Move move = new Move(from, to, movingPiece, capturedPiece);

        //Get Current Player and its opposite color
        Player movingPlayer = getCurrentPlayer();
        Color opponentColor = currentTurn.opposite();

        // After my move, does the other King comes in check status?
        boolean opponentInCheck = checkDetector.isKingCheck(board, opponentColor);

        // Opponent has any legal move that saves the King
        boolean opponentHasLegalMove = legalMoveValidator.hasAnyLegalMove(board, opponentColor);

        // Check and legal-response availability together determine the game state.
        updateStatus(movingPlayer, opponentInCheck, opponentHasLegalMove);

        moveHistory.add(move);

        System.out.println("Board after move " + from + " -> " + to + ":");
        board.printBoard();

        switchTurn();
        printStatusMessage(opponentInCheck);

        return true;
    }

    public GameStatus getStatus() {
        return status;
    }

    public Optional<Player> getWinner() {
        return Optional.ofNullable(winner);
    }

    private void switchTurn() {
        currentTurn = currentTurn.opposite();
    }

    private Player getCurrentPlayer() {
        return currentTurn == Color.WHITE
                ? whitePlayer
                : blackPlayer;
    }

    private void updateStatus(
            Player movingPlayer,
            boolean opponentInCheck,
            boolean opponentHasLegalMove
    ) {
        if (opponentInCheck && !opponentHasLegalMove) {
            status = GameStatus.CHECKMATE;
            winner = movingPlayer;
        } else if (!opponentInCheck && !opponentHasLegalMove) {
            // Stalemate occurs when the next player's King is not attacked,
            // but that player has no legal move with any piece. For example,
            // Black King on a8, White King on c6, and White Queen on b6:
            // a8 is safe, while every available Black destination is attacked.
            // The game ends as a draw because the King is not in check.
            status = GameStatus.STALEMATE;
            winner = null;
        } else if (opponentInCheck) {
            status = GameStatus.CHECK;
        } else {
            status = GameStatus.ACTIVE;
        }
    }

    private void printStatusMessage(boolean opponentInCheck) {
        if (status == GameStatus.CHECKMATE) {
            System.out.println(
                    "Checkmate! " + winner.getName() + " wins."
            );
        } else if (status == GameStatus.STALEMATE) {
            System.out.println("Stalemate! The game is a draw.");
        } else if (opponentInCheck) {
            System.out.println(
                    getCurrentPlayer().getName()
                            + ", your King is in check"
            );
        }
    }

}
