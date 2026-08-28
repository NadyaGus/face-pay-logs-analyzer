package com.facepay.app.repository;

import com.facepay.app.models.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Map;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {
    @Query("SELECT e.errorCode, COUNT(e) FROM ErrorLog e WHERE e.errorCode IS NOT NULL GROUP BY e.errorCode")
    List<Object[]> countByErrorCode();

    @Query("SELECT e.accountId, COUNT(e) FROM ErrorLog e WHERE e.accountId IS NOT NULL GROUP BY e.accountId")
    List<Object[]> countByAccountId();

    @Query("SELECT e.merchantId, COUNT(e) FROM ErrorLog e WHERE e.merchantId IS NOT NULL GROUP BY e.merchantId")
    List<Object[]> countByMerchantId();

    List<ErrorLog> findByAccountIdContaining(String accountId);
}
