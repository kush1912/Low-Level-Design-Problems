package lld.companies.phonePePractice.hackathon.services;

import lld.companies.phonePePractice.hackathon.Interface.ScoringStrategy;
import lld.companies.phonePePractice.hackathon.db.InMemoryDB;

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
