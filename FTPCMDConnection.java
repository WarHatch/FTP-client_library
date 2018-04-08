import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.*;
import java.net.Socket;
import java.text.ParseException;
import java.util.regex.Pattern;

@SuppressWarnings("StatementWithEmptyBody")
public class FTPCMDConnection extends ServerConnection implements Closeable {

    //TODO add a dataCommand Hash?
    private static final String[] dataCommands = {"STOR", "RETR"};

    FTPCMDConnection(String ip, int port) {
        super(ip, port);
    }

    FTPCMDConnection(String ip, int port, String serverMessagePrefix) {
        super(ip, port, serverMessagePrefix);
    }

    public String WaitAndGetServerResponse(String specificResponse) throws IOException {
        String fittingResponse = null;

        long waitTime = 5000;
        long endWait = System.currentTimeMillis() + waitTime;

        String serverResponse = "";
        while (!(serverResponse.startsWith(specificResponse))) {
            if (System.currentTimeMillis() > endWait) {
                throw new IOException("Error #600: Response from server timeout at " + waitTime + "ms");
            }
            if (dataReader.ready()) {
                serverResponse = dataReader.readLine();
                PrintMessage(serverResponse);
            }
        }
        fittingResponse = serverResponse; //This only gets reached if search doesn't time-out


        return fittingResponse;
    }

    //TODO add a CMD input from console with CMD recognition
//    public String SendToServer(String message)
//    {
////        if (isDataCommand(message))
////        {
////            //provide a dataConnection?
////            //use Function Hash?
////            throw new NotImplementedException();
////        }
////        else
//        dataWriter.println(message);
//
//        return WaitAndGetServerResponse();
//    }
//
//    private boolean isDataCommand(String message)
//    {
//        String command = message.split(" ", 1)[0];
//        for (String dataCommand : dataCommands) {
//            if (dataCommand.equals(command))
//                return true;
//        }
//
//        return false;
//    }

    private ServerConnection EnterPassiveMode(boolean echo)
    {
        String message = "PASV";

        if (echo)
            System.out.println("- " + message);

        // Custom variant of SendToServer(message) since a SPECIFIC response is REQUIRED;
        dataWriter.println(message);
        try {
            String serverResponse = WaitAndGetServerResponse("227");

            String passiveModePattern = "^227.*\\(?\\d+,\\d+,\\d+,\\d+,\\d+,\\d+\\)?\\.?";
            if (serverResponse.matches(passiveModePattern)) {
                String ip_port = serverResponse.split("[()]")[1]; //Possible miscomunication if reply doesn't have parenthesis
                String[] numbers = ip_port.split(",");

                String ip = "";
                for (int i = 0; i < 3; i++)
                    ip += numbers[i] + ".";
                ip += numbers[3];
                //System.out.print(ip);

                int port = (Integer.parseInt(numbers[4]) * 256 + Integer.parseInt(numbers[5]));
                System.out.println("- Data connection on port: " + port);

                ServerConnection dataConnection = new ServerConnection(ip, port, port + "#");
                return dataConnection;
            }
        }
        catch (IOException e)
        {
            System.err.println("Error #3: Failed to enter passive mode.");
        }

        return null;
    }

    public String DownloadFileList ()
    {
        ServerConnection dataConnection = EnterPassiveMode(false);
        SendToServer("LIST", true);
        ReadServerResponse();

        String response = null;
        try {
            response = dataConnection.WaitAndGetServerResponse();
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
        }

        return response;
    }

    public boolean DownloadFile(String fileName, String downloadPath)
    {
        boolean result = false;

        File outFile = new File(downloadPath+fileName);
        try {
            if(outFile.exists()) {
                outFile.delete();
            }
            outFile.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(outFile);

            ServerConnection dataConnection = this.EnterPassiveMode(true); //retrieves a port/IStream that the data will be sent to
            InputStream dataInStream = dataConnection.getServer_in();

            SendToServer("RETR " + fileName, true);

            int bytesRead = -1;
            while ((bytesRead = dataInStream.read(dataBuff)) != -1) {
                fileOutputStream.write(dataBuff, 0, bytesRead);
            }

            dataConnection.close();
            fileOutputStream.close();
            dataInStream.close();
        }
        catch (IOException e) {
            System.out.println("Error #9998: Unexpected Error downloading the file: " +e);
        }
        finally {
            if (outFile.length() != 0)
            {
                System.out.println("- Received file " + fileName);
                result = true;
            } else {
                System.err.println("- Failed to download file " + fileName);
                outFile.delete();
            }
        }
        return result;
    }

    public boolean UploadFile(String filePath, String fileName, String uploadPath)
    {
        boolean result = false;

        File sendFile = new File(filePath+fileName);
        try {
            if(!sendFile.exists()) {
                throw new FileNotFoundException(sendFile.getAbsolutePath());
            }
            FileInputStream fileInputStream = new FileInputStream(sendFile);

            ServerConnection dataConnection = this.EnterPassiveMode(true); //retrieves a port that the data will be sent through
            OutputStream dataOutStream = dataConnection.getServer_out();

            SendToServer("STOR " + uploadPath+fileName, true); //

            int bytesRead = -1;
            while ((bytesRead = fileInputStream.read(dataBuff)) != -1) {
                dataOutStream.write(dataBuff, 0, bytesRead);
            }

            dataConnection.close();
            fileInputStream.close();
            dataOutStream.close();

            String EOUploadResponse = WaitAndGetServerResponse("226");
            if (EOUploadResponse.startsWith("226")) //If it's a "transfer complete" response
            {
                result = true;
                System.out.println("- Uploaded file " + fileName);
            }
            else
                System.err.println("Warning #226: Server did not close the dataConnection or file did not upload correctly.");
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Error #4: Couldn't find the file specified: " +e.getMessage());
        }
        catch (IOException e) {
            System.err.println("Error #9998: Unexpected Error downloading the file: " +e);
        }

        return result;
    }
}
