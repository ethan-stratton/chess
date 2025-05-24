package chess;

import chess.ChessGame.TeamColor;
import chess.ChessPiece.PieceType;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class ChessGameStateTest {
    private ChessGame game;
    private ChessBoard board;

    @BeforeEach
    public void setup() {
        game = new ChessGame();
        board = new ChessBoard();
        game.setBoard(board);
        board.resetBoard();
    }

    private void assertPiece(int row, int col, TeamColor expectedColor, PieceType expectedType) {
        ChessPiece piece = board.getPiece(new ChessPosition(row, col));
        assertNotNull(piece, "Expected piece at " + row + "," + col);
        assertEquals(expectedColor, piece.getTeamColor());
        assertEquals(expectedType, piece.getPieceType());
    }

    @Test
    public void testInitialBoardSetup() {
        for (int i = 1; i <= 8; i++) {
            assertPiece(2, i, TeamColor.WHITE, PieceType.PAWN);
            assertPiece(7, i, TeamColor.BLACK, PieceType.PAWN);
        }

        assertPiece(1, 1, TeamColor.WHITE, PieceType.ROOK);
        assertPiece(1, 2, TeamColor.WHITE, PieceType.KNIGHT);
        assertPiece(1, 3, TeamColor.WHITE, PieceType.BISHOP);
        assertPiece(1, 4, TeamColor.WHITE, PieceType.QUEEN);
        assertPiece(1, 5, TeamColor.WHITE, PieceType.KING);
        assertPiece(1, 6, TeamColor.WHITE, PieceType.BISHOP);
        assertPiece(1, 7, TeamColor.WHITE, PieceType.KNIGHT);
        assertPiece(1, 8, TeamColor.WHITE, PieceType.ROOK);

        assertPiece(8, 1, TeamColor.BLACK, PieceType.ROOK);
        assertPiece(8, 2, TeamColor.BLACK, PieceType.KNIGHT);
        assertPiece(8, 3, TeamColor.BLACK, PieceType.BISHOP);
        assertPiece(8, 4, TeamColor.BLACK, PieceType.QUEEN);
        assertPiece(8, 5, TeamColor.BLACK, PieceType.KING);
        assertPiece(8, 6, TeamColor.BLACK, PieceType.BISHOP);
        assertPiece(8, 7, TeamColor.BLACK, PieceType.KNIGHT);
        assertPiece(8, 8, TeamColor.BLACK, PieceType.ROOK);

        // make sure middle rows are empty
        for (int row = 3; row <= 6; row++) {
            for (int col = 1; col <= 8; col++) {
                assertNull(board.getPiece(new ChessPosition(row, col)));
            }
        }
    }

    @Test
    public void testBasicMoveExecution() throws InvalidMoveException {
        // white pawn moves forward once
        ChessMove move = new ChessMove(
            new ChessPosition(2, 5), // e2
            new ChessPosition(4, 5), // e4
            null
        );
        game.makeMove(move);

        assertNull(board.getPiece(new ChessPosition(2, 5)));
        assertPiece(4, 5, TeamColor.WHITE, PieceType.PAWN);
        assertEquals(TeamColor.BLACK, game.getTeamTurn());
    }

    @Test
    public void testInvalidMove() {
        // Black tries to move first (fails)
        ChessMove invalidMove = new ChessMove(
            new ChessPosition(7, 5), // e7
            new ChessPosition(5, 5), // e5
            null
        );
        assertThrows(InvalidMoveException.class, () -> game.makeMove(invalidMove));
    }

    @Test
    public void testCaptureMove() throws InvalidMoveException {
        // capture situation
        board.resetBoard();
        board.addPiece(new ChessPosition(4, 5), new ChessPiece(TeamColor.WHITE, PieceType.PAWN));
        board.addPiece(new ChessPosition(5, 6), new ChessPiece(TeamColor.BLACK, PieceType.PAWN));

        // White pawn captures black pawn
        ChessMove captureMove = new ChessMove(
            new ChessPosition(4, 5),
            new ChessPosition(5, 6),
            null
        );
        game.makeMove(captureMove);

        assertNull(board.getPiece(new ChessPosition(4, 5)));
        assertPiece(5, 6, TeamColor.WHITE, PieceType.PAWN);
    }

//    @Test
//    public void testPromotion() throws InvalidMoveException {
//        // Set up promotion situation
//
//    }

//    @Test
//    public void testCheckDetection() {
//        // Set up check situation
//
//    }

//    @Test
//    public void testCheckmateDetection() {
//
//    }

//    @Test
//    public void testStalemateDetection() {
//
//    }

}