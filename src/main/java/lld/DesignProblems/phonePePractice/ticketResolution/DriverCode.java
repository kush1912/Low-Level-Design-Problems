package lld.DesignProblems.phonePePractice.ticketResolution;

import lld.DesignProblems.phonePePractice.ticketResolution.db.AgentRepository;
import lld.DesignProblems.phonePePractice.ticketResolution.db.IssueRepository;
import lld.DesignProblems.phonePePractice.ticketResolution.enums.IssueStatus;
import lld.DesignProblems.phonePePractice.ticketResolution.enums.IssueType;
import lld.DesignProblems.phonePePractice.ticketResolution.model.Issue;
import lld.DesignProblems.phonePePractice.ticketResolution.services.AgentService;
import lld.DesignProblems.phonePePractice.ticketResolution.services.AssignmentInterface;
import lld.DesignProblems.phonePePractice.ticketResolution.services.AssignmentService;
import lld.DesignProblems.phonePePractice.ticketResolution.services.IssueService;

import java.util.Arrays;
import java.util.Map;

public class DriverCode {
    public static void main(String[] args) {
        AgentRepository agentRepository = new AgentRepository();
        IssueRepository issueRepository =  new IssueRepository();
        AgentService agentService =  new AgentService(agentRepository);
        IssueService issueService =  new IssueService(issueRepository, agentRepository);
        AssignmentInterface DefaultStrategy = new DefaultStrategy();
        AssignmentService assignmentService = new AssignmentService(agentRepository, issueRepository, DefaultStrategy);

        Issue i1 = issueService.createIssue("T1", IssueType.PAYMENT_RELATED, "Payment Failed", "My payment failed but money is debited", "testUser1@test.com");
        Issue i2 = issueService.createIssue("T2", IssueType.MUTUAL_FUND_RELATED, "Purchase Failed", "Unable to purchase Mutual Fund", "testUser2@test.com");
        Issue i3 =  issueService.createIssue("T3", IssueType.PAYMENT_RELATED, "Payment Failed", "My payment failed but money is debited",  "testUser2@test.com");

        agentService.addAgent("agent1@test.com", "Agent 1", Arrays.asList(IssueType.PAYMENT_RELATED, IssueType.GOLD_RELATED));
        agentService.addAgent("agent2@test.com", "Agent 2", Arrays.asList(IssueType.MUTUAL_FUND_RELATED));

        assignmentService.assignIssue(i1.getId());
        assignmentService.assignIssue(i2.getId());
        assignmentService.assignIssue(i3.getId());

        issueService.getIssue(Map.of("type","Payment Related")).forEach(System.out::println);
        System.out.println();
        issueService.getIssue(Map.of("type","Payment Related")).forEach(System.out::println);
        System.out.println();
        issueService.getIssue(Map.of("type", "Payment Related")).forEach(System.out::println);
        System.out.println();

        issueService.updateIssue(i3.getId(), IssueStatus.IN_PROGRESS, "Waiting for payment confirmation");
        issueService.resolveIssue(i3.getId(),  "PaymentFailed debited amount will get reversed");
        agentService.viewAgentsWorkHistory();
    }
}
