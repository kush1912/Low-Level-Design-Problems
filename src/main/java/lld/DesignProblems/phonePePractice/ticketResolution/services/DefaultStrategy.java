package lld.DesignProblems.phonePePractice.ticketResolution.services;

import lld.DesignProblems.phonePePractice.ticketResolution.model.Agent;
import lld.DesignProblems.phonePePractice.ticketResolution.model.Issue;

import java.util.List;

public class DefaultStrategy implements AssignmentInterface{
    @Override
    public Agent assignIssue(List<Agent> agents, Issue issue) {
        for(Agent agent: agents){
            if(agent.isAvailable() && agent.getExpertise().contains(issue.getIssueType())){
                return agent;
            }
        }
        return null;
    }
}
