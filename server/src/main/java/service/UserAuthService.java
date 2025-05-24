package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.BadRequestException;
import dataaccess.UnauthorizedUserException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;

import java.sql.SQLException;
import java.util.UUID;

public class UserAuthService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserAuthService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData createUser(UserData userData) throws DataAccessException, BadRequestException {
        if (userData == null || userData.username() == null || userData.password() == null || userData.email() == null) {
            throw new BadRequestException("Missing required fields");
        }

        try {
            try {
                userDAO.getUser(userData.username());
                throw new DataAccessException("Username already taken");
            } catch (DataAccessException e) {
                if (!e.getMessage().contains("not found")) {
                    throw e; // Re-throw other DataAccessExceptions
                }
            }

            userDAO.createUser(userData);
            String authToken = UUID.randomUUID().toString();
            authDAO.addAuth(authToken, userData.username());
            return new AuthData(userData.username(), authToken);
        } catch (Exception e) {
            if (e.getCause() instanceof SQLException) {
                throw new DataAccessException("Database connection failed");
            }
            throw new DataAccessException(e.getMessage());
        }
    }

    public AuthData loginUser(UserData userData) throws DataAccessException, BadRequestException {
        if (userData == null || userData.username() == null || userData.password() == null) {
            throw new BadRequestException("Missing required fields");
        }

        // throw DataAccessException
        boolean isAuthenticated = userDAO.authenticateUser(userData.username(), userData.password());
        if (!isAuthenticated) {
            throw new DataAccessException("Invalid credentials");
        }

        String authToken = UUID.randomUUID().toString();
        authDAO.addAuth(authToken, userData.username());

        return new AuthData(userData.username(), authToken);
    }

    public void logoutUser(String authToken) throws DataAccessException {
        if (authToken == null || authToken.isBlank()) {
            throw new DataAccessException("Missing authorization token");
        }

        try {
            authDAO.getAuth(authToken);
            authDAO.deleteAuth(authToken);
        } catch (DataAccessException e) {
            throw new DataAccessException("Database operation failed: " + e.getMessage());
        }
    }

    public void clear() {
        authDAO.clear();
        userDAO.clear();
    }
}