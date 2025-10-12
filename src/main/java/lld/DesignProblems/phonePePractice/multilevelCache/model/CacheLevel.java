package lld.DesignProblems.phonePePractice.multilevelCache.model;

import lld.DesignProblems.phonePePractice.multilevelCache.interfaces.ReadStrategy;
import lld.DesignProblems.phonePePractice.multilevelCache.interfaces.WriteStrategy;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

@Getter
@Setter
public class CacheLevel {
    private String name;
    private int capacity;
    private int readTime;
    private int writeTime;

    private Map<String, String> data = new HashMap<>();
    private Queue<String> order = new LinkedList<>();
    private int hits = 0;
    private int misses = 0;

    public CacheLevel(String name, int capacity, int readTime, int writeTime) {
        this.name = name;
        this.capacity = capacity;
        this.readTime = readTime;
        this.writeTime = writeTime;
    }

    public boolean contains(String key) {
        return data.containsKey(key);
    }

    public String get(String key) {
        if (data.containsKey(key)) {
            hits++;
            order.remove(key);
            order.offer(key);
            return data.get(key);
        } else {
            misses++;
            return null;
        }
    }

    public void put(String key, String value) {
        if (data.size() >= capacity) {
            String lru = order.poll();
            data.remove(lru);
        }
        data.put(key, value);
        order.remove(key);
        order.offer(key);
    }

    public int getCapacity() { return capacity; }
    public int getUsage() { return data.size(); }
    public int getReadTime() { return readTime; }
    public int getWriteTime() { return writeTime; }
    public String getName() { return name; }
    public int getHits() { return hits; }
    public int getMisses() { return misses; }

}
