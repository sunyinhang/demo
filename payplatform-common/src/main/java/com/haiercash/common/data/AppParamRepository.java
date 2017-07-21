package com.haiercash.common.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppParamRepository extends JpaRepository<AppParam, String> {
    List<AppParam> findBySysTyp(String sysTyp);

}
