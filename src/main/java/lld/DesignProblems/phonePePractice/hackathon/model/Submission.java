package lld.DesignProblems.phonePePractice.hackathon.model;

import lld.DesignProblems.phonePePractice.hackathon.utils.IdGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Submission {
    private String submissionId;
    private User user;
    private Problem problem;
    private Integer scoreAwarded;
    private Integer time;

    public Submission(User user, Problem problem, Integer scoreAwarded, Integer time) {
        this.submissionId = IdGenerator.id();
        this.user = user;
        this.problem = problem;
        this.scoreAwarded = scoreAwarded;
        this.time = time;
    }
}
