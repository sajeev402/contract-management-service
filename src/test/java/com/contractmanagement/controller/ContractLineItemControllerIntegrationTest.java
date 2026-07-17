package com.contractmanagement.controller;

import com.contractmanagement.dto.request.CreateContractLineItemRequest;
import com.contractmanagement.entity.Contract;
import com.contractmanagement.enums.ContractStatus;
import com.contractmanagement.enums.ContractType;
import com.contractmanagement.repository.ContractRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
class ContractLineItemControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    private ObjectMapper objectMapper;

    @Autowired
    private ContractRepository contractRepository;

    private Contract testContract;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        testContract = Contract.builder()
                .contractNumber(UUID.randomUUID().toString().substring(0, 8))
                .customerReference("CUST-LINE-TEST")
                .title("Line Item Test Contract")
                .contractType(ContractType.LEASE)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(60))
                .currency("USD")
                .status(ContractStatus.DRAFT)
                .grossValue(BigDecimal.ZERO)
                .netValue(BigDecimal.ZERO)
                .deleted(false)
                .build();
        testContract = contractRepository.save(testContract);
    }

    @Test
    void createLineItem_ValidRequest_ReturnsCreated() throws Exception {
        CreateContractLineItemRequest request = new CreateContractLineItemRequest(
                "PROD-001",
                "Test Product",
                10,
                new BigDecimal("50.00"),
                new BigDecimal("0.00")
        );

        mockMvc.perform(post("/api/contracts/" + testContract.getId() + "/line-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productCode").value("PROD-001"))
                .andExpect(jsonPath("$.lineTotal").value(500.00));
    }
}
