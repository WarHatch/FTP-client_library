import java.io.*;
import java.net.Socket;

public class Client
{
    static int serverConnectPort = 21; //The FTP port
    static int serverDataPort = -1;

    private static String GetServerResponse(BufferedReader server_input)
    {
        String server_response = null;
        try {
            while (!server_input.ready()) {
                //just waits
            }
            while (server_input.ready()) {
                server_response = server_input.readLine();
                System.out.println("# " + server_response);
            }
        }
        catch (IOException e)
        {
            System.err.println("Error #9998: Server response exception: "+ e.getMessage());
        }

        return server_response;
    }

    private static String SendToServer(PrintWriter server_output, BufferedReader server_input, String message)
    {
        server_output.println(message);

        String serverResponse = GetServerResponse(server_input);

        if (message.equals("PASV")) {
            if (serverResponse.matches("227 Entering Passive Mode \\(\\d+\\,\\d+\\,\\d+\\,\\d+\\,\\d+\\,\\d+\\)\\.") )
            {
                String ip_port = serverResponse.split("[\\(\\)]")[1];
                String[] numbers = ip_port.split("\\,");

                String ip = "";
                for (int i = 0; i<3 ; i++)
                    ip += numbers[i]+".";
                ip += numbers[3];
                //System.out.print(ip);

                int port = (Integer.parseInt(numbers[4]) * 256 + Integer.parseInt(numbers[5]));
                serverDataPort = port;
                System.out.println("- port: "+port);
            }
        }

        return serverResponse;
    }

    private static String SendToServer(PrintWriter server_output, BufferedReader server_input, String message, boolean echo)
    {
        if (echo)
            System.out.println("- " + message);

        String serverResponse = SendToServer(server_output, server_input, message);
        return serverResponse;
    }

    public static void main(String[] args) {
        try {
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("*** Welcome to FTPclient ***");

            String downloadPath = "D:/Downloads/FromFTP/";
//          if (inputBuffer.matches("^exit") || inputBuffer.matches("^quit"))
//              exitFlag = true;
//          else
//          {
            System.out.println("- Attempting anonymous login");

            //TODO add params input
            String server = "localhost";
            String user = "anonymous";
            String pass = "gelinislakavimas@one.lt";

            System.out.println("localhost:21\nuser: 'anonymous'\npass: [email adress]");

            Socket connectSocket = new Socket(server, serverConnectPort);
            PrintWriter server_out = new PrintWriter(connectSocket.getOutputStream(), true);
            BufferedReader server_in = new BufferedReader(new InputStreamReader(connectSocket.getInputStream()));

            String serverPrefix = "# ";
            String connection_response = server_in.readLine();
            System.out.println(serverPrefix + connection_response);

            SendToServer(server_out, server_in, "USER anonymous");
            SendToServer(server_out, server_in, "PASS satelistas@gmail.com");

            //UNDONE
            SendToServer(server_out, server_in, "PASV", true);
            Socket dataSocket = new Socket(server, serverDataPort);
            PrintWriter data_out = new PrintWriter(dataSocket.getOutputStream(), true);
            BufferedReader data_in = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

            //Might get a connection confirmation
            while (data_in.ready())
                GetServerResponse(data_in);

            SendToServer(server_out, server_in, "LIST", true);
            while (server_in.ready())
                GetServerResponse(server_in);

            //----------------- User interaction block
            String inputBuffer = null;
            boolean exitFlag = false;
            while (!exitFlag) {
                while (server_in.ready())
                    GetServerResponse(server_in);
                while (data_in.ready())
                    GetServerResponse(data_in);

                System.out.print(">");
                inputBuffer = consoleReader.readLine();

                if (!inputBuffer.equals(""))
                    SendToServer(server_out, server_in, inputBuffer);

//              inputBuffer = null;
//              outputBuffer = null;
            } //End of any User Interaction
            server_in.close();
            server_out.close();
            connectSocket.close();
            dataSocket.close();
            data_out.close();
            data_in.close();
        }
        catch (Exception e){
            System.err.println("Error #9999: Unexpected exception: "+
                    e.getMessage());
        }
        System.out.println("*** Goodbye! ***");
    } //---- End of main
}
