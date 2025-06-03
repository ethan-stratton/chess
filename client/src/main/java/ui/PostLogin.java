package ui;

import java.util.*;

import model.GameData;

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
                    out.printf("Created game, ID: %d%n", gameID);
                    break;
                case "join":
                    if (input.length != 3) {
                        out.println("Please provide a game ID and color choice");
                        printJoin();
                        break;
                    }
                    if (server.joinGame(games.get(Integer.parseInt(input[1])).gameID(), input[2].toUpperCase())) {
                        out.println("You have joined the game");
                        break;
                    } else {
                        out.println("Game does not exist or color taken");
                        printJoin();
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
        for (int i = 0; i < games.size(); i++) {
            GameData game = games.get(i);
            String whiteUser = game.whiteUsername() != null ? game.whiteUsername() : "open";
            String blackUser = game.blackUsername() != null ? game.blackUsername() : "open";
            out.printf("%d -- Game Name: %s  |  White User: %s  |  Black User: %s %n", i, game.gameName(), whiteUser, blackUser);
        }
    }

    private void printHelpMenu() {
        out.println(" --- Help Menu --- ");
        printCreate();
        out.println("list - list all games");
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