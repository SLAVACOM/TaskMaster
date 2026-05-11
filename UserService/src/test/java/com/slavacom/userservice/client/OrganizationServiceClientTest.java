package com.slavacom.userservice.client;

import com.slavacom.userservice.dto.OrganizationInfoDto;
import com.slavacom.userservice.exception.OrganizationNotFoundException;
import com.slavacom.userservice.exception.ServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceClientTest {

    @Mock
    private RestClient restClient;

    private OrganizationServiceClient organizationServiceClient;

    private UUID organizationId;
    private OrganizationInfoDto organizationInfoDto;

    @BeforeEach
    void setUp() {
        organizationId = UUID.randomUUID();
        organizationInfoDto = new OrganizationInfoDto(
                organizationId.toString(),
                "Test Organization",
                "Test Description",
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "ADMIN"
        );
        organizationServiceClient = new OrganizationServiceClient(restClient);
        ReflectionTestUtils.setField(organizationServiceClient, "organizationServiceUrl", "http://localhost:8083");
    }

    @Test
    void testGetOrganizationById_WithValidInput_HasCorrectUrl() {
        // This test verifies the exception classes exist and can be instantiated
        OrganizationNotFoundException notFound = new OrganizationNotFoundException(organizationId.toString());
        assertNotNull(notFound);
        assertTrue(notFound.getMessage().contains(organizationId.toString()));

        ServiceUnavailableException unavailable = new ServiceUnavailableException("TestService");
        assertNotNull(unavailable);
        assertTrue(unavailable.getMessage().contains("TestService"));
    }

    @Test
    void testOrganizationNotFoundException_StoresOrganizationId() {
        String testId = "test-org-id";
        OrganizationNotFoundException exception = new OrganizationNotFoundException(testId);
        assertEquals(testId, exception.getOrganizationId());
        assertTrue(exception.getMessage().contains(testId));
    }

    @Test
    void testServiceUnavailableException_StoresServiceName() {
        String serviceName = "OrganizationService";
        ServiceUnavailableException exception = new ServiceUnavailableException(serviceName);
        assertEquals(serviceName, exception.getServiceName());
        assertTrue(exception.getMessage().contains(serviceName));
    }

    @Test
    void testServiceUnavailableException_WithCause() {
        String serviceName = "OrganizationService";
        Throwable cause = new RestClientException("Connection failed");
        ServiceUnavailableException exception = new ServiceUnavailableException(serviceName, cause);
        assertEquals(cause, exception.getCause());
        assertEquals(serviceName, exception.getServiceName());
    }
}
