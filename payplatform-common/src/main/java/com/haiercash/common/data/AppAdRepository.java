package com.haiercash.common.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppAdRepository extends JpaRepository<AppAd, String> {
    List<AppAd> findByIsActive(String isActive);

    AppAd findByIdAndIsActive(String adId, String isActive);
}
