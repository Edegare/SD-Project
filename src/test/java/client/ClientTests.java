package client;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import server.Server;

import conn.Frame;
import conn.TaggedConnection;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ClientTests {

    private static Thread serverThread;

    @BeforeAll
    static void startServer() {
        serverThread = new Thread(() -> {
            try {
                Server server = new Server();
                server.start(); // Inicia o servidor
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        serverThread.start();

        // Esperar o servidor inicializar
        try {
            Thread.sleep(2000); // Ajuste se necessário
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    static void stopServer() {
        if (serverThread != null && serverThread.isAlive()) {
            serverThread.interrupt(); // Interrompe a thread do servidor
        }
    }

    @Test
    void testPutAndGetCommand() {
        Socket socket = null;
        TaggedConnection conn = null;

        try {
            socket = new Socket("127.0.0.1", 12345);
            conn = new TaggedConnection(socket);

            // Registo e login
            conn.send(new Frame(1, "userPutAndGet:password1".getBytes()));
            conn.receive(); // Ignorar resposta do registo
            conn.send(new Frame(2, "userPutAndGet:password1".getBytes())); // Login
            conn.receive(); // Ignorar resposta do login

            // Comando PUT
            String putCommand = "put key1 value1";
            conn.send(new Frame(3, putCommand.getBytes())); // Tag 3 = PUT
            Frame putResponse = conn.receive();
            String putMessage = new String(putResponse.data);

            assertEquals("Key 'key1' updated successfully.", putMessage, "PUT falhou.");

            // Comando GET
            String getCommand = "get key1";
            conn.send(new Frame(4, getCommand.getBytes())); // Tag 4 = GET
            Frame getResponse = conn.receive();
            String getMessage = new String(getResponse.data);

            assertEquals("value1", getMessage, "GET falhou.");


        } catch (Exception e) {
            throw new AssertionError("Erro no comando PUT/GET: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.send(new Frame(0, "end".getBytes())); // Notificar o servidor
                    Thread.sleep(50); // Pequeno atraso para garantir o processamento
                    conn.close();
                }
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    void testMultipleClients() {
        ExecutorService executor = Executors.newFixedThreadPool(5);

        for (int i = 0; i < 5; i++) {
            int clientId = i;

            executor.submit(() -> {
                Socket socket = null;
                TaggedConnection conn = null;

                try {
                    socket = new Socket("127.0.0.1", 12345);
                    conn = new TaggedConnection(socket);

                    // Registo
                    conn.send(new Frame(1, ("userMultiCli" + clientId + ":pass" + clientId).getBytes()));
                    conn.receive();

                    // Login
                    conn.send(new Frame(2, ("userMultiCli" + clientId + ":pass" + clientId).getBytes()));
                    conn.receive();

                    // Comando PUT
                    conn.send(new Frame(3, ("put key" + clientId + " value" + clientId).getBytes()));
                    conn.receive();

                } catch (Exception e) {
                    throw new AssertionError("Erro no cliente " + clientId + ": " + e.getMessage());
                } finally {
                    try {
                        if (conn != null) {
                            conn.send(new Frame(0, "end".getBytes())); // Notificar o servidor
                            Thread.sleep(50); // Pequeno atraso para garantir o processamento
                            conn.close();
                        }
                        if (socket != null && !socket.isClosed()) socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new AssertionError("Execução interrompida: " + e.getMessage());
        }
    }

    @Test
    void testMultiPutAndMultiGet() {
        Socket socket = null;
        TaggedConnection conn = null;

        try {
            socket = new Socket("127.0.0.1", 12345);
            conn = new TaggedConnection(socket);

            // Registo e login
            conn.send(new Frame(1, "userMulti:password".getBytes()));
            conn.receive(); // Ignorar resposta do registo
            conn.send(new Frame(2, "userMulti:password".getBytes())); // Login
            conn.receive(); // Ignorar resposta do login

            // Comando multiPut
            String multiPutCommand = "multiput 2 key1 value1 key2 value2";
            conn.send(new Frame(3, multiPutCommand.getBytes())); // Tag 3 = multiPut
            Frame response = conn.receive();
            String multiPutResponse = new String(response.data);
            assertEquals("All keys updated successfully.", multiPutResponse, "Falha no comando multiPut.");

            // Comando multiGet
            String multiGetCommand = "multiget 2 key1 key2";
            conn.send(new Frame(4, multiGetCommand.getBytes())); // Tag 4 = multiGet

            // Receber múltiplas mensagens
            StringBuilder multiGetResponses = new StringBuilder();
            for (int i = 0; i < 2; i++) { // Esperar 2 respostas (número de chaves)
                response = conn.receive();
                multiGetResponses.append(new String(response.data)).append(" ");
            }

            String combinedResponse = multiGetResponses.toString().trim();
            assertTrue(combinedResponse.contains("key1 value1") && combinedResponse.contains("key2 value2"),
                    "Falha no comando multiGet.");

        } catch (Exception e) {
            throw new AssertionError("Erro nos comandos multiPut/multiGet: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.send(new Frame(0, "end".getBytes())); // Notificar o servidor
                    Thread.sleep(50); // Pequeno atraso para garantir o processamento
                    conn.close();
                }
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Test
    void testInvalidInputs() {
        Socket socket = null;
        TaggedConnection conn = null;

        try {
            socket = new Socket("127.0.0.1", 12345);
            conn = new TaggedConnection(socket);

            // Registo e login
            conn.send(new Frame(1, "userInvalid:password".getBytes()));
            conn.receive(); // Ignorar resposta do registo
            conn.send(new Frame(2, "userInvalid:password".getBytes())); // Login
            conn.receive(); // Ignorar resposta do login

            // Comando inválido
            String invalidCommand = "invalidCommand";
            conn.send(new Frame(5, invalidCommand.getBytes())); // Tag 5 = comando inválido
            Frame response = conn.receive();
            String errorResponse = new String(response.data);
            assertEquals("Unsupported command: invalidCommand", errorResponse, "Comando inválido não tratado corretamente.");


        } catch (Exception e) {
            throw new AssertionError("Erro no teste de inputs inválidos: " + e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.send(new Frame(0, "end".getBytes())); // Notificar o servidor
                    Thread.sleep(50); // Pequeno atraso para garantir o processamento
                    conn.close();
                }
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    void testHighConcurrency() {
        int NUM_CLIENTS = 50;
        ExecutorService executor = Executors.newFixedThreadPool(NUM_CLIENTS);

        for (int i = 0; i < NUM_CLIENTS; i++) {
            int clientId = i;

            executor.submit(() -> {
                Socket socket = null;
            TaggedConnection conn = null;

                try {
                    socket = new Socket("127.0.0.1", 12345);
                    conn = new TaggedConnection(socket);

                    // Registo e login
                    conn.send(new Frame(1, ("userHigh" + clientId + ":pass" + clientId).getBytes()));
                    conn.receive();
                    conn.send(new Frame(2, ("userHigh" + clientId + ":pass" + clientId).getBytes()));
                    conn.receive();

                    // Comando PUT
                    conn.send(new Frame(3, ("put key" + clientId + " value" + clientId).getBytes()));
                    conn.receive();


                } catch (Exception e) {
                    throw new AssertionError("Erro no cliente " + clientId + ": " + e.getMessage());
                } finally {
                    try {
                        if (conn != null) {
                            conn.send(new Frame(0, "end".getBytes())); // Notificar o servidor
                            Thread.sleep(50); // Pequeno atraso para garantir o processamento
                            conn.close();
                        }
                        if (socket != null && !socket.isClosed()) socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(2, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new AssertionError("Execução interrompida: " + e.getMessage());
        }
    }
}
