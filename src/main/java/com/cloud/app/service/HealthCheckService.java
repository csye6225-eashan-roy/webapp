package com.cloud.app.service;

import com.cloud.app.dao.HealthCheckDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HealthCheckService {

    @Autowired
    HealthCheckDao healthCheckDao;
    private final Logger LOGGER = LoggerFactory.getLogger(HealthCheckService.class);
    public boolean isDatabaseRunning() {
        try{
            LOGGER.debug("Checking database connectivity...");
            if(healthCheckDao.databaseConnectivityStatus() == 1) {
                LOGGER.info("Database connectivity check successful.");
                return true;
            }
        } catch(Exception e) {
            LOGGER.error("Exception occurred while checking database connectivity: {}", e.getMessage());
        }
        LOGGER.warn("Database connectivity check failed.");
        return false;
    }
}
