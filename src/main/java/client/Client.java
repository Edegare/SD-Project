package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private static final String SERVER_ADDRESS = "127.0.0.1"; // Endereço do servidor
    private static final int SERVER_PORT = 12345; // Porta do servidor

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream());
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Conectado ao servidor.");
            System.out.println("Servidor diz: " + input.readUTF());

            // Envio de mensagens para o servidor
            String message;
            do {
                System.out.print("Digite uma mensagem (ou 'exit' para sair): ");
                message = scanner.nextLine();
                output.writeUTF(message);
                System.out.println("Servidor respondeu: " + input.readUTF());
            } while (!message.equalsIgnoreCase("exit"));

        } catch (IOException e) {
            System.err.println("Erro de conexão com o servidor: " + e.getMessage());
        }
    }
}
