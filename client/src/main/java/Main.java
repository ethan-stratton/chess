
public class Main {
    public static void main(String[] args) {

        ServerFacade server = new ServerFacade();
//        System.out.println(server.register("1","2","3"));
//        System.out.println(server.login("1","2"));

        //before was getting error code 403 for using same username to login
        //Debug:
        String uniqueUsername = "user" + System.currentTimeMillis();
        System.out.println(server.register(uniqueUsername, "password123", "test@email.com"));
        System.out.println(server.login(uniqueUsername, "password123"));


    }
}