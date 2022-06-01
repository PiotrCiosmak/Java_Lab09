import java.io.*;

public class ChatMessage implements Serializable
{
    private final String message;

    ChatMessage(String message)
    {
        this.message = message;
    }

    String getMessage()
    {
        return message;
    }
}