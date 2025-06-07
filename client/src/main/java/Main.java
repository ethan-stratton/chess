import ui.*;

public class Main {
    public static void main(String[] args) {
        ServerFacade server = new ServerFacade();
        PreLogin prelogin = new PreLogin(server);
        prelogin.run();
        System.out.println("Exited Chess UI");
    }
}