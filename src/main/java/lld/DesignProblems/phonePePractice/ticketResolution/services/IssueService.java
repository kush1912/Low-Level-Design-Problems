package lld.DesignProblems.phonePePractice.ticketResolution.services;

import lld.DesignProblems.phonePePractice.hackathon.utils.AppException;
import lld.DesignProblems.phonePePractice.ticketResolution.db.AgentRepository;
import lld.DesignProblems.phonePePractice.ticketResolution.db.IssueRepository;
import lld.DesignProblems.phonePePractice.ticketResolution.enums.IssueStatus;
import lld.DesignProblems.phonePePractice.ticketResolution.enums.IssueType;
import lld.DesignProblems.phonePePractice.ticketResolution.model.Agent;
import lld.DesignProblems.phonePePractice.ticketResolution.model.Issue;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class IssueService {
    private IssueRepository issueRepository;
    private AgentRepository agentRepository;

    public Issue createIssue(String transactionId, IssueType issueType, String subject, String description, String email){
        String id = "U" + UUID.randomUUID().toString().substring(0,6);
        Issue issue =  new Issue(id,transactionId, issueType, subject, description, email);
        issue.setStatus(IssueStatus.OPEN);
        issueRepository.save(issue);
        System.out.println("Issue Created: "+ issue.getId());
        System.out.println("Issue Details: " + issue);
        return issue;
    }


    public List<Issue> getIssue(Map<String, String> filters) {
        return issueRepository.getAll().stream()
                .filter(issue -> {
                    String emailFilter = filters.get("email");
                    String typeFilter = filters.get("type");
                    String statusFilter = filters.get("status");

                    if (emailFilter != null && !issue.getEmail().equalsIgnoreCase(emailFilter)) {
                        return false;
                    }

                    if (typeFilter != null &&
                            !issue.getIssueType().name().equalsIgnoreCase(typeFilter.replace(" ", "_"))) {
                        return false;
                    }

                    if (statusFilter != null &&
                            !issue.getStatus().name().equalsIgnoreCase(statusFilter)) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }


    public void updateIssue(String issueId, IssueStatus status, String resolution){
        Issue issue =  issueRepository.getById(issueId);
        if(issue==null) throw new IllegalArgumentException("Issue Not found!");
        issue.setStatus(status);
        issue.setResolution(resolution);
        issueRepository.issues.put(issue.getId(), issue);
        System.out.println("Issue Updated: " + issue.getId());
    }

    public void resolveIssue(String issueId, String resolution){
        Issue issue = issueRepository.getById(issueId);
        if(issue==null) throw new AppException("Issue Not found!");

        issue.setStatus(IssueStatus.RESOLVED);
        issue.setResolution(resolution);
        if(issue.getAgentId()!=null){
            Agent agent= agentRepository.getById(issue.getAgentId());
            if(agent!=null){
                agent.getHistory().add(issue.getId());
                agent.setAssignedIssueId(null);

                agentRepository.agents.put(agent.getId(), agent);
            }
        }
        System.out.println("Issue Resolved: " + issue.getId());
    }
}
