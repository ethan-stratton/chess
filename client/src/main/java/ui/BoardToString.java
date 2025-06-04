package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import static java.lang.System.out;
import static ui.EscapeSequences.*;
import static ui.EscapeSequences.SET_TEXT_COLOR_BLACK;

public class BoardToString {

    ChessBoard board;

    BoardToString(ChessBoard board) {
        this.board = board;
    }

    void printBoard() {
        StringBuilder output = new StringBuilder();
        output.append(SET_TEXT_BOLD);

        boolean reversed = true;
        for (int j = 0; j < 2; j++) {

            output.append(startingRow(reversed));

            for (int i = 8; i > 0; i--) {
                int row = !reversed ? i : (i * -1) + 9;
                output.append(boardRow(row, reversed));
            }

            output.append(startingRow(reversed));
            if (j < 1) output.append("\n");

            reversed = false;
        }
        output.append(RESET_TEXT_BOLD_FAINT);
        out.println(output);
    }

    private String startingRow(boolean reversed) {
        StringBuilder output = new StringBuilder();
        output.append(SET_BG_COLOR_BLACK);
        output.append(SET_TEXT_COLOR_BLUE);
        output.append(!reversed ? "    a  b  c  d  e  f  g  h    " : "    h  g  f  e  d  c  b  a    ");
        output.append(RESET_BG_COLOR);
        output.append(RESET_TEXT_COLOR);
        output.append("\n");
        return output.toString();
    }

    private String boardRow(int row, boolean reversed) {
        StringBuilder output = new StringBuilder();
        output.append(SET_BG_COLOR_BLACK);
        output.append(SET_TEXT_COLOR_BLUE);
        output.append(" %d ".formatted(row));

        for (int i = 1; i < 9; i++) {
            int column = !reversed ? i : (i * -1) + 9;
            output.append(squareColor(row, column));
            output.append(piece(row, column));
        }


        output.append(SET_BG_COLOR_BLACK);
        output.append(SET_TEXT_COLOR_BLUE);
        output.append(" %d ".formatted(row));
        output.append(RESET_BG_COLOR);
        output.append(RESET_TEXT_COLOR);

        output.append("\n");
        return output.toString();
    }

    private String squareColor(int row, int column) {
        if (Math.ceilMod(row, 2) == 0) {
            if (Math.ceilMod(column, 2) == 0) {
                return SET_BG_COLOR_RED;
            } else {
                return SET_BG_COLOR_LIGHT_GREY;
            }
        } else {
            if (Math.ceilMod(column, 2) == 0) {
                return SET_BG_COLOR_LIGHT_GREY;
            } else {
                return SET_BG_COLOR_RED;
            }
        }
    }

    private String piece(int row, int column) {
        StringBuilder output = new StringBuilder();
        ChessPosition position = new ChessPosition(row, column);
        ChessPiece piece = board.getPiece(position);

        if (piece != null) {
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                output.append(SET_TEXT_COLOR_WHITE);
            } else {
                output.append(SET_TEXT_COLOR_BLACK);
            }

            switch (piece.getPieceType()) {
                case QUEEN -> output.append(" Q ");
                case KING -> output.append(" K ");
                case BISHOP -> output.append(" B ");
                case KNIGHT -> output.append(" N ");
                case ROOK -> output.append(" R ");
                case PAWN -> output.append(" P ");
            }
        } else {
            output.append("   ");
        }

        return output.toString();
    }
}