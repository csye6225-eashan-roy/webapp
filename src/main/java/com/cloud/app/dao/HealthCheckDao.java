package com.cloud.app.dao;

import com.cloud.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HealthCheckDao extends JpaRepository<User, Long> {
    @Query(value = "SELECT 1",nativeQuery = true)
    Integer databaseConnectivityStatus();
}
