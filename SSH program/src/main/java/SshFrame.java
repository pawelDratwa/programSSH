import com.jcraft.jsch.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class SshFrame extends JFrame {

    private Session session;
    private Channel channel;

    private PipedOutputStream pin;

    public SshFrame(String host, String password) {
        setTitle("SSH Terminal");
        setSize(1000, 600);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();

        JTextArea consoleTextArea = new JTextArea(30, 70);

        PrintStream printStream = new PrintStream(new CustomOutputStream(consoleTextArea));
        System.setOut(printStream);
        System.setErr(printStream);

        JScrollPane scroll = new JScrollPane (consoleTextArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        panel.add(scroll);

        JTextField commandField = new JTextField(60);
        panel.add(commandField);

        JButton commandButton = new JButton("Send");
        panel.add(commandButton);
        commandButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(channel != null && !channel.isClosed())
                {
                    try {
                        pin.write((commandField.getText() + "\n").getBytes(StandardCharsets.UTF_8));
                        commandField.setText("");
                    } catch (IOException ex) {
                        System.out.println(ex);
                    }
                }
            }
        });

        getContentPane().add(panel);

        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {

            }

            @Override
            public void windowClosed(WindowEvent e) {
                if(session != null)
                {
                    session.disconnect();
                }
            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });

        setVisible(true);

        connect(host, password);
    }

    public class CustomOutputStream extends OutputStream {
        private JTextArea textArea;

        public CustomOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(int b) throws IOException {
            // redirects data to the text area
            textArea.append(String.valueOf((char)b));
            // scrolls the text area to the end of data
            textArea.setCaretPosition(textArea.getDocument().getLength());
            // keeps the textArea up to date
            textArea.update(textArea.getGraphics());
        }
    }

    private void connect(String host, String password)
    {
        try {
            JSch jsch=new JSch();
            String user=host.substring(0, host.indexOf('@'));
            host=host.substring(host.indexOf('@')+1);
            session=jsch.getSession(user, host, 22);
            session.setPassword(password);

            UserInfo ui = new Main.MyUserInfo(){
                public void showMessage(String message){
                    JOptionPane.showMessageDialog(null, message);
                }
                public boolean promptYesNo(String message){
                    Object[] options={ "yes", "no" };
                    int foo=JOptionPane.showOptionDialog(null,
                            message,
                            "Warning",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);
                    return foo==0;
                }

            };

            session.setUserInfo(ui);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(30000);
            channel=session.openChannel("shell");

            InputStream in = new PipedInputStream();
            pin = new PipedOutputStream((PipedInputStream) in);

            channel.setInputStream(in);
            channel.setOutputStream(System.out);
            channel.connect(3*1000);
            JOptionPane.showMessageDialog(null, "Connected!");
        }  catch(Exception e){
            System.out.println(e);
        }

    }
}
