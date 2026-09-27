package lld.DesignProblems.battlefield;

import lld.DesignProblems.battlefield.service.BattleFieldService;
import lld.DesignProblems.battlefield.service.BattleFieldServiceImpl;
import lld.DesignProblems.battlefield.strategy.RandomFireStrategy;

public class BattleFieldDriverClass {

    public static void main(String[] args) {
        BattleFieldService service =
                new BattleFieldServiceImpl(new RandomFireStrategy());

        service.initialiseGame(8, "Player A", "Player B");

        service.addShip(
                "SH1",
                2,
                1, 1,
                1, 5);

        service.addShip(
                "SH2",
                1,
                5, 2,
                5, 6);

        System.out.println("\nBattlefield after placing ships:");
        service.viewBattleField();

        System.out.println("\nStarting game:");
        service.startGame();

        System.out.println("\nFinal battlefield:");
        service.viewBattleField();
    }
}
