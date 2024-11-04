
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainClient {
    public static void main(String[] args) {
        try {

            Socket socket = new Socket("localhost", 12345);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));
            


            String response = in.readLine(); // Welcome message
            if (response == null) {
                socket.shutdownOutput();
                socket.shutdownInput();
                socket.close();
                return;
            }
            System.out.println(response);
            
            
// ------------------ Register and Log in ----------------------
            
            System.out.println("Username: ");
            String username = systemIn.readLine();
            if (username == null) {
                socket.shutdownOutput();
                socket.shutdownInput();
                socket.close();
                return;
            }
            out.println(username);
            out.flush();


            System.out.println("Password: ");
            String password = systemIn.readLine();
            if (password == null) {
                socket.shutdownOutput();
                socket.shutdownInput();
                socket.close();
                return;
            }
            out.println(password);
            out.flush();

            response = in.readLine();
// -----------------------------------------------------------

            if (response.equals("sucess")) { // if valid credentials
                System.out.println("Logged Sucessfully! You can start writing!");
                String userInput;
                while ((userInput = systemIn.readLine()) != null) { //do something
                    out.println(userInput);
                    out.flush();

                    if (userInput.equals("end")) break;

                    response = in.readLine();
                    System.out.println("Server: " + response);
                }
            }
            else {
                System.out.println("Invalid Credentials!");
            }
                        
            socket.shutdownOutput();

            socket.shutdownInput();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
