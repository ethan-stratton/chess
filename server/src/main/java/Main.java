import server.Server;
import spark.Spark;

public class Main {
    public static void main(String[] args) {
        System.out.println("♕ 240 Chess Server: Starting...");
        try {
            Server server = new Server();
            int port = server.run(8080);
            System.out.println("♕ 240 Chess Server: Running on port " + port);
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }

    }
}