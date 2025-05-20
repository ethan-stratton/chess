package service;

import dataAccess.AuthDAO;
import dataAccess.DataAccessException;
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

    public AuthData createUser(UserData userData) throws DataAccessException {
        try {
            userDAO.createUser(userData);
            String authToken = UUID.randomUUID().toString();
            AuthData authData = new AuthData(userData.username(), authToken);
            authDAO.addAuth(authData);
            return authData;
        } catch (DataAccessException e) {
            throw new DataAccessException("Username already taken");
        }
    }

    public AuthData loginUser(UserData userData) throws DataAccessException {
        boolean userAuthenticated = userDAO.authenticateUser(userData.username(), userData.password());

        if (userAuthenticated) {
            String authToken = UUID.randomUUID().toString();
            AuthData authData = new AuthData(userData.username(), authToken);
            authDAO.addAuth(authData);  // didn't have this line...
            return authData;
        }
        else {
            throw new DataAccessException("Password is incorrect");
        }
    }

    public void logoutUser(String authToken) throws DataAccessException {
        authDAO.getAuth(authToken); // getAuth throws an exception, debug here
        authDAO.deleteAuth(authToken);
    }


    public void clear() {
        userDAO.clear();
        authDAO.clear();
    }
}