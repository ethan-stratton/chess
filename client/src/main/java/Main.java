import ui.*;

public class Main {
    public static void main(String[] args) throws Exception {
        ServerFacade server = new ServerFacade();
        PreLogin prelogin = new PreLogin(server);
        prelogin.run();
    }
}