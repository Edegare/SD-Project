import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ServerSocket;

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
             PrintWriter out = new PrintWriter(socket.getOutputStream())) {

            // ----------- Register and Login ------------------
            out.println("= Welcome to User Management System =");
            out.flush();
            
            int option = 0;
            
            // Check if client wants to register or authenticate
            String regOrLog;
            while ((regOrLog = in.readLine()) != null) {
                if (regOrLog.equals("r")) {
                    // handle register
                    out.println("register");
                    out.flush();
                    option = 1;

                } else if (regOrLog.equals("l")) {
                    // handle login
                    out.println("login");
                    out.flush();
                    option = 2;

                } else {
                    out.println("invalid");
                    out.flush();
                    continue;
                }

                // Username read
                String username = in.readLine();
                if (username == null) {
                    break;
                }
                // Password read
                String pass = in.readLine();
                if (pass == null) {
                    break;
                }

                // Handle register
                if (option == 1) {
                    if (users.register(username, pass)) {
                        out.println("New account created with username: " + username);
                        out.flush();
                    } else {
                        out.println(username + " already exists!");
                        out.flush();
                    }
                    continue;
                }
                // Handle login
                else if (option == 2) {
                    try {
                        if (users.authenticate(username, pass)) {
                            // user logged with sucess
                            out.println("success");
                            out.flush();
                            System.out.println(username + " authenticated!");
        
                            String line;
                            while ((line = in.readLine()) != null) {
                                if (line.equals("end")) break;
                                out.println("You wrote: " + line);
                                out.flush();
                            }
                            users.logOut();
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


            // close connection
            socket.shutdownOutput();
            
            System.out.println("Client disconnected.");

            socket.shutdownInput();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
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


