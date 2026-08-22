package lld.DesignProblems.chess.models;

import lld.DesignProblems.chess.enums.Color;

public class Player {
    private final String name;
    private final Color color;

    public Player(String name, Color color){
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

}

/*
    Player currently contains immutable name and color, but I expect it may later gain an ID, player type, rating, or participant-specific behavior.
    I will keep it as a class so its internal representation and constructors can evolve without changing every caller.
    I will still keep its current fields immutable.
*/