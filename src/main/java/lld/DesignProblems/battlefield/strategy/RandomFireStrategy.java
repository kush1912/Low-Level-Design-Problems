package lld.DesignProblems.battlefield.strategy;

import lld.DesignProblems.battlefield.models.Coordinate;
import lld.DesignProblems.battlefield.models.Grid;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.random.RandomGenerator;

public class RandomFireStrategy implements FireStrategy {

    private final RandomGenerator random;

    public RandomFireStrategy() {
        this(new Random());
    }

    public RandomFireStrategy(RandomGenerator random) {
        this.random = Objects.requireNonNull(random);
    }

    @Override
    public Coordinate selectTarget(Grid grid, int targetTerritory, Set<Coordinate> firedCoordinates) {
        List<Coordinate> availableTargets = new ArrayList<>();

        for (int x = 0; x < grid.getSize(); x++) {
            for (int y = 0; y < grid.getSize(); y++) {
                Coordinate coordinate = new Coordinate(x, y);

                if (grid.getCell(x, y).getTerritory() == targetTerritory
                        && !firedCoordinates.contains(coordinate)) {
                    availableTargets.add(coordinate);
                }
            }
        }

        if (availableTargets.isEmpty()) {
            throw new IllegalStateException("No unused coordinates remain in the opponent's territory");
        }

        return availableTargets.get(random.nextInt(availableTargets.size()));
    }
}
