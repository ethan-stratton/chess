//package dataaccess;
//
//import model.UserData;
//import org.junit.jupiter.api.*;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class SQLUserDAOTest {
//    private SQLUserDAO userDAO;
//
//    @BeforeEach
//    public void setUp() throws DataAccessException {
//        userDAO = new SQLUserDAO();
//        userDAO.clear(); // clear before each test
//    }
//
//    @Test
//    public void positiveCreateUser() throws DataAccessException {
//        userDAO.createUser("testuser", "password", "test@email.com");
//
//        UserData user = userDAO.getUser("testuser");
//        assertEquals("testuser", user.username());
//        assertEquals("test@email.com", user.email());
//    }
//
//    @Test
//    public void negativeCreateUser() {
//        // create the user
//        assertDoesNotThrow(() -> userDAO.createUser("testuser", "password", "test@email.com"));
//
//        // create duplicate
//        assertThrows(DataAccessException.class, () -> {
//            userDAO.createUser("testuser", "password2", "test2@email.com");
//        });
//    }
//
//    @Test
//    public void positiveAuthenticate() throws DataAccessException {
//        userDAO.createUser("testuser", "password", "test@email.com");
//        assertTrue(userDAO.authenticateUser("testuser", "password"));
//    }
//
//    @Test
//    public void negativeAuthenticate() throws DataAccessException {
//        userDAO.createUser("testuser", "password", "test@email.com");
//        assertFalse(userDAO.authenticateUser("testuser", "wrongpassword"));
//        assertFalse(userDAO.authenticateUser("nonexistent", "password"));
//    }
//
//    @Test
//    public void clearTest() throws DataAccessException {
//        userDAO.createUser("testuser", "password", "test@email.com");
//        userDAO.clear();
//        assertThrows(DataAccessException.class, () -> userDAO.getUser("testuser"));
//    }
//}