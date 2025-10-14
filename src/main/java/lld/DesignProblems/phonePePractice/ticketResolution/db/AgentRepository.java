package lld.DesignProblems.phonePePractice.ticketResolution.db;

import lld.DesignProblems.phonePePractice.ticketResolution.model.Agent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AgentRepository {
    public Map<String , Agent> agents = new HashMap<>();
    public void save(Agent agent){
        agents.put(agent.getId(),agent);
    }
    public Agent getById(String id){ return agents.get(id);}
    public Collection<Agent> getAll(){
        return agents.values();
    }
}
