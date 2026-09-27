package lld.DesignProblems.battlefield.models;

public class Grid {
    private final int size;
    private final Cell[][] grid;

    public Grid(int n){
        if(n <=0 || n%2!=0){
            throw new IllegalArgumentException("BattleField needs to be divided into 2 Equal Parts, Not Possible!");
        }

        this.size = n;
        this.grid = new Cell[n][n];

        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                int territory = y < n / 2 ? 1 : 2;
                grid[x][y] = new Cell(x, y, territory);
            }
        }
    }

    public void print() {
        for (int y = size - 1; y >= 0; y--) {
            if (y == size / 2 - 1) {
                System.out.println("=".repeat(size * 9 + 1));
            }

            for (int x = 0; x < size; x++) {
                Ship ship = grid[x][y].getShip();
                String value = ship == null ? "" : ship.getShipId();
                System.out.printf("| %-6s ", value);
            }
            System.out.println("|");
        }
    }

    public Cell getCell(int x, int y) {
        if (x < 0 || x >= size || y < 0 || y >= size) {
            throw new IllegalArgumentException(
                    "Coordinate is outside the battlefield");
        }

        return grid[x][y];
    }

    public int getSize() {
        return size;
    }
}
