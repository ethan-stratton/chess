package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {
    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private GameService gameService;

    @BeforeEach
    public void setUp() {
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        gameService = new GameService(gameDAO, authDAO);
    }

    private String createValidAuth(String username) throws DataAccessException {
        String authToken = "valid_" + username;
        authDAO.addAuth(authToken, username);
        return authToken;
    }

    @Test
    public void listGames_Positive() throws Exception {
        String authToken = createValidAuth("user1");
        gameService.createGame(authToken, "game1");
        gameService.createGame(authToken, "game2");

        assertEquals(2, gameService.listGames(authToken).size());
    }

    @Test
    public void listGames_NegativeInvalidAuth() {
        assertThrows(DataAccessException.class, () ->
                gameService.listGames("invalid_token"));
    }

    @Test
    public void createGame_Positive() throws Exception {
        String authToken = createValidAuth("user1");
        int gameId = gameService.createGame(authToken, "testGame");

        assertTrue(gameId > 0);
        assertDoesNotThrow(() -> gameDAO.getGame(gameId));
    }

    @Test
    public void createGame_NegativeInvalidAuth() {
        assertThrows(DataAccessException.class, () ->
                gameService.createGame("invalid_token", "testGame"));
    }

    @Test
    public void joinGame_PositiveWhite() throws Exception {
        String authToken = createValidAuth("user1");
        int gameId = gameService.createGame(authToken, "testGame");

        assertEquals(0, gameService.joinGame(authToken, gameId, "WHITE"));
        GameData game = gameDAO.getGame(gameId);
        assertEquals("user1", game.whiteUsername());
    }

    @Test
    public void joinGame_PositiveBlack() throws Exception {
        String authToken = createValidAuth("user1");
        int gameId = gameService.createGame(authToken, "testGame");

        assertEquals(0, gameService.joinGame(authToken, gameId, "BLACK"));
        GameData game = gameDAO.getGame(gameId);
        assertEquals("user1", game.blackUsername());
    }

    @Test
    public void joinGame_NegativeInvalidColor() throws Exception {
        String authToken = createValidAuth("user1");
        int gameId = gameService.createGame(authToken, "testGame");

        assertEquals(1, gameService.joinGame(authToken, gameId, "GREEN"));
    }

    @Test
    public void joinGame_NegativeEmptyColor() throws Exception {
        String authToken = createValidAuth("user1");
        int gameId = gameService.createGame(authToken, "testGame");

        assertEquals(1, gameService.joinGame(authToken, gameId, ""));
    }

    @Test
    public void joinGame_NegativeSpotTaken() throws Exception {
        String authToken1 = createValidAuth("user1");
        String authToken2 = createValidAuth("user2");
        int gameId = gameService.createGame(authToken1, "testGame");

        // First user joins as white
        assertEquals(0, gameService.joinGame(authToken1, gameId, "WHITE"));

        // Second user tries to join same spot
        assertEquals(2, gameService.joinGame(authToken2, gameId, "WHITE"));
    }

    @Test
    public void joinGame_NegativeInvalidGame() throws Exception {
        String authToken = createValidAuth("user1");
        assertEquals(1, gameService.joinGame(authToken, 9999, "WHITE"));
    }

    @Test
    public void clear_Positive() throws Exception {
        String authToken = createValidAuth("user1");
        gameService.createGame(authToken, "game1");
        gameService.clear();

        assertTrue(gameDAO.listGames().isEmpty());
    }
}