package com.cloud.app.service;

import com.cloud.app.dao.HealthCheckDao;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class HealthCheckServiceTest {

    @Autowired
    private HealthCheckService healthCheckService;

    @MockBean
    private HealthCheckDao healthCheckDao;
    @Test
    void whenDbConnectionSuccessful_thenReturnTrue() throws Exception{
        Mockito.when(healthCheckDao.databaseConnectivityStatus()).thenReturn(1);
        boolean value = healthCheckService.isDatabaseRunning();
        assertTrue(value);
    }
    @Test
    void whenDbConnectionFails_thenReturnFalse() throws Exception{
        Mockito.when(healthCheckDao.databaseConnectivityStatus()).thenReturn(null);
        boolean value = healthCheckService.isDatabaseRunning();
        assertFalse(value);
    }
}
