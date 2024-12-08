package client;

import java.io.IOException;

class CommandHandler implements Runnable {
    private final Demultiplexer m;
    private final int tag;
    private final String command;
    private final String[] arguments;

    public CommandHandler(Demultiplexer m, int tag, String command, String... arguments) {
        this.m = m;
        this.tag = tag;
        this.command = command;
        this.arguments = arguments;
    }

    @Override
    public void run() {
        try {
            if (this.command.equals("put")) {
                this.handlePut();
            } else if (this.command.equals("get")) {
                this.handleGet();
            } else {
                System.err.println("Unsupported command: " + command);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error handling command (" + tag + "): " + e.getMessage());
        }
    }

    private void handlePut() throws IOException, InterruptedException {
        if (arguments.length != 2) {
            System.out.println("(" + tag + ") Invalid number of arguments for 'put'.");
            return;
        }

        String data = command + " " + arguments[0] + " " + arguments[1];
        m.send(tag, data.getBytes());

        byte[] response = m.receive(tag);
        String responseString = new String(response);
        System.out.println("(" + tag + ") " + responseString);
    }

    private void handleGet() throws IOException, InterruptedException {
        if (arguments.length != 1) {
            System.out.println("(" + tag + ") Invalid number of arguments for 'get'.");
            return;
        }

        String data = command + " " + arguments[0];
        m.send(tag, data.getBytes());


        byte[] response = m.receive(tag);
        String responseString = new String(response);


        if (responseString.isEmpty()) {
            System.out.println("(" + tag + ") Key not found.");
        } else {
            System.out.println("(" + tag + ") Value of key " + arguments[0] + ": " + responseString);
        }
    }

    /*
    try(int n = Integer.parseInt(tokens[1])) {
    } catch (NumberFormatException ex) {
        System.out.println("Invalid command!");
        continue;
    } 

    number += 1;
    if (tokens.length < 4 || rest.length != number) {
        System.out.println("Invalid arguments! Command 'multiPut' requires an number '" + (number) + "' of key-value pairs.");
        continue;
    }
    try(int n = Integer.parseInt(arguments[0])) {
    } catch (NumberFormatException ex) {
        System.out.println("Invalid command!");
        continue;
    } 

    number += 1;
    if (tokens.length < 3 || rest.length != number) {
        System.out.println("Invalid arguments! Command 'multiGet' requires an number '" + (number) + "' of keys.");
        continue;
    } */
}
