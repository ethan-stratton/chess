package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class SQLGameDAOTest {
    private static SQLGameDAO gameDAO;

    @BeforeAll
    public static void init() throws DataAccessException {
        gameDAO = new SQLGameDAO();
    }

    @BeforeEach
    public void setup() {
        gameDAO.clear();
    }

    @Test
    public void testCreateAndGetGame() throws DataAccessException {
        ChessGame game = new ChessGame();
        gameDAO.createGame(1, "whiteUser", "blackUser", "testGame", game);

        GameData retrievedGame = gameDAO.getGame(1);

        assertEquals(1, retrievedGame.gameID());
        assertEquals("whiteUser", retrievedGame.whiteUsername());
        assertEquals("blackUser", retrievedGame.blackUsername());
        assertEquals("testGame", retrievedGame.gameName());
        assertNotNull(retrievedGame.game());
    }

    @Test
    public void testGetGame() {
        assertThrows(DataAccessException.class, () -> gameDAO.getGame(999));
    }

    @Test
    public void testCheckGameExists() {
        gameDAO.createGame(1, "whiteUser", "blackUser", "testGame", new ChessGame());
        assertTrue(gameDAO.checkGameExists(1));
    }

    @Test
    public void testCheckGameExistsBadID() {
        assertFalse(gameDAO.checkGameExists(999));
    }

    @Test
    public void testListGames() {
        assertEquals(0, gameDAO.listGames().size());

        gameDAO.createGame(1, "white1", "black1", "game1", new ChessGame());
        gameDAO.createGame(2, "white2", "black2", "game2", new ChessGame());

        assertEquals(2, gameDAO.listGames().size());
    }

    @Test
    public void testUpdateGame() throws DataAccessException {
        ChessGame game = new ChessGame();
        gameDAO.createGame(1, "oldWhite", "oldBlack", "oldName", game);

        ChessGame updatedGame = new ChessGame();
        GameData updatedData = new GameData(1, "newWhite", "newBlack", "newName", updatedGame);
        gameDAO.updateGame(updatedData);

        GameData retrieved = gameDAO.getGame(1);
        assertEquals("newWhite", retrieved.whiteUsername());
        assertEquals("newBlack", retrieved.blackUsername());
        assertEquals("newName", retrieved.gameName());
    }

    @Test
    public void testClear() {
        gameDAO.createGame(1, "white", "black", "game", new ChessGame());
        gameDAO.clear();

        assertEquals(0, gameDAO.listGames().size());
        assertFalse(gameDAO.checkGameExists(1));
    }

    @Test
    public void testCreateGameWithGameDataObject() {
        GameData gameData = new GameData(1, "white", "black", "game", new ChessGame());
        gameDAO.createGame(gameData);

        assertTrue(gameDAO.checkGameExists(1));
    }

    @Test
    public void testGameSerializationDeserialization() throws DataAccessException {
        ChessGame originalGame = new ChessGame();
        // make some modifications to the game to test serialization
        originalGame.setTeamTurn(ChessGame.TeamColor.BLACK);

        gameDAO.createGame(1, "white", "black", "game", originalGame);
        GameData retrieved = gameDAO.getGame(1);

        assertEquals(ChessGame.TeamColor.BLACK, retrieved.game().getTeamTurn());
    }
}