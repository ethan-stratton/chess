import ui.*;

public class Main {
    public static void main(String[] args) {

        ServerFacade server = new ServerFacade();

        PreLogin prelogin = new PreLogin(server);
        prelogin.run();
        System.out.println("Exited Chess UI");

//        //Debug for register and login:
//        String uniqueUsername = "user" + System.currentTimeMillis(); //before was getting error code 403 for using same username to login, generated new username
//
//        System.out.println(server.register(uniqueUsername, "password123", "test@email.com"));
//        System.out.println(server.login(uniqueUsername, "password123"));
//
//        System.out.println(server.createGame("game1"));
//
//        System.out.println(server.listGames());
//
//        //"false" error here when wrong gameID, or gameID in use by another color already
//        System.out.println(server.joinGame(8716, "WHITE"));
//
//        //above line should change what games are listed with whiteUsername
//        System.out.println(server.listGames());

    }
}