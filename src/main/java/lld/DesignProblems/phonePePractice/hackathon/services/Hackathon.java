package lld.DesignProblems.phonePePractice.hackathon.services;

import lld.DesignProblems.phonePePractice.hackathon.Interface.ScoringStrategy;
import lld.DesignProblems.phonePePractice.hackathon.db.InMemoryDB;

public class Hackathon {
    private HackathonService service;

    public Hackathon() {
        InMemoryDB db = new InMemoryDB();
        ScoringStrategy scoring = new DefaultScoring();
        this.service = new HackathonService(db, scoring);
    }

    public HackathonService getService() {
        return service;
    }
}
