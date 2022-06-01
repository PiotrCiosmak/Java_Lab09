import java.net.*;
import java.io.*;

public class Client
{
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;
    private static ClientGUI cg;
    private final String username;

    Client(String username, ClientGUI cg)
    {
        this.username = username;
        Client.cg = cg;
    }

    public boolean start()
    {
        try
        {
            String server = "localhost";
            int port = 1500;
            socket = new Socket(server, port);
        } catch (Exception ec)
        {
            display("Error connectiong to server:" + ec);
            return false;
        }

        String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
        display(msg);

        try
        {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException eIO)
        {
            display("Exception creating new Input/output Streams: " + eIO);
            return false;
        }

        new ListenFromServer().start();

        try
        {
            sOutput.writeObject(username);
        } catch (IOException eIO)
        {
            display("Exception doing login : " + eIO);
            disconnect();
            return false;
        }
        return true;
    }

    private void display(String msg)
    {
        if (cg == null)
            System.out.println(msg);
        else
            cg.append(msg + "\n");
    }

    void sendMessage(ChatMessage msg)
    {
        try
        {
            sOutput.writeObject(msg);
        } catch (IOException e)
        {
            display("Exception writing to server: " + e);
        }
    }

    private void disconnect()
    {
        try
        {
            if (sInput != null) sInput.close();
            if (sOutput != null) sOutput.close();
            if (socket != null) socket.close();
        } catch (Exception ignored)
        {
        }


    }

    public static void main(String[] args)
    {
        String userName = "User";
        switch (args.length)
        {
            case 1:
                userName = args[0];
            case 0:
                break;
            default:
                System.out.println("Usage is: > java Client [username] [portNumber] {serverAddress]");
                return;
        }
        Client client = new Client(userName, cg);
        if (!client.start())
            return;
    }

    class ListenFromServer extends Thread
    {

        public void run()
        {
            while (true)
            {
                try
                {
                    String msg = (String) sInput.readObject();
                    if (cg == null)
                    {
                        System.out.println(msg);
                        System.out.print("> ");
                    } else
                    {
                        cg.append(msg);
                    }
                } catch (IOException e)
                {
                    display("Server has close the connection: " + e);
                    break;
                } catch (ClassNotFoundException e2)
                {
                }
            }
        }
    }
}