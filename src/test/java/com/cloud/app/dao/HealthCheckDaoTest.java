package com.cloud.app.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
@DataJpaTest
public class HealthCheckDaoTest {

    @Autowired
    private HealthCheckDao healthCheckDao;
    @Test
    public void databaseConnectivityStatus_shouldReturnOne(){
        Integer status = healthCheckDao.databaseConnectivityStatus();
        assertEquals(0,status);
    }
}
