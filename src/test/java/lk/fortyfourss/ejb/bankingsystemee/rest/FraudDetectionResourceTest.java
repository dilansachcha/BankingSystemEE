package lk.fortyfourss.ejb.bankingsystemee.rest;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FraudDetectionResourceTest {

    @InjectMocks
    private FraudDetectionResource fraudDetectionResource;

    @Mock
    private ContainerRequestContext mockRequestContext;

    @Test
    void testAnalyzeRisk_FailsWhenUserIsNotAdmin() {
        when(mockRequestContext.getProperty("role")).thenReturn("USER");

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> fraudDetectionResource.analyzeRisk(1, mockRequestContext)
        );

        assertEquals(403, exception.getResponse().getStatus(), "Status should be 403 Forbidden for non-admins");
    }

    @Test
    void testAnalyzeRisk_FailsWhenRoleIsNull() {
        when(mockRequestContext.getProperty("role")).thenReturn(null);

        WebApplicationException exception = assertThrows(
                WebApplicationException.class,
                () -> fraudDetectionResource.analyzeRisk(1, mockRequestContext)
        );

        assertEquals(403, exception.getResponse().getStatus(), "Status should be 403 Forbidden for unauthenticated users");
    }
}