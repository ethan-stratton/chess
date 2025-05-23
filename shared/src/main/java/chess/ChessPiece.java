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
            case KING -> kingMoves(board, myPosition);
            case ROOK -> rookMoves(board, myPosition);
            case QUEEN -> queenMoves(board, myPosition);
            case KNIGHT -> knightMoves(board, myPosition);
            case PAWN -> pawnMoves(board, myPosition);
            //default -> throw new RuntimeException("Not implemented");
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
     * @return Collection of valid bishop moves the player can use
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

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        int[][] directions = {
                {1,0},
                {0,1},
                {-1,0},
                {0,-1},
                {1,1},
                {-1,1},
                {-1,-1},
                {1,-1}
        };
        for (int direction[] : directions){
            int moveRowByOne = direction[0];
            int moveColByOne = direction[1];
            int newRow = row + moveRowByOne;
            int newCol = col + moveColByOne;
            if (isValidPosition(newRow,newCol)){
                ChessPosition newMove = new ChessPosition(newRow, newCol);
                ChessPiece obstructingPiece = board.getPiece(newMove);
                if (obstructingPiece == null || obstructingPiece.getTeamColor() != this.getTeamColor()){
                    validMoves.add(new ChessMove(myPosition, newMove, null));
                }
            }
        }
        return validMoves;
    }

    /**
     *
     * @param board the chessboard object
     * @param myPosition piece position we are examining
     * @return Collection of valid bishop moves the player can use
     */
    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> validMoves = new ArrayList<>();

        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        int[][] directions = {
                {-1,0},
                {0,1},
                {0,-1},
                {1,0},
        };
        for (int direction[] : directions){
            int moveRowByOne = direction[0];
            int moveColByOne = direction[1];
            int newRow = row + moveRowByOne;
            int newCol = col + moveColByOne;

            while (isValidPosition(newRow,newCol)){
                ChessPosition newMove = new ChessPosition(newRow, newCol);
                ChessPiece obstructingPiece = board.getPiece(newMove);
                if (obstructingPiece == null){
                    //System.out.println(newRow + " " + newCol);
                    validMoves.add(new ChessMove(myPosition, newMove, null));
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
    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> validMoves = new ArrayList<>();
        validMoves.addAll(this.rookMoves(board, myPosition));
        validMoves.addAll(this.bishopMoves(board, myPosition));
        return validMoves;
    }
    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition){
        Collection<ChessMove> validMoves = new ArrayList<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        //for each direction, move two, then left and right by one
        //it will always be a valid move (knight can jump and capture)
        int[][] knightJumps = {
                {2, 1},
                {2, -1},
                {-2, 1},
                {-2, -1},
                {1, 2},
                {-1, 2},
                {1, -2},
                {-1, -2}
        };
        for (int jump[] : knightJumps){
          //if isValidPosition, get new move
            int newRow = row + jump[0];
            int newCol = col + jump[1];
            if (isValidPosition(newRow, newCol)){
                ChessPosition newPosition = new ChessPosition(newRow,newCol);
                ChessPiece obstructingPiece = board.getPiece(newPosition);
                if (obstructingPiece == null || obstructingPiece.getTeamColor() != this.getTeamColor()) {
                    validMoves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }
        return validMoves;
    }


    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        // white pawns move up, black pawns move down
        // can move one square forward if unblocked
        // can move two squares forward from first position if unblocked
        // can capture diagonally forward
        // can promote at edge of opposite board
        Collection<ChessMove> validMoves = new ArrayList<>();
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        int direction;
        int startRow;
        int promotionRow;

        //determine direction
        if (this.getTeamColor() == ChessGame.TeamColor.WHITE){
            direction = 1; //white
            startRow = 2;
            promotionRow = 8;
        }
        else{
            direction = -1; //black
            startRow = 7;
            promotionRow = 1;
        }

        int newRow = row + direction;
        if (isValidPosition(newRow,col)){
            ChessPosition goForward = new ChessPosition(newRow, col);
            if (board.getPiece(goForward) == null){
                if (newRow == promotionRow){
                    //promote piece
                    validMoves.add(new ChessMove(myPosition, goForward, PieceType.KNIGHT));
                    validMoves.add(new ChessMove(myPosition, goForward, PieceType.QUEEN));
                    validMoves.add(new ChessMove(myPosition, goForward, PieceType.ROOK));
                    validMoves.add(new ChessMove(myPosition, goForward, PieceType.BISHOP));
                } else {
                    validMoves.add(new ChessMove(myPosition, goForward, null));
                }

                // move twice
                if (row == startRow) {
                    int doubleMove = row + 2 * direction;
                    ChessPosition forwardTwo = new ChessPosition(doubleMove, col);
                    if (board.getPiece(forwardTwo) == null) {
                        validMoves.add(new ChessMove(myPosition, forwardTwo, null));
                    }
                }
            }
        }

        //capture logic (diagonally)
        //get the position {1,1}, {1,-1}.
        //see if the move is valid (in bounds), see if there's a piece there
        //if there's a piece, add it as a valid move
        //if we end up in rows 1 or 8, we can promote
        int[][] capturePosition = {
                {direction,1},
                {direction,-1}
        };
        for (int position[] : capturePosition){
            int captureRow = row + position[0];
            int captureCol = col + position[1];
            if (isValidPosition(captureRow, captureCol)){
                ChessPosition newPosition = new ChessPosition(captureRow,captureCol);
                ChessPiece obstructingPiece = board.getPiece(newPosition);
                if (obstructingPiece != null && obstructingPiece.getTeamColor() != this.getTeamColor()) {
                    if (newRow == promotionRow){
                        validMoves.add(new ChessMove(myPosition, newPosition, PieceType.KNIGHT));
                        validMoves.add(new ChessMove(myPosition, newPosition, PieceType.QUEEN));
                        validMoves.add(new ChessMove(myPosition, newPosition, PieceType.ROOK));
                        validMoves.add(new ChessMove(myPosition, newPosition, PieceType.BISHOP));
                    } else {
                        validMoves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }

        }
        return validMoves;
    }
}
