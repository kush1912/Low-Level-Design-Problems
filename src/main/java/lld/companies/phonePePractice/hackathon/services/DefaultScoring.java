package lld.companies.phonePePractice.hackathon.services;

import lld.companies.phonePePractice.hackathon.Interface.ScoringStrategy;
import lld.companies.phonePePractice.hackathon.model.Problem;

public class DefaultScoring implements ScoringStrategy {
    @Override
    public Integer computeScore(Problem problem, Integer timeTakenSeconds) {
        return problem.getScore();
    }
}
