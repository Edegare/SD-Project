package client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;


public class Client {

    private static final String SERVER_ADDRESS = "127.0.0.1"; // Endereço do servidor
    private static final int SERVER_PORT = 12345; // Porta do servidor

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));
             DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
             DataInputStream dataIn = new DataInputStream(socket.getInputStream())) {

            // ----------- Register and Login ------------------
            

            System.out.println(dataIn.readUTF());  // Welcome 


            int option = 0;
            String regOrLogOption;
            
            // Prompt for register or login option
            while (true) {

                System.out.println("Choose an option, write 1 or 2.");  // Register/Login prompt
                System.out.println("1. Register");
                System.out.println("2. Login");
                System.out.println("Your Option:");

                regOrLogOption = systemIn.readLine();
                if (regOrLogOption == null) {
                    break;
                }
                System.out.println();


                if (regOrLogOption.equals("1")) { // Register
                    System.out.println("= Create New Account =");
                    option = 1;
    
                } else if (regOrLogOption.equals("2")) { // Login
                    System.out.println("= Login =");
                    option = 2;

                } else { // Invalid option
                    System.out.println("Invalid option! Please enter 1 or 2.");
                    System.out.println();
                    continue;
                }

                dataOut.writeInt(option);
                dataOut.flush();

                // Prompt for username
                System.out.println("Username: ");
                String username = systemIn.readLine();
                dataOut.writeUTF(username);
                dataOut.flush();

                // Prompt for password
                System.out.println("Password: ");
                String password = systemIn.readLine();
                dataOut.writeUTF(password);
                dataOut.flush();

                // ------------ Create new account ----------------
                if (option == 1) {
                    System.out.println(dataIn.readUTF());
                    System.out.println();
                }
                // ------------- Log in --------------
                if (option == 2) {
                    // Authentication
                    System.out.println("Waiting for server to authenticate user...");
                    boolean isAuthenticated = dataIn.readBoolean();

                    if (isAuthenticated) { // If authenticated start data management

                        

                        System.out.println("Logged in successfully!");
                        System.out.println();
                        

                        String userInput;

                        while (true) {
                            
                            System.out.println("Enter a command ('help' to list commands):");

                            userInput = systemIn.readLine(); // Read command
                            if (userInput == null) break;
                            System.out.println();

                            if (userInput.equals("help")) { // Help commands - list all commands
                                System.out.println("List of commands:");
                                System.out.println("help - List all commands.");
                                System.out.println("put key value - Adds or updates a single key-value pair in the server.");
                                System.out.println("get key - Retrieves the value associated with the given key, or returns null if the key does not exist.");
                                System.out.println("multiPut 3 key value key value key value - Adds or updates multiple key-value pairs in the server.");
                                System.out.println("multiGet 3 key key key - Retrieves values for the specified keys and returns them as a map.");
                                System.out.println("end - End program");
                                System.out.println();
                                continue;
                            }

                            // Parse
                            String[] tokens = userInput.split(" ");
                            if (tokens.length == 0 || tokens[0].isEmpty()) {
                                System.out.println("Invalid command!");
                                System.out.println();
                                continue;
                            }

                            String command = tokens[0].toLowerCase();

                            // ---- Command Serializable ----

                            // END CLIENT
                            if (command.equals("end")) { 
                                dataOut.writeUTF(command);
                                dataOut.flush();
                                break;
                            }
                            // PUT COMMAND
                            else if (command.equals("put")){
                                if (tokens.length != 3) {
                                    System.out.println("Invalid number of arguments! Command 'put' receives 2 arguments (key value)");
                                    System.out.println();
                                    continue;
                                }
                                dataOut.writeUTF(command);
                                dataOut.flush();
                                dataOut.writeUTF(tokens[1]);
                                dataOut.writeInt(tokens[2].length());
                                dataOut.writeBytes(tokens[2]);
                                dataOut.flush();

                                boolean sucess = dataIn.readBoolean();
                                if (sucess) {
                                    System.out.println("'"+ tokens[1] + "' updated.");
                                    System.out.println();
                                }
                            }
                            // GET COMMAND
                            else if (command.equals("get")){
                                if (tokens.length != 2) {
                                    System.out.println("Invalid number of arguments! Command 'get' receives 1 argument (key)");
                                    System.out.println();
                                    continue;
                                }
                                dataOut.writeUTF(command);
                                dataOut.flush();
                                dataOut.writeUTF(tokens[1]);
                                dataOut.flush();

                                int nBytes = dataIn.readInt();
                                if (nBytes==0) {
                                    System.out.println("Key '" + tokens[1] + "' not found.");
                                    System.out.println();
                                }
                                else {
                                    byte[] value = dataIn.readNBytes(nBytes);
                                    String valueStr = new String(value);
                                    System.out.println("Value found: " + valueStr);
                                }
                            }
                            // INVALID
                            else {
                                System.out.println("Invalid command!");
                                System.out.println();
                                continue;
                            }

            
                        }
                        break;
                    } else {
                        System.out.println("Invalid credentials!");
                    }
                }
            }

            // close connection
            socket.shutdownOutput();
            socket.shutdownInput();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}