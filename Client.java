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
            String downloadPath = "D:\\Downloads\\FromFTP\\";
            System.out.println("Your download path is: " + downloadPath);

            FTPCMDConnection CMDconnection = new FTPCMDConnection(serverIP, FTPport, downloadPath);

            CMDconnection.WaitAndGetServerResponse();

            System.out.println("- Attempting anonymous login");
            System.out.println("localhost:21\nuser: 'anonymous'\npass: [email adress]");
            CMDconnection.SendToServer("USER " + user);
            CMDconnection.SendToServer("PASS " + pass);

            CMDconnection.ReadServerResponse();

//            TESTING pre-written lines
//            CMDconnection.SendToServer("LIST", true);
//            CMDconnection.SendToServer("RETR text.txt", true);
//            //D:\Programavimas\JAVA\IdeaProjects\FTPclient\FromClient.txt
//            CMDconnection.SendToServer("STOR ", true); //Empty path arg

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
                    else if (inputBuffer.length() >= 4) //FTP commands are all 4 letters + [arguments]
                        try {
                            CMDconnection.SendToServer(inputBuffer);
                        } catch (NotImplementedException e) {
                            System.err.println("Error 1000: Command not yet handleable: " + inputBuffer);
                        }
                }
            } //End of any User Interaction

            CMDconnection.close();
        }
        catch (Exception e){
            System.err.println("Error #9999: Unexpected exception: "+
                    e.getMessage());
            e.printStackTrace();
        }

        System.out.println("*** Goodbye! ***");
    } //---- End of main
}
