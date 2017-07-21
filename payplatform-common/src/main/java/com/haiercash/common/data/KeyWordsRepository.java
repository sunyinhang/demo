package com.haiercash.common.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KeyWordsRepository extends PagingAndSortingRepository<KeyWords, String> {
    @Query(value ="from KeyWords where isTrue=?1")
    List<KeyWords> findByIsTrue(String isTrue);
}
