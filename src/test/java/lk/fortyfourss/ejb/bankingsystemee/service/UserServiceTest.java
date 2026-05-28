package lk.fortyfourss.ejb.bankingsystemee.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lk.fortyfourss.ejb.bankingsystemee.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Long> mockCountQuery;

    @Test
    void testFindById_ReturnsUser() {
        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setFullName("Test User");
        when(em.find(User.class, 1)).thenReturn(mockUser);

        User result = userService.findById(1);

        assertNotNull(result);
        assertEquals("Test User", result.getFullName());
        verify(em, times(1)).find(User.class, 1);
    }

    @Test
    void testEmailExists_ReturnsTrue() {
        String testEmail = "test@example.com";
        when(em.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)).thenReturn(mockCountQuery);
        when(mockCountQuery.setParameter("email", testEmail)).thenReturn(mockCountQuery);
        when(mockCountQuery.getSingleResult()).thenReturn(1L); // 1L means 1 user found

        boolean exists = userService.emailExists(testEmail);

        assertTrue(exists, "Email should exist in the database");
        verify(em).createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class);
    }

    @Test
    void testApproveUser_UpdatesStatusToActive() {
        User pendingUser = new User();
        pendingUser.setId(2);
        pendingUser.setStatus("INACTIVE");

        when(em.find(User.class, 2)).thenReturn(pendingUser);

        userService.approveUser(2);

        assertEquals("ACTIVE", pendingUser.getStatus(), "Status should be updated to ACTIVE");
        verify(em, times(1)).merge(pendingUser);
    }
}