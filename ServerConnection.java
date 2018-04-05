import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

//TODO make father class that doesn't have Command methods

public class ServerConnection {
    Socket socket;
    PrintWriter server_out;
    BufferedReader server_in;

    String messagePrefix = "# ";

    public ServerConnection(String ip, int port) throws IOException {
            socket = new Socket(ip, port);
            try{
                server_out = new PrintWriter(socket.getOutputStream(), true);
                server_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            }
            catch (IOException e){
                System.err.println("Error #10: Failed to setup IOStreams: "+
                        e.getMessage());
            }
    }

    public ServerConnection(String ip, int port, String serverMessagePrefix) throws IOException {
        this(ip, port);
        this.messagePrefix = serverMessagePrefix;
    }

    public void PrintMessage(String message)
    {
        System.out.println(messagePrefix + message);
    }

    public String GetServerResponse()
    {
        String server_response = "";
        try {
            while (!server_in.ready()) {
                //just waits
            }
            while (server_in.ready()) {
                server_response += server_in.readLine();
            }
        }
        catch (IOException e)
        {
            System.err.println("Error #9998: Server response exception: "+ e.getMessage());
        }

        PrintMessage(server_response);
        return server_response;
    }

    public boolean ReadServerResponse()
    {
        try {
            if (server_in.ready()) {
                String server_response = server_in.readLine();
                while (server_in.ready())
                    server_response += "\n" + messagePrefix + server_in.readLine();
                PrintMessage(server_response);
                return true;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String SendToServer(String message)
    {
        server_out.println(message);

        String serverResponse = GetServerResponse();

        return serverResponse;
    }

    public String SendToServer(String message, boolean echo)
    {
        if (echo)
            System.out.println("- " + message);

        String serverResponse = SendToServer(message);
        return serverResponse;
    }

    public void Close() throws IOException {
        socket.close();
        server_out.close();
        server_in.close();
        return;
    }

    public ServerConnection EnterPassiveMode(boolean echo)
    {
        String message = "PASV";

        if (echo)
            System.out.println("- " + message);

        String serverResponse = SendToServer(message);

        if (serverResponse.matches("227 Entering Passive Mode \\(\\d+\\,\\d+\\,\\d+\\,\\d+\\,\\d+\\,\\d+\\)\\.") )
        {
            String ip_port = serverResponse.split("[\\(\\)]")[1];
            String[] numbers = ip_port.split("\\,");

            String ip = "";
            for (int i = 0; i<3 ; i++)
                ip += numbers[i]+".";
            ip += numbers[3];
//            System.out.print(ip);

            int port = (Integer.parseInt(numbers[4]) * 256 + Integer.parseInt(numbers[5]));
            System.out.println("- port: "+port);

            try {
                ServerConnection dataConnection = new ServerConnection(ip, port);
                return dataConnection;
            }
            catch (IOException e){
                    System.err.println("Error #4: Failed to open a dataConnection socket: " + e.getMessage());
            }
        }

        System.err.println("Error #3: Failed to enter passive mode: " + serverResponse);

        return null;
    }
}
