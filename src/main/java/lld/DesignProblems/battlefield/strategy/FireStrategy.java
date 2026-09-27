package lld.DesignProblems.battlefield.strategy;

import lld.DesignProblems.battlefield.models.Coordinate;
import lld.DesignProblems.battlefield.models.Grid;

import java.util.Set;

public interface FireStrategy {

    Coordinate selectTarget(Grid grid, int targetTerritory, Set<Coordinate> firedCoordinates);
}
