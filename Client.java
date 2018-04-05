import java.io.*;

public class Client
{
    static final int FTPport = 21; //For commands

    public static void main(String[] args) {
        try {
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("*** Welcome to FTPclient ***");

            String downloadPath = "D:/Downloads/FromFTP/";
//          if (inputBuffer.matches("^exit") || inputBuffer.matches("^quit"))
//              exitFlag = true;
//          else
//          {

            //TODO add params input
            String serverIP = "localhost";
            String user = "anonymous";
            String pass = "gelinislakavimas@one.lt";

            ServerConnection CMDconnection = new ServerConnection(serverIP, FTPport);

            CMDconnection.GetServerResponse();

            System.out.println("- Attempting anonymous login");
            System.out.println("localhost:21\nuser: 'anonymous'\npass: [email adress]"); //UNDONE t
            CMDconnection.SendToServer("USER " + user);
            CMDconnection.SendToServer("PASS " + pass);

            while (CMDconnection.ReadServerResponse()) {
            }

            //UNDONE special send commands
            ServerConnection dataConnection = CMDconnection.EnterPassiveMode(true);

            CMDconnection.SendToServer("LIST", true);
            CMDconnection.GetServerResponse();

            //----------------- User interaction block
            String inputBuffer = null;
            boolean exitFlag = false;
            while (!exitFlag) {
                CMDconnection.ReadServerResponse();
                dataConnection.ReadServerResponse();

                System.out.print(">");
                inputBuffer = consoleReader.readLine();

                if (!inputBuffer.equals(""))
                    CMDconnection.SendToServer(inputBuffer);

//              inputBuffer = null;
//              outputBuffer = null;
            } //End of any User Interaction

            dataConnection.Close();
            CMDconnection.Close();
        }
        catch (Exception e){
            System.err.println("Error #9999: Unexpected exception: "+
                    e.getMessage());
        }

        System.out.println("*** Goodbye! ***");
    } //---- End of main
}
