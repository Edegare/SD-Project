package server;

import java.io.IOException;
import java.io.EOFException;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import conn.*;


class ClientHandler implements Runnable {
    private final Socket socket;
    private UserManager users;
    private DataManager data;
    private TaggedConnection conn;
    private String client_username;


    public ClientHandler(Socket socket, UserManager users, DataManager data, TaggedConnection conn) {
        this.socket = socket;
        this.users = users;
        this.data = data;
        this.conn = conn;
    }

    private String getClient_username() {
        return client_username;
    }

    private void setClient_username(String client_username) {
        this.client_username = client_username;
    }

    @Override
    public void run() {
        boolean authenticated = false;
        try {
    
    
            int option = 0;
    
            // Check if client wants to register or login
            while (true) {

                Frame regLogFrame = this.conn.receive();

                option = regLogFrame.tag;

                // Parse username and password from received data
                String credentials = new String(regLogFrame.data);
                
                String[] parts = credentials.split(":");
                if (parts.length != 2) {
                    this.conn.send(option, "Invalid credentials format.".getBytes());
                    continue;
                }

                String username = parts[0];
                String pass = parts[1];

                if (option == 1) {  // ----------- Register
                    if (users.register(username, pass)) {
                        this.conn.send(option, ("New account created with username: " + username).getBytes());
                    } else {
                        this.conn.send(option, ("'" + username + "' already exists").getBytes());
                    }
                } else if (option == 2) {  // ------------- Login

                    int authentication_result = users.authenticate(username,pass);
                    if (authentication_result == 1) { // Successful login
                        conn.send(option, new byte[]{1});

                        setClient_username(username);
                        authenticated = true;
                        System.out.println(username + " authenticated!");
                        
                        // -------- Processing commands
                        while (true) {
                            Frame commandFrame = this.conn.receive();
                            if (commandFrame == null || commandFrame.tag == 0) break;

                            int tag = commandFrame.tag;

                            String[] commandTokens = new String(commandFrame.data).split(" ");
                            String command = commandTokens[0];
                            String[] rest = Arrays.copyOfRange(commandTokens, 1, commandTokens.length);

                            // handle commands
                            //Thread.sleep(3000);
                            if (command.equals("put")) { 
                                handlePut(tag, rest);
                            } else if (command.equals("get")) { 
                                handleGet(tag, rest);
                            }
                            else if (command.equals("multiput")) {
                                handleMultiPut(tag, rest);
                            }
                            else if (command.equals("multiget")) {
                                handleMultiGet(tag, rest);
                            }
                            
                        }
                        break;
                    } else if (authentication_result == 0) { // Login - Invalid credentials
                        conn.send(option, new byte[]{0});
                    } else { // Login - User is already logged by another client
                        conn.send(option, new byte[]{2});
                    }

                }
            }
        } catch (EOFException e) {
            System.out.println("Client disconnected unexpectedly.");
        } catch (IOException e) {
            System.out.println("Client disconnected unexpectedly.");
        } catch (InterruptedException e) {
            System.out.println("Client disconnected unexpectedly.");
        } finally {
            try {
                // Ensures user is logged out only if authenticated
                if (authenticated) {

                    users.logOut(getClient_username());
                    System.out.println("User logged out. Number of active sessions: " + users.getActiveSessions());
                }
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // ------------------ handle commands ------------------
    private void handlePut(int tag, String[] commandTokens) throws IOException {
        if (commandTokens.length != 2) {
            conn.send(tag, "Invalid number of arguments for 'put'. Requires key and value.".getBytes());
            return;
        }

        String key = commandTokens[0];
        byte[] value = commandTokens[1].getBytes();
        
        if (this.data.put(key, value)) {
            conn.send(tag, ("Key '" + key + "' updated successfully.").getBytes());
        }
        else {
            conn.send(tag, "Value cannot be empty.".getBytes());
        }
    }

    private void handleGet(int tag, String[] commandTokens) throws IOException {
        if (commandTokens.length != 1) {
            conn.send(tag, "Invalid number of arguments for 'get'. Requires key.".getBytes());
            return;
        }

        String key = commandTokens[0];
        byte[] value = this.data.get(key);

        if (value != null) {
            conn.send(tag, value);
        } else {
            conn.send(tag, "".getBytes());
        }
    }

    private void handleMultiGet(int tag, String[] commandTokens) throws IOException {
        if (commandTokens.length < 2) {
            conn.send(tag, "Invalid arguments! Requires at least one key.".getBytes());
            return;
        }

        int n;
        try {
            n = Integer.parseInt(commandTokens[0]); 
        } catch (NumberFormatException ex) {
            conn.send(tag, "Invalid number of keys specified.".getBytes());
            return;
        }

        if (commandTokens.length != 1 + n) { 
            conn.send(tag, ("Invalid arguments! Command 'multiGet' requires " + n + " keys.").getBytes());
            return;
        }

        Set<String> keys = new HashSet<>(Arrays.asList(commandTokens).subList(1, commandTokens.length));
        Map<String, byte[]> results = this.data.multiGet(keys);

        for (String key : keys) {
            byte[] value = results.getOrDefault(key, null);
            if (value == null) {
                conn.send(tag, key.getBytes()); // return just key if no value found
            } else {
                conn.send(tag, (key + " " + new String(value)).getBytes()); // return key and value if found
            }
        }
    }


    private void handleMultiPut(int tag, String[] commandTokens) throws IOException {
        if (commandTokens.length < 2) {
            conn.send(tag, "Invalid arguments! Requires at least one key-value pair.".getBytes());
            return;
        }

        int n;
        try {
            n = Integer.parseInt(commandTokens[0]); 
        } catch (NumberFormatException ex) {
            conn.send(tag, "Invalid number of key-value pairs specified.".getBytes());
            return;
        }

        if (commandTokens.length != 1 + (2 * n)) { 
            conn.send(tag, ("Invalid arguments! Command 'multiPut' requires " + n + " key-value pairs.").getBytes());
            return;
        }

        Map<String, byte[]> mapValues = new HashMap<>();
        for (int i = 1; i < commandTokens.length; i += 2) {
            String key = commandTokens[i];
            String value = commandTokens[i + 1];
            mapValues.put(key, value.getBytes());
        }

        this.data.multiPut(mapValues);
        conn.send(tag, "All keys updated successfully.".getBytes());
    }
}    
