package service;

import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
import dataAccess.BadRequestException;
import dataAccess.UnauthorizedUserException;
import dataAccess.UserDAO;
import model.AuthData;
import model.UserData;

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
            userDAO.createUser(userData);
        } catch (DataAccessException e) {
            throw e;
        }

        String authToken = UUID.randomUUID().toString();
        authDAO.addAuth(authToken, userData.username());
        return new AuthData(userData.username(), authToken);
    }

    public AuthData loginUser(UserData userData) throws DataAccessException, BadRequestException {
        if (userData == null || userData.username() == null || userData.password() == null) {
            throw new BadRequestException("Missing required fields");
        }

        boolean isAuthenticated = userDAO.authenticateUser(userData.username(), userData.password());
        if (!isAuthenticated) {
            throw new DataAccessException("Invalid credentials");
        }

        String authToken = UUID.randomUUID().toString();
        authDAO.addAuth(authToken, userData.username());
        return new AuthData(userData.username(), authToken);
    }

    public void logoutUser(String authToken) throws DataAccessException, UnauthorizedUserException {
        if (authToken == null || authToken.isBlank()) {
            throw new UnauthorizedUserException("Missing authorization token");
        }

        try {
            authDAO.getAuth(authToken);
            authDAO.deleteAuth(authToken);
        } catch (DataAccessException e) {
            throw new UnauthorizedUserException("Invalid authentication token");
        }
    }


    public void clear() {
        userDAO.clear();
        authDAO.clear();
    }
}