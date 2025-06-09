package ui;

import chess.ChessGame;
import chess.ChessMove;
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
                    if (input.length == 3 && input[1].matches("[a-h][1-8]") && input[2].matches("[a-h][1-8]")) {
                        ChessPosition from = new ChessPosition(input[1].charAt(1) - '0', input[1].charAt(0) - ('a'-1));
                        ChessPosition to = new ChessPosition(input[2].charAt(1) - '0',input[2].charAt(0) - ('a'-1));
                        server.makeChessMove(gameID, new ChessMove(from, to, null));
                        break;
                    }
                    else {
                        out.println("Please provide a <to> and <from> coordinate (ex: 'a1 b2')");
                        printMakeMove();
                    }
                    break;
                case "resign":
                    out.println("Will you forfeit? (y/n)");
                    Scanner scanner = new Scanner(System.in);
                    String confirmation = scanner.nextLine().trim().toLowerCase();
                    if (confirmation.equals("y") || confirmation.equals("yes")) {
                        server.resign(gameID);
                        inGame = false;
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
        out.println("move <from> <to> - make a chess move");
    }

    private void printHighlight() {
        out.println("highlight <coordinate> - highlight all legal moves for chosen piece");
    }

    private void makeMove(ChessPosition from, ChessPosition to) {

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
