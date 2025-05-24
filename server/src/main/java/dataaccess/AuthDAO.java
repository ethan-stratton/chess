package dataaccess;

import model.AuthData;

public interface AuthDAO {
    void addAuth(AuthData authData) throws DataAccessException;
    void addAuth(String authToken, String username) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void clear();
}