import java.io.*;
import java.net.Socket;

//TODO remove apache libraries
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPCmd;

public class Client
{
    private static String GetServerResponse(BufferedReader server_input)
    {
        String server_response = null;
        try {
            server_response = server_input.readLine();
            if (server_response == null) {
                System.err.println("Warning: No response gotten");
            }
        }
        catch (IOException e)
        {
            System.err.println("Error #9998: Server response exception: "+ e.getMessage());
        }
        System.out.println("# " + server_response);

        return server_response;
    }

    private static String SendToServer(PrintWriter server_output, BufferedReader server_input, String message)
    {
        server_output.println(message);

        return GetServerResponse(server_input);
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
            int port = 21;
            String user = "anonymous";
            String pass = "";
            System.out.println("localhost:21\nuser: 'anonymous'\npass:''");

            Socket socket = new Socket(server, port);
            PrintWriter server_out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader server_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String serverPrefix = "# ";
            String connection_response = server_in.readLine();
            System.out.println(serverPrefix + connection_response);

//TESTING
//            FTPClient ftpClient = new FTPClient();
//            ftpClient.connect(server, port); System.out.println(ftpClient.getReplyString());
//            ftpClient.sendCommand(FTPCmd.USER, user); System.out.println(ftpClient.getReplyString());

            SendToServer(server_out, server_in, "USER anonymous");
            SendToServer(server_out, server_in, "PASS satelistas@gmail.com");

            //Gets all server responses
            String server_response = server_in.readLine();
            while (!(server_response == null || server_response.equals("")))
            {
                System.out.println(server_response);
                server_response = server_in.readLine();
            }


//          String inputBuffer = null;
//          String outputBuffer = null;
//          boolean exitFlag = false;
//          while (!exitFlag) {
//              System.out.print("> ");
//              inputBuffer = consoleReader.readLine();
//
//              inputBuffer = null;
//              outputBuffer = null;
//          } //End of while for program

            server_in.close();
            server_out.close();
            socket.close();
        }
        catch (Exception e){
            System.err.println("Error #9999: Unexpected exception: "+
                    e.getMessage());
        }
    } //---- End of main
}
