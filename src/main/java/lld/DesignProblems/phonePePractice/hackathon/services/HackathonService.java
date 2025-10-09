package lld.DesignProblems.phonePePractice.hackathon.services;

import lld.DesignProblems.phonePePractice.hackathon.Interface.ScoringStrategy;
import lld.DesignProblems.phonePePractice.hackathon.db.InMemoryDB;
import lld.DesignProblems.phonePePractice.hackathon.enums.Difficulty;
import lld.DesignProblems.phonePePractice.hackathon.model.Problem;
import lld.DesignProblems.phonePePractice.hackathon.model.Submission;
import lld.DesignProblems.phonePePractice.hackathon.model.User;
import lld.DesignProblems.phonePePractice.hackathon.utils.AppException;
import lombok.ToString;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ToString
public class HackathonService {
    private final InMemoryDB db;
    private ScoringStrategy scoring;

    public HackathonService(InMemoryDB db, ScoringStrategy scoring) {
        this.db = db;
        this.scoring = scoring;
    }


    //Add problems
    public void addProblem(Problem problem) {
        if(db.getProblems().contains(problem)) throw new AppException("Problem already exists: " + problem.getTitle());
        db.getProblems().add(problem);
        Set<Submission> submissions = new HashSet<>();
        db.getProblemSubmissions().put(problem, submissions);
        System.out.println("Problem added Success!" + problem.getTitle());
        return;
    }

    //Register user
    public void registerUser(User user){
        if(db.getRegisteredUsers().contains(user)) throw new AppException("User already registered!");
        db.getRegisteredUsers().add(user);
        Set<Submission> submissions = new HashSet<>();
        db.getUserSubmission().put(user, submissions);
        System.out.println("User registration Success!" + user.getName());
        return;
    }

    //Solve Problem
    @Transactional
    public void Solve(User user, Problem problem, Integer Time){
        int score = scoring.computeScore(problem, Time);
        Submission submission = new Submission(user,problem, score,Time);
        db.getSubmissions().add(submission);
        db.getUserSubmission().get(user).add(submission);
        db.getProblemSubmissions().get(problem).add(submission);
        System.out.println(user.getName() + "   Solved  " + problem.getTitle());
    }

    public List<Problem> getProblemsSolvedByUser(User user){
       return db.getUserSubmission().get(user).stream().map(submission -> submission.getProblem()).collect(Collectors.toList());
    }

    public Map<String, Object> problemSolvedBy(Problem problem) {
        Map<String, Object> result = new HashMap<>();

        Set<Submission> submissions = db.getProblemSubmissions()
                .getOrDefault(problem, Collections.emptySet());

        List<String> users = submissions.stream()
                .map(sub -> sub.getUser().getName())
                .collect(Collectors.toList());

        // Compute average time
        double avgTimeTaken = submissions.stream()
                .mapToLong(Submission::getTime) // or getTime()
                .average()
                .orElse(0.0);

        // Fill the result map
        result.put("users", users);
        result.put("avgTimeTaken", avgTimeTaken);

        return result;
    }


    //Fetch Problems
    public List<Map<String, Object>> fetchProblems(
            Optional<Set<String>> tagsOpt,
            Optional<Difficulty> difficultyOpt,
            Optional<String> sortByOpt,
            boolean desc
    ) {
        Stream<Problem> stream = db.getProblems().stream();

        // Filter by tags
        if (tagsOpt.isPresent()) {
            Set<String> lowerTags = tagsOpt.get().stream().map(String::toLowerCase).collect(Collectors.toSet());
            stream = stream.filter(p -> p.getTags().stream().anyMatch(lowerTags::contains));
        }

        // Filter by difficulty
        if (difficultyOpt.isPresent()) {
            Difficulty diff = difficultyOpt.get();
            stream = stream.filter(p -> p.getDifficulty() == diff);
        }

        // Map to result with metadata
        List<Map<String, Object>> result = stream.map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getProblemId());
            m.put("title", p.getTitle());
            m.put("description", p.getDescription());
            m.put("tags", p.getTags());
            m.put("difficulty", p.getDifficulty());
            m.put("score", p.getScore());
            Set<Submission> subs = db.getProblemSubmissions().getOrDefault(p, Collections.emptySet());
            m.put("solvedCount", subs.size());
            double avgTimeTaken = subs.stream()
                    .mapToLong(Submission::getTime) // or getTime()
                    .average()
                    .orElse(0.0);
            m.put("avgTimeSec", avgTimeTaken);
//            m.put("likes", p.likes());
            return m;
        }).collect(Collectors.toList());

        // Sorting comparator
        String sortBy = sortByOpt.orElse("score");
        Comparator<Map<String, Object>> comparator;
        switch (sortBy) {
            case "popularity":
                comparator = Comparator.comparingInt(m -> (Integer) m.get("solvedCount"));
                break;
            case "score":
            default:
                comparator = Comparator.comparingInt(m -> (Integer) m.get("score"));
                break;
        }
        if (desc) comparator = comparator.reversed();

        result.sort(comparator);
        return result;
    }



    public List<Map<String, Object>> topUsers(int n) {
        // 1️⃣ Convert each user entry into a leaderboard map
        List<Map<String, Object>> leaderboard = db.getUserSubmission().entrySet().stream()
                .map(entry -> {
                    User user = entry.getKey();
                    Set<Submission> submissions = entry.getValue();

                    // 2️⃣ Calculate total score
                    int totalScore = submissions.stream()
                            .mapToInt(Submission::getScoreAwarded)
                            .sum();

                    // 3️⃣ Collect solved problem titles
                    List<String> solvedProblems = submissions.stream()
                            .map(sub -> sub.getProblem().getTitle())
                            .collect(Collectors.toList());

                    // 4️⃣ Build the map
                    Map<String, Object> userMap = new LinkedHashMap<>();
                    userMap.put("userId", user.getUserId());
                    userMap.put("name", user.getName());
                    userMap.put("department", user.getDepartment());
                    userMap.put("totalScore", totalScore);
                    userMap.put("solvedProblems", solvedProblems);

                    return userMap;
                })
                // 5️⃣ Sort by totalScore descending
                .sorted((m1, m2) -> Integer.compare((Integer)m2.get("totalScore"), (Integer)m1.get("totalScore")))
                // 6️⃣ Limit to top n
                .limit(n)
                .collect(Collectors.toList());

        return leaderboard;
    }

    public List<Map<String, Object>> topDepartments(int n) {
        // 1️⃣ Aggregate total scores per department
        Map<String, Integer> departmentScores = new HashMap<>();
        for (Map.Entry<User, Set<Submission>> entry : db.getUserSubmission().entrySet()) {
            User user = entry.getKey();
            Set<Submission> submissions = entry.getValue();
            int totalScore = submissions.stream().mapToInt(Submission::getScoreAwarded).sum();

            departmentScores.merge(user.getDepartment(), totalScore, Integer::sum);

            /*
            //Equivalent to
            if (departmentScores.containsKey(user.getDepartment())) {
                departmentScores.put(user.getDepartment(),
                        departmentScores.get(user.getDepartment()) + totalScore);
            } else {
                departmentScores.put(user.getDepartment(), totalScore);
            }
             */

        }

        // 2️⃣ Convert to list of maps for leaderboard
        List<Map<String, Object>> leaderboard = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : departmentScores.entrySet()) {
            Map<String, Object> deptMap = new LinkedHashMap<>();
            deptMap.put("department", entry.getKey());
            deptMap.put("totalScore", entry.getValue());
            leaderboard.add(deptMap);
        }

        // 3️⃣ Sort by totalScore descending
        leaderboard.sort((m1, m2) -> Integer.compare((Integer)m2.get("totalScore"), (Integer)m1.get("totalScore")));

        // 4️⃣ Limit to top n
        return leaderboard.stream().limit(n).collect(Collectors.toList());
    }

//    public List<Problem> getTopProblemsByTag(String tag, int n) {
//        return problemRepo.values().stream()
//                .filter(p -> p.getTag().equalsIgnoreCase(tag))
//                .sorted(Comparator.comparingInt(Problem::getLikes).reversed())
//                .limit(n)
//                .collect(Collectors.toList());
//    }

    public void userLikedProblem(User user, Problem problem) {
        db.getLikedProblems().get(user).add(problem);
        if (db.getProblems().remove(problem)) {
            problem.setLikes(problem.getLikes() + 1);
            db.getProblems().add(problem);
        }
        return;
    }
}
