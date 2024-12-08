package server;

import java.io.IOException;
import java.io.EOFException;
import java.net.Socket;
import java.util.Arrays;

import conn.*;


class ClientHandler implements Runnable {
    private final Socket socket;
    private UserManager users;
    private DataManager data;
    private TaggedConnection conn;

    public ClientHandler(Socket socket, UserManager users, DataManager data, TaggedConnection conn) {
        this.socket = socket;
        this.users = users;
        this.data = data;
        this.conn = conn;
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

                if (option == 1) {  // Register
                    if (users.register(username, pass)) {
                        this.conn.send(option, ("New account created with username: " + username).getBytes());
                    } else {
                        this.conn.send(option, ("'" + username + "' already exists").getBytes());
                    }
                } else if (option == 2) {  // Login
                    
                    if (users.authenticate(username, pass)) {
                        conn.send(option, "success".getBytes());

                        authenticated = true;
                        System.out.println(username + " authenticated!");
                        
                        // Processing commands
                        while (true) {
                            Frame commandFrame = this.conn.receive();
                            if (commandFrame == null || commandFrame.tag == 0) break;

                            int tag = commandFrame.tag;

                            String[] commandTokens = new String(commandFrame.data).split(" ");
                            String command = commandTokens[0];
                            String[] rest = Arrays.copyOfRange(commandTokens, 1, commandTokens.length);

                            // handle commands
                            Thread.sleep(3000);
                            if (command.equals("put")) { 
                                handlePut(tag, rest);
                            } else if (command.equals("get")) { 
                                handleGet(tag, rest);
                            }
                            
                        }
                        break;
                    } else {
                        conn.send(option, "".getBytes());
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
                    users.logOut();
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
}    
