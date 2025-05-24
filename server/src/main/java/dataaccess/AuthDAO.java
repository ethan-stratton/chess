package dataaccess;

import model.AuthData;

public interface AuthDAO {
    void addAuth(AuthData authData);
    void addAuth(String authToken, String username);
    void deleteAuth(String authToken) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException;
    void clear();
}