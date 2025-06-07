package ui;

import chess.ChessGame;
import chess.ChessPosition;
import ui.ServerFacade;
import model.GameData;

import java.util.Scanner;

import static java.lang.System.out;
import static ui.EscapeSequences.RESET_BG_COLOR;
import static ui.EscapeSequences.RESET_TEXT_COLOR;

public class Gameplay {

    ServerFacade server;
    BoardToString boardRepr;
    ChessGame game;
    ChessGame.TeamColor color;

    public Gameplay(ServerFacade server, ChessGame game, ChessGame.TeamColor color) {
        this.server = server;
        this.boardRepr = new BoardToString(game);
        this.game = game;
        this.color = color;
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
                    break;
                case "move": //todo
                    printMakeMove();
                    break;
                case "resign": //todo
                    resignation();
                    break;
                case "highlight": //todo
                    printHighlight();
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
