package lld.DesignProblems.phonePePractice.hackathon.db;

import lld.DesignProblems.phonePePractice.hackathon.model.Problem;
import lld.DesignProblems.phonePePractice.hackathon.model.Submission;
import lld.DesignProblems.phonePePractice.hackathon.model.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;
@Getter
@Setter
@ToString
public class InMemoryDB {
    private Set<User> registeredUsers = new HashSet<>();
    private Set<Problem> problems = new HashSet<>();
    private Set<Submission> submissions = new HashSet<>();

    // userId -> Submission
    private Map<User, Set<Submission>> userSubmission = new HashMap<>();
    // problem -> Submission
    private Map<Problem, Set<Submission>> problemSubmissions = new HashMap<>();

    private Map<User, Set<Problem>> likedProblems = new HashMap<>();

}
