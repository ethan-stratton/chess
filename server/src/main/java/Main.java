import server.Server;
import spark.Spark;

public class Main {
    public static void main(String[] args) {
        System.out.println("♕ 240 Chess Server: ");
        Server server = new Server();
        server.run(8080);
    }
}