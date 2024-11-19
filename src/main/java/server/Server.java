package server;

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;


public class Server {
    private static final int PORT = 12345; // Porta do servidor
    private static final int MAX_CLIENTS = 2; // Número máximo de clientes para controle de threads
    
    private ServerSocket serverSocket;
    private UserManager users;
    private DataManager data;

    public Server() {
        try {
            users = new UserManager(MAX_CLIENTS);
            data = new DataManager();
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server waiting for clients on port 12345...");

        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }
    
    public void start() {
        try {
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
        } finally {
            shutdown();
        }
    }

    private void shutdown() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            System.out.println("Server closed.");
        } catch (IOException e) {
            System.err.println("Error closing server: " + e.getMessage());
        }
    }
    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}