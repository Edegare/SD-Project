package server;

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import conn.*;

public class Server {
    private static final int PORT = 12345;
    private static final int MAX_CLIENTS = 10;

    private ServerSocket serverSocket;
    private UserManager users;
    private DataManager data;
    private ExecutorService threadPool;

    public Server() {
        try {
            users = new UserManager(MAX_CLIENTS);
            data = new DataManager();
            serverSocket = new ServerSocket(PORT);
            threadPool = Executors.newFixedThreadPool(MAX_CLIENTS * 2); // Thread pool with MAX_CLIENTS * 2 number of threads
            System.out.println("Server waiting for clients on port " + PORT + "...");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void start() {
        try {
            while (true) {
                // Accept a client connection
                Socket socket = serverSocket.accept();
                TaggedConnection conn = new TaggedConnection(socket);
                System.out.println("Client connected: " + socket.getInetAddress().getHostAddress());

                // Submit a ClientHandler to the thread pool
                threadPool.submit(new ClientHandler(socket, users, data, conn));
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
            if (threadPool != null && !threadPool.isShutdown()) {
                threadPool.shutdown(); // shutdown the thread pool
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
