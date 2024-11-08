import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;


public class MainClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream());
             BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));
             DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
             DataInputStream dataIn = new DataInputStream(socket.getInputStream())) {

            // ----------- Register and Login ------------------
            System.out.println(in.readLine());  // Welcome 


            int option = 0;
            String regOrLogOption;
            String regOrLogMenu;
            
            // Prompt for register or login option
            while (true) {

                System.out.println("Write 'r' to register new account or 'l' to log in to your account.");  // Register/Login prompt
                
                regOrLogOption = systemIn.readLine();
                if (regOrLogOption == null) {
                    break;
                }
                System.out.println();

                out.println(regOrLogOption);
                out.flush();

                regOrLogMenu = in.readLine();
                if (regOrLogMenu.equals("register")) {
                    System.out.println("= Create New Account =");
                    option = 1;
                } else if (regOrLogMenu.equals("login")) {
                    System.out.println("= Login =");
                    option = 2;
                } else {
                    System.out.println("Invalid option!");
                    continue;
                }

                // Prompt for username
                System.out.println("Username: ");
                String username = systemIn.readLine();
                out.println(username);
                out.flush();

                // Prompt for password
                System.out.println("Password: ");
                String password = systemIn.readLine();
                out.println(password);
                out.flush();

                // ------------ Create new account ----------------
                if (option == 1) {
                    System.out.println(in.readLine());
                }
                // ------------- Log in --------------
                if (option == 2) {
                    // Authentication
                    System.out.println("Waiting for server to authenticate user...");
                    String response = in.readLine();
                    if (response == null) break;

                    else if (response.equals("success")) { // If authenticated start data management
                        
                        String object = in.readLine(); // Type of the objects of the shared map
                        if (object == null) break;

                        

                        System.out.println("Logged in successfully!");
                        System.out.println();
                        

                        String userInput;

                        // Do something
                        while (true) {
                            
                            System.out.println("Enter a command ('help' to list commands):");

                            userInput = systemIn.readLine(); // Read command
                            if (userInput == null) break;
                            System.out.println();

                            if (userInput.equals("help")) { // Help commands - list all commands
                                System.out.println("List of commands:");
                                System.out.println("help - List all commands.");
                                System.out.println("put(String key, byte[] value) - Adds or updates a single key-value pair in the server.");
                                System.out.println("get(String key) - Retrieves the value associated with the given key, or returns null if the key does not exist.");
                                System.out.println("multiPut(Map<String, byte[]> mapValues) - Adds or updates multiple key-value pairs in the server.");
                                System.out.println("multiGet(Set<String> keys) - Retrieves values for the specified keys and returns them as a map.");
                                System.out.println("end - End program");
                                System.out.println();
                                continue;
                            }
                            


                            else if (userInput.equals("end")) { 
                                out.println(userInput);
                                out.flush();
                                break;
                            }

                            String[] tokens = userInput.split(" ");
                            
                            if (tokens.length == 0 || tokens[0].isEmpty()) {
                                System.out.println("Invalid command!");
                                System.out.println();
                                continue;
                            }

                            String command = tokens[0];
                            
                            // Send command
                            out.println(command);
                            out.flush();

                            String commandOutput = in.readLine();
                            if (commandOutput == null) {
                                break;
                            }
                            else if (commandOutput.equals("invalid")) {
                                System.out.println("Invalid command!");
                                System.out.println();
                                continue;
                            }

                            System.out.println("Server: " + commandOutput);
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

