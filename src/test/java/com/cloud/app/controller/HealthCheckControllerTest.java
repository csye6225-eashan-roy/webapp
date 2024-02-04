package com.cloud.app.controller;

import com.cloud.app.service.HealthCheckService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthCheckController.class)
public class HealthCheckControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HealthCheckService healthCheckService;

    @Test
    //200 status ok
    void dbConnectionSuccessful() throws Exception {
        Mockito.when(healthCheckService.isDatabaseRunning()).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.get("/healthz"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-control", "no-cache, no-store, must-revalidate"))
                .andExpect(header().string("Pragma","no-cache"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }
    @Test
    //503 service unavailable
    void dbConnectionFailure() throws Exception {
        Mockito.when(healthCheckService.isDatabaseRunning()).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get("/healthz"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().string("Cache-control", "no-cache, no-store, must-revalidate"))
                .andExpect(header().string("Pragma","no-cache"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }
    @Test
    //400 bad request
    void queryParamsInRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/healthz")
                .contentType(MediaType.APPLICATION_JSON)
                .param("key","value"))
                .andExpect(status().isBadRequest());
    }
    @Test
    //400 bad request
    void payloadInRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/healthz")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "key":"value"
                        }"""))
                .andExpect(status().isBadRequest());
    }
    @Test
    //405 method not allowed
    void methodNotAllowedCheck() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.post("/healthz"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Cache-control", "no-cache, no-store, must-revalidate"))
                .andExpect(header().string("Pragma","no-cache"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }
}
