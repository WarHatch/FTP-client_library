import java.io.*;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class Client {
    private static void showServerReply(FTPClient ftpClient) {
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
                System.out.println("SERVER: " + aReply);
            }
        }
    }

    public static void main(String[] args) {
        String server = "localhost";
        int port = 21;
        String user = "anonymous";
        String pass = "";

        String downloadPath = "D:/Downloads/FromFTP/";

        FTPClient ftpClient = new FTPClient();
        try {

            ftpClient.connect(server, port);
            showServerReply(ftpClient);
            ftpClient.login(user, pass);
            showServerReply(ftpClient);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // APPROACH #1: using retrieveFile(String, OutputStream)
            String remoteFile1 = "/textFile.txt";
            File downloadFile1 = new File(downloadPath + "downloadedFile.txt");
            OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile1));
            boolean success = ftpClient.retrieveFile(remoteFile1, outputStream1);
            showServerReply(ftpClient);

            outputStream1.close();

            if (success) {
                System.out.println("File #1 has been downloaded successfully.");
            }
            else {
                System.out.println("File has not been downloaded successfully. Returned: "+ success);
            }

            // APPROACH #2: using InputStream retrieveFileStream(String)
            String remoteFile2 = "/sortgif.gif";
            File downloadFile2 = new File(downloadPath + "gotGif.gif");
            OutputStream outputStream2 = new BufferedOutputStream(new FileOutputStream(downloadFile2));
            InputStream inputStream = ftpClient.retrieveFileStream(remoteFile2);
            byte[] bytesArray = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(bytesArray)) != -1) {
                outputStream2.write(bytesArray, 0, bytesRead);
            }

            showServerReply(ftpClient); //INSPECTING
            success = ftpClient.completePendingCommand();
            showServerReply(ftpClient); //INSPECTING
            if (success) {
                System.out.println("File #2 has been downloaded successfully.");
            }
            outputStream2.close();
            inputStream.close();

        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        //##############################################################

        try {
            String inputBuffer = null;
            boolean exitFlag = false;

            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("*** Welcome to FTPclient ***");
            while (!exitFlag) {
                System.out.print("> ");
                inputBuffer = consoleReader.readLine();

                if (inputBuffer.matches("^exit") || inputBuffer.matches("^quit"))
                    exitFlag = true;
                else
                {
                    System.out.println("# Attempting anonymous login");
                    
                }
            }
        }
        catch (Exception e){
            System.err.println("Error #9999: Unexpected exception: "+
                    e.getMessage());
        }
    } //---- End of main
}
