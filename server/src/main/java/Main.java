import server.Server;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import spark.Spark;

public class Main {
    public static void main(String[] args) {
            try { DatabaseManager.createDatabase(); } catch (DataAccessException ex) {
                throw new RuntimeException(ex);
            }

            Server server = new Server();
            int port = server.run(8080);
            System.out.println("♕ 240 Chess Server: Running on port " + port);



//        System.out.println("♕ 240 Chess Server: Starting...");
//        try {
//            Server server = new Server();
//            int port = server.run(8080);
//            System.out.println("♕ 240 Chess Server: Running on port " + port);
//        } catch (Exception e) {
//            System.err.println("Failed to start server: " + e.getMessage());
//        }

    }
}