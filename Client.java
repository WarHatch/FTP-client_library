import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;

public class Client
{
    static final int FTPport = 21; //For commands

    private static void printHelp()
    {
        System.out.println(
                "RETR [FTPpathname] - downloads a file at pathname to your currently specified download directory\n" +
                "STOR [FTPpath] - uploads a file to the specified FTPpath\n" +
                "LIST - shows all files in current FTP directory\n" +
                "RNFR [FTPpathname] - specify which file you want to rename (follow up with RNTO)\n" +
                "RNTO [newpathname] - change file's name\n" +
                "CWD [FTPpath] - change working directory\n" +
                "RMD|MKD [FTPpath] - remove|make a directory at FTPpath\n" +
                "PWD - prints working directory name\n" +
                "program also recognizes some other standard FTP commands\n" +
                "QUIT - disconnects from the FTP server\n" +
                "EXIT or CLOSE - exits the program"
        );
    }

    public static void main(String[] args) {
        try {
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("*** Welcome to FTPclient ***");
            printHelp();

//          if (inputBuffer.matches("^exit") || inputBuffer.matches("^quit"))
//              exitFlag = true;
//          else
//          {

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
                    else
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
