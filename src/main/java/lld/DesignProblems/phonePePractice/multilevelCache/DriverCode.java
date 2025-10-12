package lld.DesignProblems.phonePePractice.multilevelCache;

import lld.DesignProblems.phonePePractice.multilevelCache.interfaces.DefaultReadStrategy;
import lld.DesignProblems.phonePePractice.multilevelCache.interfaces.DefaultWriteStrategy;
import lld.DesignProblems.phonePePractice.multilevelCache.model.CacheLevel;
import lld.DesignProblems.phonePePractice.multilevelCache.model.MultiLevelCacheManager;

import java.util.ArrayList;
import java.util.List;

public class DriverCode {
    public static void main(String[] args) {
        List<CacheLevel> levels = new ArrayList<>();
        levels.add(new CacheLevel("L1", 2, 1, 2));
        levels.add(new CacheLevel("L2", 3, 2, 4));
        levels.add(new CacheLevel("L3", 5, 3, 6));

        //MultiLevelCacheManager cacheManager = new MultiLevelCacheManager(levels);
        MultiLevelCacheManager cacheManager = MultiLevelCacheManager.getInstance(
                levels, new DefaultReadStrategy(), new DefaultWriteStrategy()
        );
        cacheManager.writeKey("A", "Apple"); // Write key
        cacheManager.writeKey("B", "Banana");
        cacheManager.writeKey("C", "Cat");
        cacheManager.writeKey("D", "Dog");
        cacheManager.writeKey("E", "Egg");
        cacheManager.readKey("A");           // Read key
        cacheManager.readKey("E");
        cacheManager.readKey("D");
        cacheManager.stat();                 // Print statistics
    }
}



/*
com.cache
 ├── strategy
 │    ├── ReadStrategy.java
 │    ├── WriteStrategy.java
 │    ├── DefaultReadStrategy.java
 │    └── DefaultWriteStrategy.java
 ├── model
 │    └── CacheLevel.java
 ├── service
 │    ├── MultiLevelCacheManager.java
 │    ├── CacheReadService.java
 │    ├── CacheWriteService.java
 │    └── CacheStatService.java
 └── Main.java

 */