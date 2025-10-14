package lld.DesignProblems.phonePePractice.ticketResolution.model;

import lld.DesignProblems.phonePePractice.ticketResolution.enums.IssueType;

import java.util.*;

public class Agent {
    private String id;
    private String email;
    private String name;
    private Set<IssueType> expertise;

    public Agent(String id, String email, String name, Set<IssueType> expertise) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.expertise = expertise;
    }

    private String assignedIssueId;
    private Queue<String> waitList = new LinkedList<>();
    private List<String> history = new ArrayList<>();

    public boolean isAvailable(){
        return  assignedIssueId == null;
    }


    //Getter & Setter
    public void setId(String id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setExpertise(Set<IssueType> expertise) {
        this.expertise = expertise;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public Set<IssueType> getExpertise() {
        return expertise;
    }

    public String getAssignedIssueId() {
        return assignedIssueId;
    }

    public Queue<String> getWaitList() {
        return waitList;
    }

    public List<String> getHistory() {
        return history;
    }

    public void setAssignedIssueId(String assignedIssueId) {
        this.assignedIssueId = assignedIssueId;
    }

    public void setWaitList(Queue<String> waitList) {
        this.waitList = waitList;
    }

    public void setHistory(List<String> history) {
        this.history = history;
    }
}
