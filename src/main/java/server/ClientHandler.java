package server;

import java.io.IOException;
import java.io.EOFException;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;


class ClientHandler implements Runnable {
    private final Socket socket;
    private UserManager users;
    private DataManager data;

    public ClientHandler(Socket socket, UserManager users, DataManager data) {
        this.socket = socket;
        this.users = users;
        this.data = data;
    }


    @Override
    public void run() {
        boolean authenticated = false;
        try (DataInputStream dataIn = new DataInputStream(socket.getInputStream());
             DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream())) {
    
            dataOut.writeUTF("= Welcome to User Management System =");
            dataOut.flush();
    
            int option = 0;
    
            // Check if client wants to register or login
            while (true) {

                option = dataIn.readInt();
                
                // Username and Password
                String username = dataIn.readUTF();
                if (username == null) break;
    
                String pass = dataIn.readUTF();
                if (pass == null) break;
    
                if (option == 1) {  // Register
                    if (users.register(username, pass)) {
                        dataOut.writeUTF("New account created with username: " + username);
                        dataOut.flush();
                    } else {
                        dataOut.writeUTF(username + " already exists!");
                        dataOut.flush();
                    }
                    continue;
                } else if (option == 2) {  // Login
                    try {
                        if (users.authenticate(username, pass)) {
                            authenticated = true;
                            dataOut.writeBoolean(true);
                            dataOut.flush();
    
                            System.out.println(username + " authenticated!");
    
                            // Processing commands
                            String command;
                            while (true) {
                                command = dataIn.readUTF();
                                if (command == null || command.equals("end")) break;
    
                                else if (command.equals("put")) { //PUT COMMAND DESERIALIZE
                                    String key = dataIn.readUTF();
                                    int nBytes = dataIn.readInt();
                                    byte[] value = dataIn.readNBytes(nBytes);
    
                                    this.data.put(key, value);
                                    dataOut.writeBoolean(true);
                                    dataOut.flush();
                                } else if (command.equals("get")) { // GET COMMAND DESERIALIZE
                                    String key = dataIn.readUTF();
                                    byte[] value = this.data.get(key);
    
                                    if (value != null) {
                                        dataOut.writeInt(value.length);
                                        dataOut.write(value);
                                    } else {
                                        dataOut.writeInt(0); // Key not found
                                    }
                                    dataOut.flush();
                                }
                            }
                            break;
                        } else {
                            dataOut.writeBoolean(false);
                            dataOut.flush();
                        }
                    } catch (InterruptedException e) {
                        System.err.println("Authentication interrupted for user: " + username);
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } catch (EOFException e) {
            System.out.println("Client disconnected unexpectedly.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // Ensures user is logged out only if authenticated
                if (authenticated) {
                    users.logOut();
                    System.out.println("User logged out. Number of active sessions: " + users.getActiveSessions());
                }
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }    
}    
