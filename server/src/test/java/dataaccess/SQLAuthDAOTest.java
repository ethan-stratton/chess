package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class SQLAuthDAOTest {
    private static SQLAuthDAO authDAO;
    private static SQLUserDAO userDAO;

    @BeforeAll
    public static void init() throws DataAccessException {
        authDAO = new SQLAuthDAO();
        userDAO = new SQLUserDAO();
    }

    @BeforeEach
    public void setup() throws DataAccessException {
        authDAO.clear();
        userDAO.clear();

        userDAO.createUser("testUser", "password", "test@email.com");
        userDAO.createUser("newUser", "password", "new@email.com");
        userDAO.createUser("getUser", "password", "get@email.com");
        userDAO.createUser("deleteUser", "password", "delete@email.com");
    }

    @Test
    public void addAuthWithAuthData() throws DataAccessException {
        AuthData authData = new AuthData("testUser", "validToken123");
        authDAO.addAuth(authData);

        AuthData retrieved = authDAO.getAuth("validToken123");
        assertEquals("testUser", retrieved.username());
        assertEquals("validToken123", retrieved.authToken());
    }

    @Test
    public void addAuthWithAuthDataDuplicateToken() throws DataAccessException {
        AuthData authData1 = new AuthData("testUser", "duplicateToken");
        AuthData authData2 = new AuthData("newUser", "duplicateToken");

        authDAO.addAuth(authData1);
        assertThrows(DataAccessException.class, () -> authDAO.addAuth(authData2));
    }

    @Test
    public void addAuthWithTokenAndUsername() throws DataAccessException {
        authDAO.addAuth("newToken456", "newUser");

        AuthData retrieved = authDAO.getAuth("newToken456");
        assertEquals("newUser", retrieved.username());
        assertEquals("newToken456", retrieved.authToken());
    }

    @Test
    public void addAuthWithTokenAndUsernameDuplicateToken() throws DataAccessException {
        authDAO.addAuth("dupToken", "testUser");
        assertThrows(DataAccessException.class, () -> authDAO.addAuth("dupToken", "newUser"));
    }

    @Test
    public void getAuthReturnsCorrectData() throws DataAccessException {
        authDAO.addAuth("getToken", "getUser");
        AuthData authData = authDAO.getAuth("getToken");

        assertEquals("getUser", authData.username());
        assertEquals("getToken", authData.authToken());
    }

    @Test
    public void getAuthTokenNotFound() {
        assertThrows(DataAccessException.class, () -> authDAO.getAuth("nonexistentToken"));
    }

    @Test
    public void deleteAuthRemovesToken() throws DataAccessException {
        authDAO.addAuth("toDelete", "deleteUser");
        authDAO.deleteAuth("toDelete");

        assertThrows(DataAccessException.class, () -> authDAO.getAuth("toDelete"));
    }

    @Test
    public void deleteAuthThrowsWhenTokenNotFound() {
        assertThrows(DataAccessException.class, () -> authDAO.deleteAuth("nonexistentToken"));
    }

    @Test
    public void clearRemovesAllAuthTokens() throws DataAccessException {
        authDAO.addAuth("token1", "testUser");
        authDAO.addAuth("token2", "newUser");

        authDAO.clear();

        assertThrows(DataAccessException.class, () -> authDAO.getAuth("token1"));
        assertThrows(DataAccessException.class, () -> authDAO.getAuth("token2"));
    }
}