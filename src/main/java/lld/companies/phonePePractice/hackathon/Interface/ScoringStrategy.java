package lld.companies.phonePePractice.hackathon.Interface;

import lld.companies.phonePePractice.hackathon.model.Problem;

public interface ScoringStrategy {
    Integer computeScore(Problem problem, Integer timeTakenSeconds);
}
