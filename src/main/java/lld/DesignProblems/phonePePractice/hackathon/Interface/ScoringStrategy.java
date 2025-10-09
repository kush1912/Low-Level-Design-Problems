package lld.DesignProblems.phonePePractice.hackathon.Interface;

import lld.DesignProblems.phonePePractice.hackathon.model.Problem;

public interface ScoringStrategy {
    Integer computeScore(Problem problem, Integer timeTakenSeconds);
}
