package lld.DesignProblems.chess;

import lld.DesignProblems.chess.enums.Color;
import lld.DesignProblems.chess.models.Board;
import lld.DesignProblems.chess.models.Game;
import lld.DesignProblems.chess.models.Player;
import lld.DesignProblems.chess.models.Position;
import lld.DesignProblems.chess.rules.CheckDetector;
import lld.DesignProblems.chess.rules.LegalMoveValidator;

public final class ChessDriverClass {

    private ChessDriverClass() {
    }

    public static void main(String[] args) {
        Player whitePlayer = new Player("Ajay", Color.WHITE);
        Player blackPlayer = new Player("Ritik", Color.BLACK);
        CheckDetector checkDetector = new CheckDetector();
        LegalMoveValidator legalMoveValidator =
                new LegalMoveValidator(checkDetector);

        Game game = new Game(
                Board.standardBoard(),
                whitePlayer,
                blackPlayer,
                checkDetector,
                legalMoveValidator
        );

        System.out.println("\nFool's Mate demonstration");

        playMove(
                game,
                "1. White moves f2 to f3",
                new Position(6, 5),
                new Position(5, 5)
        );
        playMove(
                game,
                "1... Black moves e7 to e5",
                new Position(1, 4),
                new Position(3, 4)
        );
        playMove(
                game,
                "2. White moves g2 to g4",
                new Position(6, 6),
                new Position(4, 6)
        );
        playMove(
                game,
                "2... Black Queen moves d8 to h4",
                new Position(0, 3),
                new Position(4, 7)
        );

        System.out.println("\nFinal status: " + game.getStatus());
        game.getWinner().ifPresent(
                winner -> System.out.println(
                        "Winner: " + winner.getName()
                )
        );

        playMove(
                game,
                "Attempting a move after the game has ended",
                new Position(6, 0),
                new Position(5, 0)
        );
    }

    private static void playMove(
            Game game,
            String description,
            Position from,
            Position to
    ) {
        System.out.println("\n" + description);
        boolean successful = game.move(from, to);

        if (!successful) {
            System.out.println("Invalid move: " + from + " -> " + to);
        }
    }
}
