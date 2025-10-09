package lld.DesignProblems.phonePePractice.hackathon.model;

import lld.DesignProblems.phonePePractice.hackathon.enums.Difficulty;
import lld.DesignProblems.phonePePractice.hackathon.utils.IdGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;

@Getter
@Setter
@ToString
public class Problem {
    private String problemId;
    private String title;
    private String description;
    private Set<String> tags = new HashSet<>();
    private Difficulty difficulty;
    private Integer score;

    private Integer likes;


    public Problem(String title, String description, Set<String> tags, Difficulty difficulty, Integer score) {
        this.problemId = IdGenerator.id();
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.difficulty = difficulty;
        this.score = score;
        this.likes=0;
    }
}
