package client;

import org.junit.jupiter.api.*;
import server.Server;
import ui.ServerFacade;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private ServerFacade facade;
    static int port;

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {

        server.stop();
    }

    @BeforeEach
    void setup() {
        server.clearDB();

        facade = new ServerFacade();
        facade.setServerPort(port);

        System.out.println("Testing Server on Port:" + port);
    }

    @AfterEach
    void cleanup() {
        server.clearDB();
    }

    @Test
    public void registerUser() {
        assertTrue(facade.register("username", "password", "email"));
    }

    @Test
    public void registerInvalid() {
        facade.register("username", "password", "email");
        assertFalse(facade.register("username", "password", "email"));
    }

    @Test
    public void login() {
        facade.register("username", "password", "email");
        assertTrue(facade.login("username", "password"));
    }

    @Test
    public void loginInvalid() {
        facade.register("username", "password", "email");
        assertFalse(facade.login("username", "pass"));
    }

    @Test
    public void logout() {
        facade.register("username", "password", "email");
        assertTrue(facade.logout());
    }

    @Test
    public void logoutInvalid() {
        assertFalse(facade.logout());
    }

    @Test
    public void createGame() {
        facade.register("username", "password", "email");
        assertTrue(facade.createGame("gameName") >= 0);
    }

    @Test
    public void createGameInvalid() {
        assertEquals(-1, facade.createGame("gameName"));
    }

    @Test
    public void listGames() {
        System.out.println("Initial games: " + facade.listGames().size());

        facade.register("username", "password", "email");
        System.out.println("Games after register: " + facade.listGames().size());

        facade.createGame("gameName");
        System.out.println("Games after create: " + facade.listGames().size());

        assertEquals(1, facade.listGames().size());
    }

    @Test
    public void listGamesInvalid() {
        assertEquals(facade.listGames(), HashSet.newHashSet(8));
    }

    @Test
    public void joinGame() {
        facade.register("username", "password", "email");
        int id = facade.createGame("gameName");
        assertTrue(facade.joinGame(id, "WHITE"));
    }

    @Test
    public void joinGameInvalid() {
        facade.register("username", "password", "email");
        int id = facade.createGame("gameName");
        facade.joinGame(id, "WHITE");
        assertFalse(facade.joinGame(id, "WHITE"));
    }

}
