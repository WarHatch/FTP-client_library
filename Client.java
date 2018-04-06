import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;

public class Client
{
    static final int FTPport = 21; //For commands

    public static void main(String[] args) {
        try {
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("*** Welcome to FTPclient ***");

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
            System.out.println("localhost:21\nuser: 'anonymous'\npass: [email adress]");
            CMDconnection.SendToServer("USER " + user);
            CMDconnection.SendToServer("PASS " + pass);

            while (CMDconnection.ReadServerResponse()) {
            }

            ServerConnection dataConnection = CMDconnection.EnterPassiveMode(true);

            //FIXME messes up sometimes (det: The response message is in one line
            CMDconnection.SendToServer("LIST", true);
            CMDconnection.GetServerResponse();
            dataConnection.ReadServerResponse();


            CMDconnection.DownloadFile("textFile.txt", 18);
            CMDconnection.ReadServerResponse();
            CMDconnection.DownloadFile("sortgif.gif", 328089); //FIXME unhandled exception if size is too big (file received whole)
            CMDconnection.ReadServerResponse();

            //----------------- User interaction block
            String inputBuffer = null;
            boolean exitFlag = false;
            while (!exitFlag) {
                CMDconnection.ReadServerResponse();
                //dataConnection.ReadServerResponse();

                System.out.print(">");
                inputBuffer = consoleReader.readLine();

                if (!inputBuffer.equals(""))
                    try {
                        CMDconnection.SendToServer(inputBuffer);
                    }
                    catch (NotImplementedException e){
                        System.err.println("Error 1000: Command not yet handleable: " + inputBuffer);
                    }
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
