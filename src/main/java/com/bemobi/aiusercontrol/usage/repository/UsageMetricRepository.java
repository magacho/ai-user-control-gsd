package com.bemobi.aiusercontrol.usage.repository;

import com.bemobi.aiusercontrol.enums.UsageMetricType;
import com.bemobi.aiusercontrol.model.entity.UsageMetric;
import com.bemobi.aiusercontrol.model.entity.UserAIToolAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsageMetricRepository extends JpaRepository<UsageMetric, Long> {

    Optional<UsageMetric> findByAccountAndMetricDateAndMetricType(
            UserAIToolAccount account, LocalDate metricDate, UsageMetricType metricType);

    List<UsageMetric> findByAccountAndMetricDateBetween(
            UserAIToolAccount account, LocalDate startDate, LocalDate endDate);

    List<UsageMetric> findByAccountIdAndMetricDateBetween(
            Long accountId, LocalDate startDate, LocalDate endDate);

    List<UsageMetric> findByMetricDate(LocalDate metricDate);

    List<UsageMetric> findByAccountId(Long accountId);
}
