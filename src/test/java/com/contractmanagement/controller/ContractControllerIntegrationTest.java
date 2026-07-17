package com.contractmanagement.controller;

import com.contractmanagement.dto.request.CreateContractRequest;
import com.contractmanagement.enums.ContractType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.junit.jupiter.api.BeforeEach;

@SpringBootTest
@ActiveProfiles("test")
class ContractControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    private ObjectMapper objectMapper;

    @Test
    void createContract_ValidRequest_ReturnsCreated() throws Exception {
        CreateContractRequest request = new CreateContractRequest(
                "CUST-001",
                "Integration Test Contract",
                ContractType.LEASE,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(60),
                "USD",
                "Net 30"
        );

        mockMvc.perform(post("/api/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerReference").value("CUST-001"))
                .andExpect(jsonPath("$.title").value("Integration Test Contract"));
    }

    @Test
    void getContract_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/contracts/999"))
                .andExpect(status().isNotFound());
    }
}
