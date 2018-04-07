import java.io.*;
import java.net.Socket;

//TODO make father class that doesn't have Command methods

@SuppressWarnings("StatementWithEmptyBody")
public class FTPCMDConnection extends ServerConnection {

    //TODO add a dataCommand Hash?
    private static final String[] dataCommands = {"STOR", "RETR"};

    FTPCMDConnection(String ip, int port) {
        super(ip, port);
    }

    FTPCMDConnection(String ip, int port, String serverMessagePrefix) {
        super(ip, port, serverMessagePrefix);
    }

    public String WaitAndGetServerResponse(String specificResponse)
    {
        String fittingResponse = null;

        long waitTime = 5000;
        long endWait = System.currentTimeMillis() + waitTime;
        try {
            String serverResponse = "";
            while (!(serverResponse.startsWith(specificResponse))) {
                if (System.currentTimeMillis() > endWait) {
                    throw new IOException("Response from server timeout at " + waitTime + "ms");
                }
                if (dataReader.ready()) {
                    serverResponse = dataReader.readLine();
                    PrintMessage(serverResponse);
                }
            }
            fittingResponse = serverResponse; //This only gets reached if search doesn't time-out
        }
        catch (IOException e)
        {
            System.err.println("Warning #300: Server response exception: "+ e.getMessage());
        }

        return fittingResponse;
    }

//    public String SendToServer(String message)
//    {
//        //FIXME method gets called inside classes. Needs rework or different class
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

    //UNDONE needs rework
    private boolean isDataCommand(String message)
    {
        String command = message.split(" ", 1)[0];
        for (String dataCommand : dataCommands) {
            if (dataCommand.equals(command))
                return true;
        }

        return false;
    }

    private ServerConnection EnterPassiveMode(boolean echo)
    {
        String message = "PASV";

        if (echo)
            System.out.println("- " + message);

        // Custom variant of SendToServer(message) since a response is REQUIRED;
        dataWriter.println(message);
        String serverResponse = WaitAndGetServerResponse("227");

        if (serverResponse.matches("^227.*\\(?\\d+,\\d+,\\d+,\\d+,\\d+,\\d+\\)?\\.?") )
        {
            String ip_port = serverResponse.split("[()]")[1];
            String[] numbers = ip_port.split(",");

            String ip = "";
            for (int i = 0; i<3 ; i++)
                ip += numbers[i]+".";
            ip += numbers[3];
            //System.out.print(ip);

            int port = (Integer.parseInt(numbers[4]) * 256 + Integer.parseInt(numbers[5]));
            System.out.println("- Data connection on port: "+port);

            ServerConnection dataConnection = new ServerConnection(ip, port, port + "#");
            return dataConnection;
        }

        System.err.println("Error #3: Failed to enter passive mode. Server response: '" + serverResponse + "'");

        return null;
    }

    public String DownloadFileList ()
    {
        ServerConnection dataConnection = EnterPassiveMode(false);
        SendToServer("LIST", true);
        ReadServerResponse();

        return dataConnection.WaitAndGetServerResponse(); //TODO this is null-problematic
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
            InputStream dataInStream = dataConnection.getServer_in(); //TODO need to handle null exception

            SendToServer("RETR " + fileName, true);

            int bytesRead = -1;
            while ((bytesRead = dataInStream.read(dataBuff)) != -1) {
                fileOutputStream.write(dataBuff, 0, bytesRead);
            }

            fileOutputStream.close();
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

    //TODO add STOR command method
}
