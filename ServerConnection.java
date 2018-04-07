import java.io.*;
import java.net.Socket;

//TODO make father class that doesn't have Command methods

@SuppressWarnings("StatementWithEmptyBody")
public class ServerConnection {
    private Socket socket;
    private OutputStream server_out;
    private InputStream server_in;
    BufferedReader dataReader;
    PrintWriter dataWriter;

    //TODO add a dataCommand Hash?
    private static final String[] dataCommands = {"STOR", "RETR"};
    private byte[] dataBuff = new byte[4096];
    private String messagePrefix = "# ";

    public InputStream getServer_in() {
        return server_in;
    }

    ServerConnection(String ip, int port) throws IOException {
            socket = new Socket(ip, port);
            try{
                server_out = socket.getOutputStream();
                server_in = socket.getInputStream();
                dataWriter = new PrintWriter(server_out, true);
                dataReader = new BufferedReader(new InputStreamReader(server_in));
            }
            catch (IOException e){
                System.err.println("Error #10: Failed to setup IOStreams: "+
                        e.getMessage());
            }
    }

    ServerConnection(String ip, int port, String serverMessagePrefix) throws IOException {
        this(ip, port);
        this.messagePrefix = serverMessagePrefix;
    }

    private void PrintMessage(String message)
    {
        System.out.println(messagePrefix + message);
    }

    public String WaitAndGetServerResponse()
    {
        String server_response = null;

        long waitTime = 5000;
        long endWait = System.currentTimeMillis() + waitTime;
        try {
            while (!dataReader.ready()) {
                if (System.currentTimeMillis() > endWait) {
                    throw new IOException("Response from server timeout at "+ waitTime +"ms");
                }
            }
            server_response = dataReader.readLine();
            while (dataReader.ready()) {
                server_response += "\n" + dataReader.readLine();
            }
        }
        catch (IOException e)
        {
            System.err.println("Warning #300: Server response exception: "+ e.getMessage());
        }

        PrintMessage(server_response);
        return server_response;
    }

    public boolean ReadServerResponse()
    {
        try {
            if (dataReader.ready()) {
                String server_response = dataReader.readLine();
                while (dataReader.ready())
                    server_response += "\n" + messagePrefix + dataReader.readLine();
                PrintMessage(server_response);
                return true;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String SendToServer(String message)
    {
        //FIXME method gets called inside classes. Needs rework or different class
//        if (isDataCommand(message))
//        {
//            //provide a dataConnection?
//            //use Function Hash?
//            throw new NotImplementedException();
//        }
//        else
            dataWriter.println(message);

        return WaitAndGetServerResponse();
    }

    public String SendToServer(String message, boolean echo)
    {
        if (echo)
            System.out.println("- " + message);

        return SendToServer(message);
    }

    public void Close() throws IOException {
        socket.close();
        dataReader.close();
        dataWriter.close();
        server_out.close();
        server_in.close();
    }

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

        String serverResponse = SendToServer(message);

        if (serverResponse.matches("227 Entering Passive Mode \\(\\d+\\,\\d+\\,\\d+\\,\\d+\\,\\d+\\,\\d+\\)\\.") )
        {
            String ip_port = serverResponse.split("[\\(\\)]")[1];
            String[] numbers = ip_port.split("\\,");

            String ip = "";
            for (int i = 0; i<3 ; i++)
                ip += numbers[i]+".";
            ip += numbers[3];
//            System.out.print(ip);

            int port = (Integer.parseInt(numbers[4]) * 256 + Integer.parseInt(numbers[5]));
            System.out.println("- Data connection on port: "+port);

            try {
                ServerConnection dataConnection = new ServerConnection(ip, port, port + "#");
                return dataConnection;
            }
            catch (IOException e){
                    System.err.println("Error #4: Failed to open a dataConnection socket: " + e.getMessage());
            }
        }

        System.err.println("Error #3: Failed to enter passive mode: " + serverResponse);

        return null;
    }

    public String DownloadFileList ()
    {
        ServerConnection dataConnection = EnterPassiveMode(false);
        SendToServer("LIST", true);
        WaitAndGetServerResponse();

        return dataConnection.WaitAndGetServerResponse();
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

            ServerConnection dataConnection = this.EnterPassiveMode(false); //retrieves a port/IStream that the data will be sent to
            InputStream dataInStream = dataConnection.getServer_in();

            SendToServer("RETR " + fileName, true);

            int bytesRead = -1;
            while ((bytesRead = dataInStream.read(dataBuff)) != -1) {
                fileOutputStream.write(dataBuff, 0, bytesRead);
            }
            //Alternative
//            long size = fileSize;
//            long len = 0;
//            int recv = 0;
//            if (size > 0) {
//                while (len + recv < size) {
//                    len += recv;
//                    recv = dataInStream.read(dataBuff,0,dataBuff.length);
//                    fileOutputStream.write(dataBuff,0,recv);
//                }
//            }

            fileOutputStream.close();
            if (outFile.length() != 0) {
                System.out.println("- Received file " + fileName);
                result = true;
            } else {
                System.err.println("- Failed to download file " + fileName);
                outFile.delete();
            }

        } catch (IOException e) {
            System.out.println("Error #9998: Unexpected Error downloading the file: " +e);
        }
        return result;
    }

    //TODO add STOR command method
}
