package dataaccess;

import model.AuthData;
import java.util.HashSet;

public class MemoryAuthDAO implements AuthDAO {

    HashSet<AuthData> db;

    public MemoryAuthDAO() {
        db = HashSet.newHashSet(16);
    }

    @Override
    public void addAuth(String authToken, String username) {
        db.add(new AuthData(username, authToken));
    }

    @Override
    public void addAuth(AuthData authData) {
        //debug
        System.out.println("Storing token: " + authData.authToken() +
                " for user: " + authData.username());
        db.add(authData);
        //debug
        System.out.println("Current tokens: " +
                db.stream().map(AuthData::authToken).toList());
    }

    @Override
    public void deleteAuth(String authToken) {
        for (AuthData authData : db) {
            if (authData.authToken().equals(authToken)) {
                db.remove(authData);
                break;
            }
        }
    }

//    @Override
//    public void deleteAuth(String authToken) throws DataAccessException {
//        AuthData toRemove = getAuth(authToken); // Will throw if not found
//        db.remove(toRemove);
//    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        System.out.println("Validating token: " + authToken); // debug
        System.out.println("Stored tokens: " + db.stream().map(AuthData::authToken).toList());

        for (AuthData authData : db) {
            if (authData.authToken().equals(authToken)) {
                return authData;
            }
        }
        throw new DataAccessException("Token not found in database");
    }

    @Override
    public void clear() {
        db = HashSet.newHashSet(16);
    }
}