package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private DataInputStream input;
    private DataOutputStream output;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            input = new DataInputStream(clientSocket.getInputStream());
            output = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            System.err.println("Erro ao obter streams do cliente: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            // Exemplo de comunicação simples com o cliente
            output.writeUTF("Conexão estabelecida com o servidor!");

            // Loop de comunicação
            String clientMessage;
            while ((clientMessage = input.readUTF()) != null) {
                System.out.println("Mensagem do cliente: " + clientMessage);

                // Responde ao cliente
                output.writeUTF("Servidor recebeu: " + clientMessage);

                if (clientMessage.equalsIgnoreCase("exit")) {
                    System.out.println("Cliente solicitou fim da conexão.");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Erro de comunicação com o cliente: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private void closeConnection() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            if (input != null) input.close();
            if (output != null) output.close();
            System.out.println("Conexão com o cliente encerrada.");
        } catch (IOException e) {
            System.err.println("Erro ao fechar conexão com o cliente: " + e.getMessage());
        }
    }
}
