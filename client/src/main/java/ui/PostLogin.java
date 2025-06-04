package ui;

import java.util.*;

import model.GameData;
import chess.ChessBoard;
import chess.ChessGame;

import ui.EscapeSequences.*; //
import static ui.EscapeSequences.*;

import static java.lang.System.out;


public class PostLogin {

    ServerFacade server;

    List<GameData> games;

    public PostLogin (ServerFacade server) {
        this.server = server;
        games = new ArrayList<>();
    }

    public void run() {
        boolean loggedIn = true;
        out.print(RESET_TEXT_COLOR + RESET_BG_COLOR);

        while (loggedIn) {
            String[] input = getUserInput();
            if (input[0].equals("login")) {
                out.println("You are already logged in. Use 'logout' first.");
                continue;
            }
            switch (input[0]) {
                case "quit":
                    return;
                case "help":
                    printHelpMenu();
                    break;
                case "logout":
                    server.logout();
                    loggedIn = false;
                    break;
                case "list":
                    //out.println(server.listGames());
                    //server.printGamesFormatted();
                    refreshGames();
                    printGames();
                    break;
                case "create":
                    if (input.length != 2) {
                        out.println("Please provide a name");
                        printCreate();
                        break;
                    }
                    int gameID = server.createGame(input[1]);
                    //out.printf("Created game, ID: %d%n", gameID);
                    break;
                case "join":
                    if (input.length != 3) {
                        out.println("Usage: join <LIST_ID> [WHITE|BLACK]");
                        out.println("Note: Use the LIST_ID (first column) not the gameID");
                        break;
                    }
                    try {
                        refreshGames();
                        int listIndex = Integer.parseInt(input[1]);
                        GameData game = games.get(listIndex);
                        if (server.joinGame(game.gameID(), input[2].toUpperCase())) {
                            out.println("Successfully joined game " + game.gameName());

                            //find user color
                            ChessGame.TeamColor color = input[2].equalsIgnoreCase("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
                            //according to user color, print correct board
                            new BoardToString(game.game().getBoard(), color).printBoard();
                        } else {
                            out.println("Incorrect Usage: join <LIST_ID> [WHITE|BLACK]");
                            //out.println("Failed to join game");
                        }
                        //different catch blocks (list out of bounds error)
                    } catch (IndexOutOfBoundsException e){
                        out.println("List ID out of Bounds");
                    } catch (Exception e) {
                        out.println("Incorrect Usage: join <LIST_ID> [WHITE|BLACK]");
                        //out.println("Incorrect Usage: " + e.getMessage());
                    }
                    break;
                case "observe":
                    if (input.length != 2) {
                        out.println("Please provide a game ID");
                        printObserve();
                        break;
                    }
                    int listIndex = Integer.parseInt(input[1]);
                    if (listIndex < 0 || listIndex >= games.size()) {
                        out.println("Invalid game index. Use the ID from the 'list' command.");
                        break;
                    }
                    GameData observeGame = games.get(Integer.parseInt(input[1]));
                    if (server.joinGame(observeGame.gameID(), null)) {
                        out.println("You are now observing game "+ observeGame.gameName());
                        new BoardToString(observeGame.game().getBoard(), ChessGame.TeamColor.WHITE).printBoard();
                        break;
                    } else {
                        out.println("Game does not exist");
                        printObserve();
                        break;
                    }
                default:
                    out.println("Command not recognized, please try again:");
                    printHelpMenu();
                    break;
            }
        }

        PreLogin prelogin = new PreLogin(server);
        prelogin.run();
    }

    private String[] getUserInput() {
        out.print("\n[LOGGED IN] >>> ");
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine().split(" ");
    }

    private void refreshGames() {
        games = new ArrayList<>();
        HashSet<GameData> gameList = server.listGames();
        games.addAll(gameList);
    }

    private void printGames() {
        out.println("ID  Game Name          White User       Black User");
        out.println("----------------------------------------------------------");
        for (int i = 1; i <= games.size(); i++) {
            GameData game = games.get(i-1);
            String whiteUser = game.whiteUsername() != null ? game.whiteUsername() : "open";
            String blackUser = game.blackUsername() != null ? game.blackUsername() : "open";
            out.printf("%-3d %-18s %-16s %-16s%n",
                    i,
                    game.gameName(),
                    whiteUser,
                    blackUser);
        }
    }

    private void printHelpMenu() {
        out.println(" --- Help Menu --- ");
        out.println("Note: Use LIST_ID (from 'list' command) when joining games");
        printCreate();
        out.println("list - list all games with IDs");
        printJoin();
        printObserve();
        out.println("logout - log out of current user");
        out.println("quit - stop playing");
        out.println("help - show this menu");
    }

    private void printCreate() {
        out.println("create <NAME> - create a new game");
    }

    private void printJoin() {
        out.println("join <ID> [WHITE|BLACK] - join a game as color");
    }

    private void printObserve() {
        out.println("observe <ID> - observe a game");
    }


}