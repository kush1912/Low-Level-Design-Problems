package lld.DesignProblems.bookmyshow.model;

import lld.DesignProblems.bookmyshow.enums.Tier;

public record Seat(String row, int column, Tier tier) { }
