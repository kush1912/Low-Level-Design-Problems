package lld.DesignProblems.phonePePractice.hackathon.utils;

import java.util.UUID;

public class IdGenerator {
    private IdGenerator() {}
    public static String id() { return UUID.randomUUID().toString(); }
}
