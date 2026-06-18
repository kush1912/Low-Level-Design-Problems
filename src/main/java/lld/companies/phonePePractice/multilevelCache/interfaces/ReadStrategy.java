package lld.companies.phonePePractice.multilevelCache.interfaces;

import lld.companies.phonePePractice.multilevelCache.model.CacheLevel;

import java.util.List;

public interface ReadStrategy {
    void read(String key, List<CacheLevel> levels);
}
