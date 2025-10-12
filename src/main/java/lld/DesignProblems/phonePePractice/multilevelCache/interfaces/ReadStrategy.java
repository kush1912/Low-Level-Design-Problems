package lld.DesignProblems.phonePePractice.multilevelCache.interfaces;

import lld.DesignProblems.phonePePractice.multilevelCache.model.CacheLevel;

import java.util.List;

public interface ReadStrategy {
    void read(String key, List<CacheLevel> levels);
}
