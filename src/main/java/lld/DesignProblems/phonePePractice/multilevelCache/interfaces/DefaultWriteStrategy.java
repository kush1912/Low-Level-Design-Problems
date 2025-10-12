package lld.DesignProblems.phonePePractice.multilevelCache.interfaces;

import lld.DesignProblems.phonePePractice.multilevelCache.model.CacheLevel;

import java.util.List;

public class DefaultWriteStrategy implements WriteStrategy{
    @Override
    public void write(String key, String value, List<CacheLevel> levels) {
        int totalTime = 0;

        for (CacheLevel level : levels) {
            String existing = level.contains(key) ? level.get(key) : null;
            totalTime += level.getReadTime();

            if (existing != null && existing.equals(value)) {
                break;
            }

            level.put(key, value);
            totalTime += level.getWriteTime();
        }

        System.out.println("WRITE: Key=" + key + ", Value=" + value + ", TotalTime=" + totalTime);
    }

}
