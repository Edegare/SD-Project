package server;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.HashMap;
import java.util.Map;


public class UserManager {
    private Map<String, String> users = new HashMap<>();  // Store username and password
    
    private Lock lock = new ReentrantLock();  
    private Condition cond = lock.newCondition();

    private int activeSessions = 0;
    private int maxSessions;

    public UserManager(int maxSessions) {
        this.maxSessions=maxSessions;
    }

    // Get number of users logged in
    public int getActiveSessions() {
        return this.activeSessions;
    }

    // Registers a new user
    public boolean register(String username, String password) {
        lock.lock();
        try {
            if (users.containsKey(username)) {
                return false;  
            }
            users.put(username, password);
            return true;
        } finally {
            lock.unlock();
        }
    }


    // Authenticates an existing user
    public boolean authenticate(String username, String password) throws InterruptedException{
        lock.lock();
        try {
            if (users.containsKey(username) && users.get(username).equals(password)) {
                
                while (this.activeSessions >= this.maxSessions) { // Wait till a user log out
                    this.cond.await();
                }
                

                this.activeSessions++; // New user logged
                return true;

            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    // Log Out user
    public void logOut() {
        lock.lock();
        try {
            this.activeSessions--;
            this.cond.signal(); // Sign a thread that there is a free spot
        } finally {
            lock.unlock();
        }

    }
}