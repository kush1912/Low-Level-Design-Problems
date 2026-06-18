package lld.companies.phonePePractice.ticketResolution.services;

import lld.companies.phonePePractice.ticketResolution.model.Agent;
import lld.companies.phonePePractice.ticketResolution.model.Issue;

import java.util.List;

public interface AssignmentInterface {
    Agent assignIssue(List<Agent> agents, Issue issue);
}
