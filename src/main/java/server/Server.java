package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static final int PORT = 12345; // Porta do servidor
    private static final int MAX_CLIENTS = 10; // Número máximo de clientes para controle de threads

    private ServerSocket serverSocket;
    private ExecutorService clientPool; // Gerencia um pool de threads para os clientes

    public Server() {
        try {
            serverSocket = new ServerSocket(PORT);
            clientPool = Executors.newFixedThreadPool(MAX_CLIENTS); // Limita o número de threads
            System.out.println("Servidor iniciado e aguardando conexões na porta " + PORT);
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }

    public void start() {
        try {
            while (true) {
                // Aguarda uma conexão com o cliente
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

                // Cria um novo ClientHandler para lidar com o cliente
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientPool.execute(clientHandler); // Executa o handler no pool de threads
            }
        } catch (IOException e) {
            System.err.println("Erro ao aceitar conexão de cliente: " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    private void shutdown() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            clientPool.shutdown();
            System.out.println("Servidor encerrado.");
        } catch (IOException e) {
            System.err.println("Erro ao encerrar o servidor: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
