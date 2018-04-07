import java.io.*;
import java.net.Socket;

public class ServerConnection {
    protected Socket socket;
    protected OutputStream server_out;
    protected InputStream server_in;
    BufferedReader dataReader;
    PrintWriter dataWriter;

    protected byte[] dataBuff = new byte[4096];
    protected String messagePrefix = "# ";

    public ServerConnection(String ip, int port) {
        try{
            socket = new Socket(ip, port);
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

    public ServerConnection(String ip, int port, String messagePrefix) {
        this(ip, port);
        this.messagePrefix = messagePrefix;
    }

    public InputStream getServer_in() {
        return server_in;
    }

    protected void PrintMessage(String message)
    {
        System.out.println(messagePrefix + message);
    }

    protected boolean EOOperation(String serverResponse) {
        return serverResponse.startsWith("2") || serverResponse.startsWith("4") || serverResponse.startsWith("5");
    }

    public String WaitAndGetServerResponse()
    {
        String serverResponse = null;

        long waitTime = 5000;
        long endWait = System.currentTimeMillis() + waitTime;
        try {
            while (!dataReader.ready()) {
                if (System.currentTimeMillis() > endWait) {
                    throw new IOException("Response from server timeout at "+ waitTime +"ms");
                }
            }
            serverResponse = dataReader.readLine();
            while (dataReader.ready()) {
                String anotherResponse = dataReader.readLine();
                serverResponse += "\n" + anotherResponse;
            }
        }
        catch (IOException e)
        {
            System.err.println("Warning #300: Server response exception: "+ e.getMessage());
        }

        PrintMessage(serverResponse);
        return serverResponse;
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
}
