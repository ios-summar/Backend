package com.summar.summar.results.repository;

import com.summar.summar.domain.Setting;
import com.summar.summar.enumeration.SettingType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettingRepository extends JpaRepository<Setting,Long> {
    List<Setting> findAllBySettingType(SettingType notice);

    List<Setting> findBySettingType(SettingType notice);
}
