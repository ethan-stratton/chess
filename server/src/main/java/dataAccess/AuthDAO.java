package dataAccess;

import model.AuthData;

public interface AuthDAO {
    void addAuth(AuthData authData);
    void deleteAuth(String authToken);
    AuthData getAuth(String authToken);
    void clear();
}