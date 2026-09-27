package lld.DesignProblems.battlefield.service;

public interface BattleFieldService {
    void initialiseGame(Integer n, String playerAName, String playerBName);

    void addShip(String Id, int size, int pos_xa, int pos_ya, int pos_xb, int pos_yb);

    void startGame();

    void fireMissile();

    void fireMissile(int x, int y);

    void viewBattleField();
}
