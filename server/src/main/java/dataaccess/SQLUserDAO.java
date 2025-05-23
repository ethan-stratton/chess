package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.SQLException;

public class SQLUserDAO implements UserDAO {

    public SQLUserDAO() {
        try (var conn = DatabaseManager.getConnection()) {
            conn.setCatalog("chess");
            var createTestTable = """            
                    CREATE TABLE if NOT EXISTS user (
                                    username VARCHAR(255) NOT NULL,
                                    password VARCHAR(255) NOT NULL,
                                    email VARCHAR(255),
                                    PRIMARY KEY (username)
                                    )""";
            try (var createTableStatement = conn.prepareStatement(createTestTable)) {
                createTableStatement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("SELECT username, password, email FROM user WHERE username=?")) {
                statement.setString(1, username);
                try (var results = statement.executeQuery()) {
                    if (!results.next()) {
                        throw new DataAccessException("User not found: " + username);
                    }
                    var password = results.getString("password");
                    var email = results.getString("email");
                    return new UserData(username, password, email);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error accessing user: " + e.getMessage());
        }
    }

    @Override
    public void createUser(String username, String password, String email) throws DataAccessException {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("INSERT INTO user (username, password, email) VALUES(?, ?, ?)")) {
                statement.setString(1, username);
                statement.setString(2, hashedPassword);
                statement.setString(3, email);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating user: " + e.getMessage());
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        // delegate to the other createUser method
        createUser(user.username(), user.password(), user.email());
    }

    @Override
    public boolean authenticateUser(String username, String password) throws DataAccessException {
        try {
            UserData user = getUser(username);
            return BCrypt.checkpw(password, user.password());
        } catch (DataAccessException e) {
            return false; // no user by that name
        }
    }

    @Override
    public void clear() {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("TRUNCATE user")) {
                statement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Failed to clear users: " + e.getMessage());
        }
    }
}