package lld.DesignProblems.battlefield.models;

import lld.DesignProblems.battlefield.enums.GameStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class BattleFieldGame {
    private final String gameId;
    private final Set<Ship> playerAShips = new HashSet<>();
    private final Set<Ship> playerBShips = new HashSet<>();
    private final Player playerA;
    private final Player playerB;
    private final Grid grid;
    private final Set<Coordinate> firedCoordinates = new HashSet<>();

    private GameStatus status;
    private Player winner;
    private Player currentPlayer;

    public BattleFieldGame(Player playerA, Player playerB, int size) {
        this.gameId = "G-" + UUID.randomUUID();
        this.status = GameStatus.INITIALISED;
        this.playerA = playerA;
        this.playerB = playerB;
        this.grid = new Grid(size);
    }

    public void start() {
        if (status != GameStatus.INITIALISED) {
            throw new IllegalStateException("Game cannot be started");
        }
        status = GameStatus.STARTED;
        currentPlayer = playerA;
    }

    public boolean registerMissile(Coordinate coordinate) {
        return firedCoordinates.add(coordinate);
    }

    public void switchTurn() {
        currentPlayer = currentPlayer.equals(playerA)
                ? playerB
                : playerA;
    }

    public void finish(Player winner) {
        this.winner = winner;
        this.status = GameStatus.FINISHED;
    }
}
