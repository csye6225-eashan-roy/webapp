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
            if(healthCheckDao.databaseConnectivityStatus() == 1)
                return true;
        } catch(Exception e) {
            LOGGER.error("Error while checking your postgresql database connectivity");
        }
        return false;
    }
}
