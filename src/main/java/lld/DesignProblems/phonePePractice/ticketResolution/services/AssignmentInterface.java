package lld.DesignProblems.phonePePractice.ticketResolution.services;

import lld.DesignProblems.phonePePractice.ticketResolution.model.Agent;
import lld.DesignProblems.phonePePractice.ticketResolution.model.Issue;

import java.util.List;

public interface AssignmentInterface {
    Agent assignIssue(List<Agent> agents, Issue issue);
}
