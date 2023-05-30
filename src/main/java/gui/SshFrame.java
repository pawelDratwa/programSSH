package gui;

import com.jcraft.jsch.*;
import command.Command;
import command.CommandManager;
import command.CommandsListModel;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SshFrame extends JFrame {

    private Session session;
    private Channel channel;

    private PipedOutputStream pin;

    private final CommandManager commandManager;

    public SshFrame(CommandManager commandManager, String host, String password) {
        this.commandManager = commandManager;

        setTitle("SSH Terminal");
        setSize(1000, 600);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        //Panel główny zawierajacy wszystkie elementy tego okna
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1));


        //Konsola
        JTextPane consoleTextArea = new JTextPane();
        Font font = new Font("Gill Sans", Font.PLAIN, 18);
        consoleTextArea.setFont(font);

        consoleTextArea.setForeground(Color.WHITE);
        consoleTextArea.setBackground(Color.BLACK);


        PrintStream printStream = new PrintStream(new CustomOutputStream(consoleTextArea));
        System.setOut(printStream);
        System.setErr(printStream);

        JScrollPane scroll = new JScrollPane(consoleTextArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setSize(1000, 300);
        panel.add(scroll);

        //Panel reprezentujacy operacje wysylania komend oraz wyboru komend z listy

        JPanel bottomPanel = new JPanel();
        panel.add(bottomPanel);

        //Panel wewnetrzny z mozliwosci wysylania dowolnie napisanej komendy
        JPanel sendPanel = new JPanel();
        bottomPanel.add(sendPanel);
        sendPanel.setLayout(new BoxLayout(sendPanel, BoxLayout.X_AXIS));

        JTextField commandField = new JTextField(60);
        sendPanel.add(commandField);

        JButton commandButton = new JButton("Send");
        sendPanel.add(commandButton);
        commandButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (channel != null && !channel.isClosed()) {
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
                if (session != null) {
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

        //Panel wewnetrzny zwiazany z wysylaniem predefiniowanych komend
        JList commandsList = new JList(new CommandsListModel(commandManager));

        JButton runCommandButton = new JButton("Run");
        runCommandButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (channel != null && !channel.isClosed()) {
                    try {
                        Command command = (Command) commandsList.getSelectedValue();
                        pin.write((command.getText() + "\n").getBytes(StandardCharsets.UTF_8));
                    } catch (IOException ex) {
                        System.out.println(ex);
                    }
                }
            }
        });

        JScrollPane listScroller = new JScrollPane(commandsList);
        listScroller.setPreferredSize(new Dimension(300, 150));

        JPanel sendPanel2 = new JPanel();
        sendPanel2.add(listScroller);
        sendPanel2.add(runCommandButton);
        bottomPanel.add(sendPanel2);

        setVisible(true);

        connect(host, password);
    }

    //Klasa do obslugi strumienia danych SSH, jest w stanie manipulować JTextPane w celu umieszczania w nim tekstu ze strumienia oraz odpowiednim jego kolorowaniu
    public class CustomOutputStream extends OutputStream {
        private JTextPane textPane;

        private List<String> data = new ArrayList<>();

        private boolean colorReading = false;

        private String colorStr = "";

        private static final Map<String, Color> colorsMap = Map.of(
                "[01;30m", Color.WHITE,
                "[01;31m", Color.RED,
                "[01;32m", Color.GREEN,
                "[01;33m", Color.YELLOW,
                "[01;34m", Color.BLUE,
                "[01;35m", Color.PINK,
                "[01;36m", Color.CYAN,
                //"[01;37m", Color.WHITE,
                "[0m", Color.WHITE,
                "[?2004h", Color.CYAN,
                "[?2004l", Color.WHITE
        );
        private SimpleAttributeSet textAttributes = new SimpleAttributeSet();

        public CustomOutputStream(JTextPane textPane) {
            this.textPane = textPane;
            StyleConstants.setForeground(textAttributes, Color.WHITE);
        }

        @Override
        public void write(int b) throws IOException {

            //Metoda write odczytuje jeden znak ze strumienia a nastepnie sprawdza czy nie jest on pewnym znakiem specjalnym (ESC) jesli tak to odczytuje ten znak oraz kolejne
            //a na koniec gdy dany ciag ANSI zakonczy sie wybrana litera (m h lub l) nastepuje uzycie odpowiedniego formatu (np zmiana koloru)
            //w przypadku zwyklych znakow sa od razu umieszczane w textPanel

            Document doc = textPane.getDocument();
            try {
                if (b == 27) {
                    colorStr = "";
                    colorReading = true;
                    return;
                }

                if (colorReading) {
                    colorStr += String.valueOf((char) b);
                    if (b == 'm' || b == 'h' || b == 'l') {
                        colorReading = false;
                        data.add(colorStr);
                        if (colorsMap.containsKey(colorStr)) {
                            Color color = colorsMap.get(colorStr);
                            StyleConstants.setForeground(textAttributes, color);
                        }
                    }
                } else {
                    doc.insertString(doc.getLength(), String.valueOf((char) b), textAttributes);
                }

            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void connect(String host, String password) {
        try {
            //Stworzenie obiektu polaczenia SSH, ustawienie w sesji odpowiedniego hosta i hasła

            JSch jsch = new JSch();
            String user = host.substring(0, host.indexOf('@'));
            host = host.substring(host.indexOf('@') + 1);
            session = jsch.getSession(user, host, 22);
            session.setPassword(password);

            //Skonfigurowanie akcji zwiazanej z wymiana kluczy SSL
            UserInfo ui = new MyUserInfo() {
                public void showMessage(String message) {
                    JOptionPane.showMessageDialog(null, message);
                }

                public boolean promptYesNo(String message) {
                    Object[] options = {"yes", "no"};
                    int foo = JOptionPane.showOptionDialog(null,
                            message,
                            "Warning",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);
                    return foo == 0;
                }

            };

            //nazwiazanie polaczenia
            session.setUserInfo(ui);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(30000);
            channel = session.openChannel("shell");

            //przekierowanie wyjscia konsoli do opowiedniego strumienia ktory pozniej bedzie wykorzystany przez JTextPane
            InputStream in = new PipedInputStream();
            pin = new PipedOutputStream((PipedInputStream) in);

            channel.setInputStream(in);
            channel.setOutputStream(System.out);
            channel.connect(3 * 1000);
            JOptionPane.showMessageDialog(null, "Connected!");
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public static abstract class MyUserInfo
            implements UserInfo, UIKeyboardInteractive{
        public String getPassword(){ return null; }
        public boolean promptYesNo(String str){ return false; }
        public String getPassphrase(){ return null; }
        public boolean promptPassphrase(String message){ return false; }
        public boolean promptPassword(String message){ return false; }
        public void showMessage(String message){ }
        public String[] promptKeyboardInteractive(String destination,
                                                  String name,
                                                  String instruction,
                                                  String[] prompt,
                                                  boolean[] echo){
            return null;
        }
    }
}
