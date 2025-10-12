package lld.DesignProblems.phonePePractice.multilevelCache.model;

import lld.DesignProblems.phonePePractice.multilevelCache.interfaces.ReadStrategy;
import lld.DesignProblems.phonePePractice.multilevelCache.interfaces.WriteStrategy;

import java.util.LinkedList;
import java.util.List;


public class MultiLevelCacheManager {
    private static MultiLevelCacheManager instance;
    private final List<CacheLevel> levels;
    private final ReadStrategy readStrategy;
    private final WriteStrategy writeStrategy;

    private final List<Integer> last5Reads = new LinkedList<>();
    private final List<Integer> last5Writes = new LinkedList<>();

    private MultiLevelCacheManager(List<CacheLevel> levels,
                                   ReadStrategy readStrategy,
                                   WriteStrategy writeStrategy) {
        this.levels = levels;
        this.readStrategy = readStrategy;
        this.writeStrategy = writeStrategy;
    }

    public static synchronized MultiLevelCacheManager getInstance(
            List<CacheLevel> levels,
            ReadStrategy readStrategy,
            WriteStrategy writeStrategy) {
        if (instance == null) {
            instance = new MultiLevelCacheManager(levels, readStrategy, writeStrategy);
        }
        return instance;
    }

    public void readKey(String key) {
        readStrategy.read(key, levels);
    }

    public void writeKey(String key, String value) {
        writeStrategy.write(key, value, levels);
    }

    public void stat() {
        System.out.println("\n=== CACHE STATS ===");
        for (CacheLevel level : levels) {
            double usage = (double) level.getUsage() / level.getCapacity() * 100;
            System.out.printf("%s: %.0f%% used (%d/%d)%n", level.getName(), usage,
                    level.getUsage(), level.getCapacity());
            int total = level.getHits() + level.getMisses();
            double hitRatio = total == 0 ? 0 : (100.0 * level.getHits() / total);
            System.out.printf("   Hit Ratio: %.2f%% (%d hits, %d misses)%n",
                    hitRatio, level.getHits(), level.getMisses());
        }
    }
}
