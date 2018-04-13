import java.io.*;

@SuppressWarnings("StatementWithEmptyBody")
//Data connection are established using PASV
public class FTPCMDConnection extends ServerConnection {
    private BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

    String downloadPath = null;

    FTPCMDConnection(String ip, int port) {
        super(ip, port);
    }

    FTPCMDConnection(String ip, int port, String downloadPath) {
        super(ip, port);
        this.downloadPath = downloadPath;
    }

    private String WaitAndGetServerResponse(String specificResponse) throws IOException {
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

    private String extractArgs(String message) //Experimental
    {
        if (message.length() < 5)
            return "";
        return message.substring(5);
    }

    private String AskForPathname() throws IOException {
        System.out.println("Enter pathname of the file to upload");
        return consoleReader.readLine();
    }

    //If it's a command requiring to a open a data connection this class' method is called. Else ServerConnection method is called
    public String SendToServer(String message)
    {
        String serverResponse = null;

        String command = message.substring(0, Math.min(4, message.length()));
        command = command.toUpperCase();

        switch (command) {
            case "LIST":
                DownloadFileList();
                break;
            case "RETR":
                String args = extractArgs(message);
                if (args.length() > 0)
                    DownloadFile(downloadPath, extractArgs(message));
                else
                    System.out.println("You forgot to enter the pathname of file you're trying to download\n" +
                            "Try writing RETR [filename] again");
                break;
            case "STOR":
                try {
                    String filePath = AskForPathname();
                    UploadFile(filePath, extractArgs(message));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "STOU":
                try {
                    String filePath = AskForPathname();
                    UploadFile(filePath, extractArgs(message), true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                dataWriter.println(message);
                try {
                    serverResponse = WaitAndGetServerResponse();
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
                break;
        }
        return serverResponse;
    }

    public String SendToServer(String message, boolean echo)
    {
        if (echo)
            System.out.println("- " + message);

        return SendToServer(message);
    }

    private ServerConnection EnterPassiveMode(boolean echo) throws IOException {
        String message = "PASV";

        if (echo)
            System.out.println("- " + message);

        // Custom variant of SendToServer(message) since a SPECIFIC response is REQUIRED;
        dataWriter.println(message);

        String serverResponse = WaitAndGetServerResponse("227");

        String passiveModePattern = "^227.*\\(?\\d+,\\d+,\\d+,\\d+,\\d+,\\d+\\)?\\.?";
        if (serverResponse.matches(passiveModePattern)) {
            String ip_port = serverResponse.split("[()]")[1]; //Possible miscommunication if reply doesn't have parenthesis
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
        else throw new CharConversionException("Error #9981: Unexpected exception. " +
                "ServerResponse '" + serverResponse + "' doesn't match passiveModePattern '" + passiveModePattern + "'");
//        catch (IOException e)
//        {
//            System.err.println("Error #3: Failed to enter passive mode.");
//        }

    }

    public String DownloadFileList ()
    {
        try {
            ServerConnection dataConnection = EnterPassiveMode(false);
            dataWriter.println("LIST");
            ReadServerResponse();
            return dataConnection.WaitAndGetServerResponse();
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
        }

        return null;
    }

    private String ExtractFileName(String filePath)
    {
        int pointToFileName = filePath.lastIndexOf('\\') + 1;
        return filePath.substring(pointToFileName);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean DownloadFile(String downloadPath, String filePath)
    {
        boolean result = false;

        String fileName = ExtractFileName(filePath);
        File outFile = new File(downloadPath + fileName);
        try {
            if(outFile.exists()) {
                outFile.delete();
            }
            //If file is not successfully created it can't download
            if(!outFile.createNewFile()){
                throw new FileNotFoundException("Unable to download to specified path: '" +outFile.getAbsolutePath() + "'");
            }
            FileOutputStream fileOutputStream = new FileOutputStream(outFile);

            //retrieves a port/IStream that the data will be sent to
            ServerConnection dataConnection = this.EnterPassiveMode(true);
            InputStream dataInStream = dataConnection.getServer_in();

            dataWriter.println("RETR " + filePath);

            int bytesRead = -1;
            while ((bytesRead = dataInStream.read(dataBuff)) != -1) {
                fileOutputStream.write(dataBuff, 0, bytesRead);
            }

            dataConnection.close();
            fileOutputStream.close();
            dataInStream.close();
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Error #7: " +e);
        }
        catch (IOException e) {
            System.err.println("Error #9998: Unexpected Error downloading the file: " +e);
            e.printStackTrace();
        }

        finally {
            if (outFile.length() != 0)
            {
                System.out.println("- Received file '" + fileName + "'");
                result = true;
            } else {
                System.err.println("- Failed to download file '" + fileName + "'");
                outFile.delete();
            }
        }
        return result;
    }

    private boolean UploadFile(String filePath, String uploadPath)
    {
        return UploadFile(filePath, uploadPath, false);
    }

    public boolean UploadFile(String filePath, String uploadPath, boolean unique)
    {
        boolean result = false;
        String fileName = ExtractFileName(filePath);

        File sendFile = new File(filePath);
        try {
            if(!sendFile.exists()) {
                throw new FileNotFoundException(sendFile.getAbsolutePath());
            }
            FileInputStream fileInputStream = new FileInputStream(sendFile);

            ServerConnection dataConnection = this.EnterPassiveMode(true); //retrieves a port that the data will be sent through
            OutputStream dataOutStream = dataConnection.getServer_out();

            if (unique)
                dataWriter.println("STOU " + uploadPath+fileName);
            else
                dataWriter.println("STOR " + uploadPath+fileName);

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
                System.err.println("Error #226: Server did not close the dataConnection or file did not upload correctly.");
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Error #4: Couldn't find the file specified: " +e.getMessage());
        }
        catch (IOException e) {
            System.err.println("Error #9997: Unexpected Error uploading the file: " +e);
        }

        return result;
    }
}
