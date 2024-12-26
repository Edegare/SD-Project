package server;

import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.concurrent.*;

import conn.*;

public class Server {
    private static final int PORT = 12345;
    private static final int MAX_CLIENTS = 10;

    private ServerSocket serverSocket;
    private UserManager users;
    private DataManager data;
    private ThreadPoolExecutor clientThreadPool;
    private ThreadPoolExecutor commandThreadPool;

    public Server() {
        try {
            users = new UserManager(MAX_CLIENTS);
            data = new DataManager();
            serverSocket = new ServerSocket(PORT);

            // Create a custom thread pool
            clientThreadPool = new ThreadPoolExecutor(
                    MAX_CLIENTS,                              // Core pool size (reusable threads)
                    (int) (MAX_CLIENTS+(MAX_CLIENTS*0.5)),    // Maximum pool size (core size + extra)
                    5L, TimeUnit.SECONDS,                     // Keep-alive time for idle threads
                    new LinkedBlockingQueue<>(),              // Unbounded task queue
                    Executors.defaultThreadFactory(),         // Default thread factory
                    new ThreadPoolExecutor.AbortPolicy() {    // Custom rejection policy
                        @Override
                        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                            new Thread(r).start();
                        }
                    }
            );

            // Thread pool for CommandExecutors
            commandThreadPool = new ThreadPoolExecutor(
                    MAX_CLIENTS * 2,               // Core pool size
                    MAX_CLIENTS * 4,                          // Maximum pool size
                    5L, TimeUnit.SECONDS,                     // Keep-alive time for extra threads
                    new LinkedBlockingQueue<>(),              // Bounded queue for commands
                    Executors.defaultThreadFactory(),         // Default thread factory
                    new ThreadPoolExecutor.AbortPolicy() {    // Custom rejection policy for commands
                        @Override
                        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                            new Thread(r).start();
                        }
                    }
            );

            // Prestart all core threads
            clientThreadPool.prestartAllCoreThreads();
            commandThreadPool.prestartAllCoreThreads();
            System.out.println("Server waiting for clients on port " + PORT + "...");
        } catch (IOException e) {
            System.err.println("Error initializing server: " + e.getMessage());
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
                clientThreadPool.submit(new ClientHandler(socket, users, data, conn, commandThreadPool));
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
            if (commandThreadPool != null && !commandThreadPool.isShutdown()) {
                commandThreadPool.shutdown(); // Gracefully shutdown the thread pool
            }
            if (clientThreadPool != null && !clientThreadPool.isShutdown()) {
                clientThreadPool.shutdown(); // Gracefully shutdown the thread pool
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
