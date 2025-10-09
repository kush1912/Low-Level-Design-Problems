package lld.DesignProblems.phonePePractice.hackathon;

import lld.DesignProblems.phonePePractice.hackathon.db.InMemoryDB;
import lld.DesignProblems.phonePePractice.hackathon.enums.Difficulty;
import lld.DesignProblems.phonePePractice.hackathon.model.Problem;
import lld.DesignProblems.phonePePractice.hackathon.model.User;
import lld.DesignProblems.phonePePractice.hackathon.services.Hackathon;
import lombok.ToString;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ToString
public class DriverCode {
    public static void main(String[] args) {

        User u1 = new User("Ajay" ,"PG");
        User u2 = new User("Umang" , "SQl");
        User u3 = new User("Dolly" ,"PG");
        User u4 = new User("Dolly" ,"SQL");
        User u5 = new User("Nikhil" ,"Cosmos");
        User u6 = new User("Gautam" ,"Cosmos");


        Problem p1 = new Problem( "Two Sum", "Find two numbers that add up to target", new HashSet<>(Arrays.asList("array","hashmap","dsa")), Difficulty.EASY, 10);
        Problem p2 = new Problem("Longest Substring Without Repeating Characters", "Find the length of the longest substring without repeating characters.", new HashSet<>(Arrays.asList("string", "array")), Difficulty.MEDIUM, 20);
        Problem p3 = new Problem( "LRU Cache","LRU Cache", new HashSet<>(Arrays.asList("design","dsa")), Difficulty.MEDIUM, 20);
        Problem p4 = new Problem("Shortest Distance","Dijkstra", new HashSet<>(Arrays.asList("graphs","dijkstra", "dsa")), Difficulty.HARD, 30);
        Problem p5 = new Problem("Maximum Subarray Sum", "Find the contiguous subarray with the largest sum.", new HashSet<>(Arrays.asList("array", "dp", "kadane")), Difficulty.MEDIUM, 20);

        Hackathon hackathon =  new Hackathon();
        hackathon.getService().addProblem(p1);
        hackathon.getService().addProblem(p2);
        hackathon.getService().addProblem(p3);
        hackathon.getService().addProblem(p4);
        hackathon.getService().addProblem(p5);

        hackathon.getService().registerUser(u1);
        hackathon.getService().registerUser(u2);
        hackathon.getService().registerUser(u3);
        hackathon.getService().registerUser(u4);
        hackathon.getService().registerUser(u5);
        hackathon.getService().registerUser(u6);

        hackathon.getService().Solve(u1,p2,5);
        hackathon.getService().Solve(u3,p2,10);
        hackathon.getService().Solve(u1,p1,10);
        hackathon.getService().Solve(u4,p2,3);
        hackathon.getService().Solve(u5,p4,15);
        hackathon.getService().Solve(u6,p5,15);
        hackathon.getService().Solve(u4,p1,10);
        hackathon.getService().Solve(u5,p2,50);

        System.out.println(hackathon.getService().getProblemsSolvedByUser(u1).toString());

        System.out.println(hackathon.getService().topDepartments(2));

        System.out.println(hackathon.getService().topUsers(2));

        System.out.println(hackathon.getService().problemSolvedBy(p4));

//        hackathon.getService().userLikedProblem(u2,p4);

        Set<String> tags = new HashSet<>(Arrays.asList("array", "dsa"));
        System.out.println(hackathon.getService().fetchProblems(Optional.of(tags),
                Optional.empty(), Optional.of("score"), true));

    }
}
