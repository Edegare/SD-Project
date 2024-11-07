import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;


public class MainClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream());
             BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in))) {

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

                if (option == 1) {
                    System.out.println(in.readLine());
                }

                if (option == 2) {
                    // Authentication
                    System.out.println("Waiting for server response...");
                    String response = in.readLine();
                    if (response.equals("success")) {

                        System.out.println("Logged in successfully! You can start writing messages.");
                        String userInput;

                        // Do something
                        while ((userInput = systemIn.readLine()) != null) {
                            out.println(userInput);
                            out.flush();

                            if (userInput.equals("end")) break;

                            response = in.readLine();
                            System.out.println("Server: " + response);
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

