package lld.DesignProblems.phonePePractice.hackathon.services;

import lld.DesignProblems.phonePePractice.hackathon.Interface.ScoringStrategy;
import lld.DesignProblems.phonePePractice.hackathon.model.Problem;

public class DefaultScoring implements ScoringStrategy {
    @Override
    public Integer computeScore(Problem problem, Integer timeTakenSeconds) {
        return problem.getScore();
    }
}
