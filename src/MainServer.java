

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.management.RuntimeErrorException;

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
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            
            out.println("= Welcome to User Management System =");
            out.flush();

            // Get client username 
            String username = in.readLine();
            if (username == null) {
                socket.shutdownOutput();
                socket.shutdownInput();
                socket.close();
                return;
            }

            // Get client pass
            String pass = in.readLine();
            if (pass==null) {
                socket.shutdownOutput();
                socket.shutdownInput();
                socket.close();
                return;
            }
            
            if (this.users.register(username, pass)){ // Regist new account if username doesnt exist, else does nothing
                System.out.println("New account created with username: " + username);
            
            }
            else {
                System.out.println(username + " already exist!");
            }

            try {
                if (this.users.authenticate(username, pass)) {
                    out.println("sucess");
                    out.flush();

                    System.out.println(username + " authenticated!");

                    String line;
                    while ((line = in.readLine()) != null) { // Process client requests
                        if (line.equals("end")) {
                            break;
                        }
                        out.println("You wrote: " + line);
                        out.flush();
                    }

                    this.users.logOut();  // Logout user 
                } else {
                    out.println("invalid");
                    out.flush();
                }
            } catch (InterruptedException e) {
                System.err.println("Authentication interrupted for user: " + username);
                Thread.currentThread().interrupt();  // Preserve interrupt status
            }


            System.out.println("Client disconnected.");

            // Close socket and streams
            socket.shutdownOutput();
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


