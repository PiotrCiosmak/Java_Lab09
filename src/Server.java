import java.io.*;
import java.net.*;
import java.util.*;

public class Server
{
    private static int uniqueId;
    private ArrayList<ClientThread> al;
    private int port;
    private boolean keepGoing;

    public Server(int port)
    {
        this.port = port;
        al = new ArrayList<>();
    }

    public void start()
    {
        keepGoing = true;
        try
        {
            ServerSocket serverSocket = new ServerSocket(port);

            while (keepGoing)
            {
                System.out.println("Server is waiting on port " + port);

                Socket socket = serverSocket.accept();
                if (!keepGoing)
                    break;
                ClientThread t = new ClientThread(socket);
                al.add(t);
                t.start();
            }
            try
            {
                serverSocket.close();
                for (ClientThread tc : al)
                {
                    try
                    {
                        tc.sInput.close();
                        tc.sOutput.close();
                        tc.socket.close();
                    } catch (IOException ignored)
                    {
                    }
                }
            } catch (Exception e)
            {
                System.out.println("Exception closing the server and clients: " + e);
            }
        } catch (IOException e)
        {
            String msg = " Exception on new ServerSocket: " + e + "\n";
            System.out.println(msg);
        }
    }

    private synchronized void broadcast(String message)
    {
        String messageLf = message + "\n";
        for (int i = al.size(); --i >= 0; )
        {
            ClientThread ct = al.get(i);
            if (!ct.writeMsg(messageLf))
            {
                al.remove(i);
                System.out.println("Disconnected Client " + ct.username + " removed from list.");
            }
        }
    }

    synchronized void remove(int id)
    {
        for (int i = 0; i < al.size(); ++i)
        {
            ClientThread ct = al.get(i);
            if (ct.id == id)
            {
                al.remove(i);
                return;
            }
        }
    }

    public static void main(String[] args)
    {
        int portNumber = 1500;
        switch (args.length)
        {
            case 1:
                try
                {
                    portNumber = Integer.parseInt(args[0]);
                } catch (Exception e)
                {
                    System.out.println("Invalid port number.");
                    System.out.println("Usage is: > java Server [portNumber]");
                    return;
                }
            case 0:
                break;
            default:
                System.out.println("Usage is: > java Server [portNumber]");
                return;

        }
        // create a server object and start it
        Server server = new Server(portNumber);
        server.start();
    }

    class ClientThread extends Thread
    {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        ChatMessage cm;

        ClientThread(Socket socket)
        {
            id = ++uniqueId;
            this.socket = socket;
            System.out.println("Thread trying to create Object Input/Output Streams");
            try
            {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
                System.out.println(username + " just connected.");
            } catch (IOException e)
            {
                System.out.println("Exception creating new Input/output Streams: " + e);
                return;
            } catch (ClassNotFoundException ignored)
            {
            }
        }

        public void run()
        {
            while (true)
            {
                try
                {
                    cm = (ChatMessage) sInput.readObject();
                } catch (IOException e)
                {
                    System.out.println(username + " Exception reading Streams: " + e);
                    break;
                } catch (ClassNotFoundException e2)
                {
                    break;
                }
                String message = cm.getMessage();
                broadcast(username + ": " + message);
            }
            remove(id);
            close();
        }

        private void close()
        {
            try
            {
                sOutput.close();
                sInput.close();
                socket.close();
            } catch (Exception ignored)
            {
            }
        }

        private boolean writeMsg(String msg)
        {
            if (!socket.isConnected())
            {
                close();
                return false;
            }
            try
            {
                sOutput.writeObject(msg);
            } catch (IOException e)
            {
                System.out.println("Error sending message to " + username);
            }
            return true;
        }
    }
}