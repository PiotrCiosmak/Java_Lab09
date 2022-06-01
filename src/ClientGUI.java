import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ClientGUI extends JFrame implements ActionListener
{
    private JLabel label;
    private JTextField tf;
    private JButton login;
    private JTextArea ta;
    private JButton send;
    private Client client;

    ClientGUI()
    {
        super("Chat Client");
        JPanel northPanel = new JPanel(new GridLayout(3, 1));

        label = new JLabel("Enter your nick", SwingConstants.LEFT);
        northPanel.add(label);
        tf = new JTextField("user");
        tf.setBackground(Color.WHITE);
        northPanel.add(tf);
        add(northPanel, BorderLayout.NORTH);

        ta = new JTextArea("You have entered to the chat\n", 80, 80);
        JPanel centerPanel = new JPanel(new GridLayout(1, 1));
        centerPanel.add(new JScrollPane(ta));
        ta.setEditable(false);
        add(centerPanel, BorderLayout.CENTER);

        login = new JButton("Login");
        login.addActionListener(this);

        send = new JButton("Send");
        send.addActionListener(this);
        send.setEnabled(false);

        JPanel southPanel = new JPanel();
        southPanel.add(login);
        southPanel.add(send);
        add(southPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 600);
        setVisible(true);
        tf.requestFocus();
    }

    void append(String str)
    {
        ta.append(str);
        ta.setCaretPosition(ta.getText().length() - 1);
    }

    public void actionPerformed(ActionEvent e)
    {
        Object o = e.getSource();
        if (o == send)
        {
            client.sendMessage(new ChatMessage(tf.getText()));
            tf.setText("");
            return;
        }

        if (o == login)
        {
            String username = tf.getText().trim();
            if (username.length() == 0)
                return;
            client = new Client(username, this);
            if (!client.start())
                return;
            tf.setText("");
            label.setText("Enter your message");

            login.setEnabled(false);
            send.setEnabled(true);
            tf.addActionListener(this);
        }
    }

    public static void main(String[] args)
    {
        new ClientGUI();
    }
}
