import java.io.BufferedReader;
import java.io.IOException;
import java.io.EOFException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ServerSocket;
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
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream());
             DataInputStream dataIn = new DataInputStream(socket.getInputStream());
             DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream())) {
    
            out.println("= Welcome to User Management System =");
            out.flush();
    
            int option = 0;
    
            // Check if client wants to register or login
            String regOrLog;
            while ((regOrLog = in.readLine()) != null) {
                if (regOrLog.equals("r")) {
                    out.println("register");
                    out.flush();
                    option = 1;
                } else if (regOrLog.equals("l")) {
                    out.println("login");
                    out.flush();
                    option = 2;
                } else {
                    out.println("invalid");
                    out.flush();
                    continue;
                }
    
                // Username and Password
                String username = in.readLine();
                if (username == null) break;
    
                String pass = in.readLine();
                if (pass == null) break;
    
                if (option == 1) {  // Register
                    if (users.register(username, pass)) {
                        out.println("New account created with username: " + username);
                        out.flush();
                    } else {
                        out.println(username + " already exists!");
                        out.flush();
                    }
                    continue;
                } else if (option == 2) {  // Login
                    try {
                        if (users.authenticate(username, pass)) {
                            out.println("success");
                            out.flush();
    
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
                            out.println("invalid");
                            out.flush();
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
                // Ensures user is logged out if session was interrupted
                users.logOut();
                System.out.println("Number of active sessions: " + users.getActiveSessions());
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }    
}    


public class MainServer {
    public static void main(String[] args) throws InterruptedException{
        try {
            UserManager users = new UserManager(2);
            DataManager data = new DataManager();
            ServerSocket serverSocket = new ServerSocket(12345);
            System.out.println("Server waiting for clients on port 12345...");


            while (true) {

                // Accept a client connection
                Socket socket = serverSocket.accept();
                System.out.println("Client connected: " + socket.getInetAddress().getHostAddress());

                // Create a new thread to handle the client
                Thread clientThread = new Thread(new ClientHandler(socket, users, data));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


