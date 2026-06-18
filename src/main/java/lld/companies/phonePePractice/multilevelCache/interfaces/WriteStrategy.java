package lld.companies.phonePePractice.multilevelCache.interfaces;

import lld.companies.phonePePractice.multilevelCache.model.CacheLevel;

import java.util.List;

public interface WriteStrategy {
    void write(String key, String value, List<CacheLevel> levels);
}
