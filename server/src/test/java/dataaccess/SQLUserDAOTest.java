package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

public class SQLUserDAOTest {
    private static SQLUserDAO userDAO;

    @BeforeAll
    public static void init() throws DataAccessException {
        userDAO = new SQLUserDAO();
    }

    @BeforeEach
    public void setup() {
        userDAO.clear();
    }

    @Test
    public void createUserSuccessfullyCreatesNewUser() throws DataAccessException {
        userDAO.createUser("newUser", "password123", "new@email.com");
        UserData retrieved = userDAO.getUser("newUser");
        assertEquals("newUser", retrieved.username());
        assertEquals("new@email.com", retrieved.email());
        assertTrue(BCrypt.checkpw("password123", retrieved.password()));
    }

    @Test
    public void createUserThrowsWhenUsernameExists() throws DataAccessException {
        userDAO.createUser("duplicate", "pass1", "email1@test.com");
        assertThrows(DataAccessException.class, () -> {
            userDAO.createUser("duplicate", "pass2", "email2@test.com");
        });
    }

    @Test
    public void createUserWithUserData() throws DataAccessException {
        UserData user = new UserData("userData", "password", "user@data.com");
        userDAO.createUser(user);
        UserData retrieved = userDAO.getUser("userData");
        assertEquals(user.username(), retrieved.username());
        assertEquals(user.email(), retrieved.email());
        assertTrue(BCrypt.checkpw(user.password(), retrieved.password()));
    }

    //create user with user data which throws when the same username exists
    @Test
    public void createUserThrows() throws DataAccessException {
        userDAO.createUser(new UserData("dupUser", "pass1", "email1@test.com"));
        assertThrows(DataAccessException.class, () -> {
            userDAO.createUser(new UserData("dupUser", "pass2", "email2@test.com"));
        });
    }

    @Test
    public void getUserReturnCorrectUserData() throws DataAccessException {
        userDAO.createUser("getUser", "password", "get@user.com");
        UserData retrieved = userDAO.getUser("getUser");
        assertEquals("getUser", retrieved.username());
        assertEquals("get@user.com", retrieved.email());
    }

    @Test
    public void getUserUserNotFound() {
        assertThrows(DataAccessException.class, () -> {
            userDAO.getUser("nonexistent");
        });
    }

    @Test
    public void authenticateReturnsTrue() throws DataAccessException {
        String plainPassword = "correctPass";
        userDAO.createUser("authUser", plainPassword, "auth@test.com");
        assertTrue(userDAO.authenticateUser("authUser", plainPassword));
    }

    @Test
    public void authenticateReturnsFalse() throws DataAccessException {
        userDAO.createUser("authUser2", "rightPass", "auth2@test.com");
        assertFalse(userDAO.authenticateUser("authUser2", "wrongPass"));
    }

    @Test
    public void clearRemovesAllUsers() throws DataAccessException {
        userDAO.createUser("user1", "pass1", "email1@test.com");
        userDAO.createUser("user2", "pass2", "email2@test.com");
        userDAO.clear();
        assertThrows(DataAccessException.class, () -> userDAO.getUser("user1"));
        assertThrows(DataAccessException.class, () -> userDAO.getUser("user2"));
    }
}