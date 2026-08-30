package lld.DesignProblems.distributedScheduler.helper;

import java.util.concurrent.ThreadLocalRandom;

public final class IdGenerator {

    public static String generate(String prefix) {
        int randomNumber = ThreadLocalRandom.current().nextInt(100_000, 1_000_000);
        return prefix + "-" + randomNumber;
    }
}
