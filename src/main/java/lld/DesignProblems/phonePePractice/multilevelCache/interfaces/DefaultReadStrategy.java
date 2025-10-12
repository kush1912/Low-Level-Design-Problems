package lld.DesignProblems.phonePePractice.multilevelCache.interfaces;

import lld.DesignProblems.phonePePractice.multilevelCache.model.CacheLevel;

import java.util.List;

public class DefaultReadStrategy implements ReadStrategy{
    @Override
    public void read(String key, List<CacheLevel> levels) {
        int totalTime = 0;
        String value = null;
        int foundIndex = -1;

        for (int i = 0; i < levels.size(); i++) {
            CacheLevel level = levels.get(i);
            totalTime += level.getReadTime();

            if (level.contains(key)) {
                value = level.get(key);
                foundIndex = i;
                break;
            }
        }

        // If found at lower level, promote to upper levels
        if (value != null && foundIndex > 0) {
            for (int i = 0; i < foundIndex; i++) {
                CacheLevel upper = levels.get(i);
                upper.put(key, value);
                totalTime += upper.getWriteTime();
            }
        }

        System.out.println(value != null
                ? "READ: Key=" + key + ", Value=" + value + ", TotalTime=" + totalTime
                : "READ: Key=" + key + " not found, TotalTime=" + totalTime);
    }
}
