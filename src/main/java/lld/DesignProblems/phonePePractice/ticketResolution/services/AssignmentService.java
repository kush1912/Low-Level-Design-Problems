package lld.DesignProblems.phonePePractice.ticketResolution.services;

import lld.DesignProblems.phonePePractice.hackathon.utils.AppException;
import lld.DesignProblems.phonePePractice.ticketResolution.db.AgentRepository;
import lld.DesignProblems.phonePePractice.ticketResolution.db.IssueRepository;
import lld.DesignProblems.phonePePractice.ticketResolution.enums.IssueStatus;
import lld.DesignProblems.phonePePractice.ticketResolution.model.Agent;
import lld.DesignProblems.phonePePractice.ticketResolution.model.Issue;

import java.util.ArrayList;
import java.util.List;

public class AssignmentService {
    private AgentRepository agentRepository;
    private IssueRepository issueRepository;
    private AssignmentInterface DefaultStrategy;

    public AssignmentService(AgentRepository agentRepository, IssueRepository issueRepository, AssignmentInterface DefaultStrategy ) {
        this.agentRepository = agentRepository;
        this.issueRepository = issueRepository;
        this.DefaultStrategy = DefaultStrategy;

    }

    public void assignIssue(String issueId){
        Issue issue = issueRepository.getById(issueId);
        if(issue==null) throw  new AppException("Issue Not found!");
        List<Agent> agents = new ArrayList<>(agentRepository.getAll());
        Agent assigned = DefaultStrategy.assignIssue(agents,issue);
        if(assigned!=null){
            assigned.setAssignedIssueId(issue.getId());
            issue.setAgentId(assigned.getId());
            issue.setStatus(IssueStatus.IN_PROGRESS);
            issueRepository.issues.put(issue.getId(), issue);
            agentRepository.agents.put(assigned.getId(), assigned);
            System.out.println("Issue assigned to Agent: "+ assigned.getId());
        }else{
            for(Agent agent: agents){
                if(agent.getExpertise().contains(issue.getIssueType())){
                    agent.getWaitList().add(issue.getId());
                    issue.setStatus(IssueStatus.WAITING);
                    issue.setAgentId(agent.getId());
                    issueRepository.issues.put(issue.getId(), issue);
                    agentRepository.agents.put(agent.getId(), agent);
                    System.out.println("Issue added to Wait List of Agent: " + agent.getId());
                    return;
                }
            }
            System.out.println("No Agent found for the expertise!");
        }
    }
}
