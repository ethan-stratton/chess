package dataaccess;

import model.UserData;

import java.util.HashSet;

public class MemoryUserDAO implements UserDAO {

    private HashSet<UserData> db;

    public MemoryUserDAO() {
        db = HashSet.newHashSet(16);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        for (UserData user : db) {
            if (user.username().equals(username)) {
                return user;
            }
        }
        throw new DataAccessException("User not found: " + username);
    }

    @Override
    public void createUser(String username, String password, String email) throws DataAccessException {
        try {
            getUser(username);
        }
        catch (DataAccessException e) {
            db.add(new UserData(username, password, email));
            return;
        }

        throw new DataAccessException("User already exists: " + username);

    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        for (UserData existing : db) {
            if (existing.username().equals(user.username())) {
                throw new DataAccessException("Username already taken");
            }
        }
        db.add(user);
    }


    @Override
    public boolean authenticateUser(String username, String password) throws DataAccessException {
        boolean userExists = false;
        for (UserData user : db) {
            if (user.username().equals(username)) {
                userExists = true;
            }
            if (user.username().equals(username) &&
                    user.password().equals(password)) {
                return true;
            }
        }
        if (userExists) {
            return false;
        }
        else {
            throw new DataAccessException("User does not exist: " + username);
        }
    }

    @Override
    public void clear() {
        db = HashSet.newHashSet(16);
    }
}