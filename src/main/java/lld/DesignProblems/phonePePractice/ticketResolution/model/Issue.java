package lld.DesignProblems.phonePePractice.ticketResolution.model;

import lld.DesignProblems.phonePePractice.ticketResolution.enums.IssueStatus;
import lld.DesignProblems.phonePePractice.ticketResolution.enums.IssueType;

public class Issue {
    private String id;
    private String transactionId;
    private IssueType issueType;
    private String subject;
    private String description;
    private String email;
    private IssueStatus status;
    private String resolution;
    private String agentId;

    public Issue(String id,String transactionId, IssueType issueType, String subject, String description, String email) {
        this.id= id;
        this.transactionId = transactionId;
        this.issueType = issueType;
        this.subject = subject;
        this.description = description;
        this.email = email;
    }

    //getter and Setter


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public IssueType getIssueType() {
        return issueType;
    }

    public void setIssueType(IssueType issueType) {
        this.issueType = issueType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public void setStatus(IssueStatus status) {
        this.status = status;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
}
