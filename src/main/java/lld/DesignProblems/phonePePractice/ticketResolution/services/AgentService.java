package lld.DesignProblems.phonePePractice.ticketResolution.services;

import lld.DesignProblems.phonePePractice.ticketResolution.db.AgentRepository;
import lld.DesignProblems.phonePePractice.ticketResolution.enums.IssueType;
import lld.DesignProblems.phonePePractice.ticketResolution.model.Agent;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class AgentService {
    private AgentRepository agentRepository;

    public AgentService(AgentRepository agentRepository){
        this.agentRepository= agentRepository;
    }
    public void addAgent(String email, String name, List<IssueType> issueTypes){
        String id = "A" + UUID.randomUUID().toString().substring(0,6);
        Agent agent = new Agent(id, email, name, new HashSet<>(issueTypes));
        agentRepository.save(agent);
        System.out.println("Agent added: "+ agent.getId());
    }

    public void viewAgentsWorkHistory(){
        System.out.println(agentRepository.getAll());
        for(Agent agent: agentRepository.getAll()){
            System.out.println(agent.getId() + ": "+ agent.getHistory());
        }
    }
}

