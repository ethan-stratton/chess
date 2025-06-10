package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import ui.ServerFacade;
import model.GameData;

import java.util.Scanner;

import static java.lang.System.out;
import static ui.EscapeSequences.RESET_BG_COLOR;
import static ui.EscapeSequences.RESET_TEXT_COLOR;

public class Gameplay {

    ServerFacade server;
    static BoardToString boardRepr;
    ChessGame game;
    int gameID;
    static ChessGame.TeamColor color;

    public Gameplay(ServerFacade server, GameData gameData, ChessGame.TeamColor color) {
        this.server = server;
        this.game = gameData.game();
        boardRepr = new BoardToString(game);
        this.gameID = gameData.gameID();
        Gameplay.color = color;
    }

    public void run(){
        boolean inGame = true;
        out.print(RESET_TEXT_COLOR + RESET_BG_COLOR);
        boardRepr.printBoard(color, null);
        while (inGame){
            String[] input = getUserInput();
            switch(input[0]){
                case "help":
                    printHelpMenu();
                    break;
                case "redraw":
                    boardRepr.printBoard(color, null);
                    break;
                case "leave":
                    inGame = false;
                    server.leave(gameID);
                    break;
                case "move":
                    makeMove(input);
                    break;
                case "resign":
                    out.println("Will you forfeit? (y/n)");
                    Scanner scanner = new Scanner(System.in);
                    String confirmation = scanner.nextLine().trim().toLowerCase();
                    if (confirmation.equals("y") || confirmation.equals("yes")) {
                        try {
                            server.resign(gameID);
                            inGame = false;
                        } catch (Exception e) {
                            out.println("Error resigning: " + e.getMessage());
                        }
                    } else {
                        out.println("Resignation cancelled.");
                    }
                    break;
                case "highlight":
                    if (input.length == 2 && input[1].matches("[a-h][1-8]")) {
                        ChessPosition position = new ChessPosition(input[1].charAt(1) - '0', input[1].charAt(0) - ('a'-1));
                        boardRepr.printBoard(color, position);
                    }
                    else {
                        out.println("Please provide a coordinate (ex: 'a1')");
                        printHighlight();
                    }
                    break;
                default:
                    out.println("Command unrecognized");
                    printHelpMenu();
                    break;
            }
        }
        PostLogin postlogin = new PostLogin(server);
        postlogin.run();
    }

    private String[] getUserInput() {
        String prompt = color == null ? "OBSERVING" : color == ChessGame.TeamColor.WHITE ? "WHITE" : "BLACK";
        out.printf("\n[%s] >>> ", prompt);
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine().split(" ");
    }

    private void printMakeMove() {
        out.println("move <from> <to> [promotion] - make a chess move");
        out.println("  promotion options: queen, rook, bishop, knight");
        out.println("  example: move f4 f5");
        out.println("  example: move a7 a8 queen");
    }

    private void printHighlight() {
        out.println("highlight <coordinate> - highlight all legal moves for chosen piece");
    }

    private void makeMove(String[] input) {
        if (input.length >= 3 && input[1].matches("[a-h][1-8]") && input[2].matches("[a-h][1-8]")) {
            ChessPosition from = new ChessPosition(input[1].charAt(1) - '0', input[1].charAt(0) - ('a'-1));
            ChessPosition to = new ChessPosition(input[2].charAt(1) - '0', input[2].charAt(0) - ('a'-1));

            if (from.equals(to)) {
                out.println("Error: Start and end positions cannot be the same");
                return;
            }

            // promotion logic
            ChessPiece.PieceType promotion = null;
            if (input.length == 4) {
                promotion = getPieceType(input[3]);
                if (promotion == null) {
                    out.println("Error: Invalid promotion piece. Use: queen, rook, bishop, knight");
                    return;
                }
            }

            server.makeChessMove(gameID, new ChessMove(from, to, promotion));
        } else {
            out.println("Invalid format. Use: move <from> <to> [promotion]");
            printMakeMove();
        }
    }

    public ChessPiece.PieceType getPieceType(String name) {
        ChessPiece.PieceType newType = null;
        return switch (name.toUpperCase()) {
            case "QUEEN" -> ChessPiece.PieceType.QUEEN;
            case "BISHOP" -> ChessPiece.PieceType.BISHOP;
            case "KNIGHT" -> ChessPiece.PieceType.KNIGHT;
            case "ROOK" -> ChessPiece.PieceType.ROOK;
            case "PAWN" -> ChessPiece.PieceType.PAWN;
            default -> null;
        };
    }

    private void resignation() {
    }

    private void printHelpMenu() {
        out.println("redraw - redraw game board");
        out.println("leave - leave current game");
        printMakeMove();
        out.println("resign - forfeit game");
        printHighlight();
        out.println("help - show help menu");
    }
}
