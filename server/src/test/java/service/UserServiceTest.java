package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private UserDAO userDAO;
    private AuthDAO authDAO;
    private UserAuthService userService;

    @BeforeEach
    public void setUp() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserAuthService(userDAO, authDAO);
    }

    private UserData createTestUser(String username, String password, String email) {
        return new UserData(username, password, email);
    }

    @Test
    public void register() throws Exception {
        UserData newUser = createTestUser("testUser", "password", "test@email.com");
        AuthData authData = userService.createUser(newUser);

        assertNotNull(authData);
        assertNotNull(authData.authToken());
        assertEquals("testUser", authData.username());

        UserData storedUser = userDAO.getUser("testUser");
        assertEquals("testUser", storedUser.username());
    }

    @Test
    public void registerDuplicateUser() throws Exception {
        UserData user = createTestUser("testUser", "password", "test@email.com");
        userService.createUser(user);

        assertThrows(DataAccessException.class, () ->
                userService.createUser(user));
    }

    @Test
    public void login() throws Exception {
        UserData user = createTestUser("testUser", "password", "test@email.com");
        userService.createUser(user);

        AuthData authData = userService.loginUser(user);
        assertNotNull(authData);
        assertEquals("testUser", authData.username());
    }

    @Test
    public void loginWrongPassword() throws Exception {
        UserData realUser = createTestUser("testUser", "password", "test@email.com");
        userService.createUser(realUser);

        UserData wrongPassUser = createTestUser("testUser", "wrongPass", "test@email.com");
        assertThrows(DataAccessException.class, () ->
                userService.loginUser(wrongPassUser));
    }

    @Test
    public void loginNonexistentUser() {
        UserData fakeUser = createTestUser("noSuchUser", "password", "email@test.com");
        assertThrows(DataAccessException.class, () ->
                userService.loginUser(fakeUser));
    }

    @Test
    public void logout() throws Exception {
        UserData user = createTestUser("testUser", "password", "test@email.com");
        AuthData authData = userService.createUser(user);

        assertDoesNotThrow(() -> userService.logoutUser(authData.authToken()));

        assertThrows(DataAccessException.class, () ->
                authDAO.getAuth(authData.authToken()));
    }

    @Test
    public void logoutInvalidToken() { // remember to figure this out later
        assertThrows(DataAccessException.class, () ->
                userService.logoutUser("invalidToken123"));
    }

    @Test
    public void clear() throws Exception {
        userService.createUser(createTestUser("user1", "pass1", "email1@test.com"));
        userService.createUser(createTestUser("user2", "pass2", "email2@test.com"));

        userService.clear();

        assertThrows(DataAccessException.class, () -> userDAO.getUser("user1"));
    }
}