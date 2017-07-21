package com.haiercash.common.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AppOrderRequestRepository extends JpaRepository<AppOrderRequest, String> {

    @Query(" from AppOrderRequest where state = '0' or state = '3'")
    List<AppOrderRequest> findUncompletedRequest();
}
