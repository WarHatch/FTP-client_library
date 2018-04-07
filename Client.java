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

            FTPCMDConnection CMDconnection = new FTPCMDConnection(serverIP, FTPport);

            CMDconnection.WaitAndGetServerResponse();

            System.out.println("- Attempting anonymous login");
            System.out.println("localhost:21\nuser: 'anonymous'\npass: [email adress]");
            CMDconnection.SendToServer("USER " + user);
            CMDconnection.SendToServer("PASS " + pass);

            CMDconnection.ReadServerResponse();

            //FIXME If gets 226 with 125 - freezes up (Replication: DEBUG with break in WaitAndGetServerResponse)
            CMDconnection.DownloadFileList();

            CMDconnection.DownloadFile("text.txt","D:\\Downloads\\FromFTP\\");
            CMDconnection.ReadServerResponse();

            //----------------- User interaction block
            String inputBuffer = null;
            boolean exitFlag = false;
            while (!exitFlag) {
                CMDconnection.ReadServerResponse();

                System.out.print(">");
                inputBuffer = consoleReader.readLine();

                if (!inputBuffer.equals("")) {
                    if (inputBuffer.matches("^close") || inputBuffer.matches("^exit"))
                        exitFlag = true;
                    else try {
                        CMDconnection.SendToServer(inputBuffer);
                    } catch (NotImplementedException e) {
                        System.err.println("Error 1000: Command not yet handleable: " + inputBuffer);
                    }
                }
            } //End of any User Interaction

            CMDconnection.Close();
        }
        catch (Exception e){
            System.err.println("Error #9999: Unexpected exception: "+
                    e.getMessage());
            e.printStackTrace();
        }

        System.out.println("*** Goodbye! ***");
    } //---- End of main
}
