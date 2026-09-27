package lld.DesignProblems.battlefield.service;

import lld.DesignProblems.battlefield.enums.GameStatus;
import lld.DesignProblems.battlefield.models.*;
import lld.DesignProblems.battlefield.strategy.FireStrategy;

import java.util.Set;

public class BattleFieldServiceImpl implements BattleFieldService {
    private final FireStrategy fireStrategy;
    private BattleFieldGame game;

    public BattleFieldServiceImpl(FireStrategy fireStrategy) {
        this.fireStrategy = fireStrategy;
    }

    @Override
    public void initialiseGame(Integer size, String playerAName, String playerBName) {
        if (game != null) {
            throw new IllegalStateException("Game is already initialized");
        }
        Player playerA = new Player(playerAName);
        Player playerB = new Player(playerBName);
        this.game = new BattleFieldGame(playerA, playerB, size);
        System.out.println("The Game has been initialized with Game ID: " + game.getGameId());
        viewBattleField();
    }

    @Override
    public void addShip(String id, int size, int posXa, int posYa, int posXb, int posYb) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Ship ID is required");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Ship size must be positive");
        }

        BattleFieldGame game = getGame();
        String shipIdA = "A-" + id;
        String shipIdB = "B-" + id;

        boolean duplicateId = game.getPlayerAShips().stream()
                .anyMatch(ship -> ship.getShipId().equals(shipIdA));
        if (duplicateId) {
            throw new IllegalArgumentException(
                    "Ship already exists with ID: " + id);
        }

        int startXa = posXa - size / 2;
        int endXa = startXa + size - 1;
        int startYa = posYa - size / 2;
        int endYa = startYa + size - 1;

        int startXb = posXb - size / 2;
        int endXb = startXb + size - 1;
        int startYb = posYb - size / 2;
        int endYb = startYb + size - 1;

        Grid grid = game.getGrid();

        validatePlacement(grid, startXa, endXa, startYa, endYa, 1, shipIdA);
        validatePlacement(grid, startXb, endXb, startYb, endYb, 2, shipIdB);

        Ship shipA = new Ship(shipIdA, startXa, endXa, startYa, endYa);
        Ship shipB = new Ship(shipIdB, startXb, endXb, startYb, endYb);

        placeShip(grid, shipA);
        placeShip(grid, shipB);

        game.getPlayerAShips().add(shipA);
        game.getPlayerBShips().add(shipB);
    }

    @Override
    public void startGame() {
        BattleFieldGame game = getGame();

        if (game.getPlayerAShips().isEmpty() || game.getPlayerBShips().isEmpty()) {
            throw new IllegalStateException("Both players must have at least one ship");
        }

        game.start();
        System.out.println(
                "Game started. " + game.getCurrentPlayer().Name()
                        + " will fire first.");

        while (game.getStatus() == GameStatus.STARTED) {
            fireMissile();
        }
    }

    @Override
    public void fireMissile() {
        BattleFieldGame game = getRunningGame();
        int defenderTerritory = game.getCurrentPlayer().equals(game.getPlayerA())
                ? 2
                : 1;

        Coordinate target = fireStrategy.selectTarget(
                game.getGrid(),
                defenderTerritory,
                Set.copyOf(game.getFiredCoordinates()));

        fireMissile(target.x(), target.y());
    }

    @Override
    public void fireMissile(int x, int y) {
        BattleFieldGame game = getRunningGame();

        Cell targetCell = game.getGrid().getCell(x, y);
        Player attacker = game.getCurrentPlayer();
        Player defender;
        Set<Ship> defenderShips;
        int defenderTerritory;

        if (attacker.equals(game.getPlayerA())) {
            defender = game.getPlayerB();
            defenderShips = game.getPlayerBShips();
            defenderTerritory = 2;
        } else {
            defender = game.getPlayerA();
            defenderShips = game.getPlayerAShips();
            defenderTerritory = 1;
        }

        if (targetCell.getTerritory() != defenderTerritory) {
            throw new IllegalArgumentException(attacker.Name() + " must fire inside " + defender.Name() + "'s territory");
        }

        Coordinate target = new Coordinate(x, y);
        if (!game.registerMissile(target)) {
            throw new IllegalArgumentException("A missile has already been fired at (" + x + ", " + y + ")");
        }

        Ship ship = targetCell.getShip();
        if (ship == null) {
            System.out.println(attacker.Name() + " fired at (" + x + ", " + y + "): Miss");
        } else {
            clearShipFromGrid(game.getGrid(), ship);
            defenderShips.remove(ship);
            System.out.println(attacker.Name() + " fired at (" + x + ", " + y + "): Hit. " + ship.getShipId() + " destroyed.");
        }

        if (defenderShips.isEmpty()) {
            game.finish(attacker);
            System.out.println(attacker.Name() + " wins the game.");
            return;
        }

        game.switchTurn();
    }

    @Override
    public void viewBattleField() {
        getGame().getGrid().print();
    }

    private BattleFieldGame getGame() {
        if (game == null) {
            throw new IllegalStateException("Initialize the game first");
        }
        return game;
    }

    private BattleFieldGame getRunningGame() {
        BattleFieldGame game = getGame();
        if (game.getStatus() != GameStatus.STARTED) {
            throw new IllegalStateException("Game is not currently running");
        }
        return game;
    }

    private void validatePlacement(Grid grid, int startX, int endX, int startY, int endY, int expectedTerritory, String shipId) {
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                Cell cell = grid.getCell(x, y);

                if (cell.getTerritory() != expectedTerritory) {
                    throw new IllegalArgumentException(
                            shipId + " must stay inside its player's territory");
                }
                if (cell.getShip() != null) {
                    throw new IllegalArgumentException(
                            shipId + " overlaps ship "
                                    + cell.getShip().getShipId());
                }
            }
        }
    }

    private void placeShip(Grid grid, Ship ship) {
        for (int x = ship.getStart_x(); x <= ship.getEnd_x(); x++) {
            for (int y = ship.getStart_y(); y <= ship.getEnd_y(); y++) {
                grid.getCell(x, y).setShip(ship);
            }
        }
    }

    private void clearShipFromGrid(Grid grid, Ship ship) {
        for (int x = ship.getStart_x(); x <= ship.getEnd_x(); x++) {
            for (int y = ship.getStart_y(); y <= ship.getEnd_y(); y++) {
                grid.getCell(x, y).setShip(null);
            }
        }
    }
}
