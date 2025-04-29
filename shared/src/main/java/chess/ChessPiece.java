package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return switch (getPieceType()) {
            case BISHOP -> bishopMoves(board, myPosition);
            default -> throw new RuntimeException("Not implemented");
        };
    }

    /**
     *
     * @param row the row
     * @param col the column
     * @return whether piece value is outside the game board or not
     */
    private boolean isValidPosition(int row, int col){
        return (row >= 1 && row <= 8 && col >= 1 && col <= 8);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    /**
     *
     * @param board the chessboard object
     * @param myPosition piece position we are examining
     * @return Collection of valid chess moves the player can use
     */
    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> validMoves = new ArrayList<>();

        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        //specific piece logic: find the direction they can move in,
        // check if piece is there (most pieces can't jump)
        // add value to list
        int[][] directions = {
                {1,1},
                {-1,1},
                {-1,-1},
                {1,-1},
        };
        for (int direction[] : directions){
            int moveRowByOne = direction[0];
            int moveColByOne = direction[1];
            int newRow = row + moveRowByOne;
            int newCol = col + moveColByOne;

            while (isValidPosition(newRow,newCol)){
                ChessPosition newMove = new ChessPosition(newRow, newCol);
                ChessPiece obstructingPiece = board.getPiece(newMove);//get the piece at the new move location (if there is one)
                if (obstructingPiece == null){
                    validMoves.add(new ChessMove(myPosition, newMove, null));
                    //System.out.println(newRow + " " + newCol);
                    newRow += moveRowByOne;
                    newCol += moveColByOne;
                }
                else if (obstructingPiece.getTeamColor() != this.getTeamColor()){
                    validMoves.add(new ChessMove(myPosition, newMove, null));
                    break;
                }
                else {
                    break;
                }

            }

        }

        return validMoves;
    }
}
