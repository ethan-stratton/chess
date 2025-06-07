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
        boolean inGame = false;
        out.print(RESET_TEXT_COLOR + RESET_BG_COLOR);

        while (loggedIn && !inGame) {
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
                    out.printf("Created game: %s%n", input[1]);
                    break;
                case "join":
                    if (input.length != 3) {
                        out.println(SET_TEXT_COLOR_RED + "Error: Missing arguments" + RESET_TEXT_COLOR);
                        out.println("Usage: join <LIST_ID> [WHITE|BLACK|<empty>]");
                        out.println("Example: join 1 WHITE  (to join as white)");
                        break;
                    }
                    try {
                        refreshGames();
                        int listIndex = Integer.parseInt(input[1]) - 1;
                        if (listIndex < 0 || listIndex >= games.size()) {
                            out.println(SET_TEXT_COLOR_RED + "Error: Invalid LIST_ID" + RESET_TEXT_COLOR);
                            out.println("Valid LIST_IDs are between 1 and " + games.size());
                            printGames(); // show the list again for reference
                            break;
                        }
                        GameData game = games.get(listIndex);
                        String colorInput = input[2].toUpperCase();
                        if (!colorInput.equalsIgnoreCase("WHITE") && !colorInput.equalsIgnoreCase("BLACK")) {
                            out.println(SET_TEXT_COLOR_RED + "Error: Invalid color" + RESET_TEXT_COLOR);
                            out.println("Please specify either WHITE or BLACK");
                            break;
                        }

                        if (server.joinGame(game.gameID(), input[2].toUpperCase())) {
                            out.println(SET_TEXT_COLOR_GREEN + "Successfully joined game " + RESET_TEXT_COLOR + game.gameName());
                            ChessGame.TeamColor color = input[2].equalsIgnoreCase("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
                            //todo
                            //new BoardToString(game.game().getBoard(), color).printBoard();
                            //instead of Board representation (delete above line), we need:
                            // the server to send a test message to make sure it works: server.sendWSMessage("Test Join");
                            inGame = true;
                            Gameplay gameplay = new Gameplay(server, game.game(), color);
                            gameplay.run();
                        } else {
                            if (colorInput.equals("WHITE") && game.whiteUsername() != null) {
                                out.println(SET_TEXT_COLOR_RED + "Error: White position already taken by " + RESET_TEXT_COLOR + game.whiteUsername());
                            } else if (colorInput.equals("BLACK") && game.blackUsername() != null) {
                                out.println(SET_TEXT_COLOR_RED + "Error: Black position already taken by "+ RESET_TEXT_COLOR + game.blackUsername());
                            } else {
                                out.println(SET_TEXT_COLOR_RED + "Error: Could not join game" + RESET_TEXT_COLOR);
                            }
                        }
                    } catch (NumberFormatException e){
                        out.println(SET_TEXT_COLOR_RED + "Error: LIST_ID must be a number" + RESET_TEXT_COLOR);
                        out.println("Example: join 1 WHITE");
                    } catch (Exception e) {
                        out.println(SET_TEXT_COLOR_RED +"Something Went Wrong: "+ RESET_TEXT_COLOR + e.getMessage());
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
                    GameData observeGame = games.get(Integer.parseInt(input[1]) - 1);
                    if (server.joinGame(observeGame.gameID(), null)) {
                        out.println("You are now observing game "+ observeGame.gameName());
                        //todo delete below line
                        //new BoardToString(observeGame.game().getBoard(), ChessGame.TeamColor.WHITE).printBoard();

                        // instead implement something similar to joinGame:
                        inGame = true;
                        //Gameplay gameplay = new Gameplay(server, observeGame.game(), null);
                        //gameplay.run();
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

        if (!loggedIn) {
            PreLogin prelogin = new PreLogin(server);
            prelogin.run();
        }
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