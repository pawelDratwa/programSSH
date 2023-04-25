import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {
    private CredentialManager credentialManager = new CredentialManager();

    public MainFrame(){
        setTitle("Choose connection");

        setSize(1000, 600);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();

        JLabel hostLabel = new JLabel("Host");
        panel.add(hostLabel);

        JTextField hostField = new JTextField(30);
        hostField.setText("testpawel@testpawel-pc");
        panel.add(hostField);

        JLabel passwordLabel = new JLabel("Password");
        panel.add(passwordLabel);

        JPasswordField passwordField = new JPasswordField(30);
        panel.add(passwordField);

        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SshFrame sshFrame = new SshFrame(hostField.getText(), new String(passwordField.getPassword()));
            }
        });
        panel.add(connectButton);

        JList credentialsList = new JList(new CredentialsListModel(credentialManager));

        JButton addCredentialButton = new JButton("Save");
        addCredentialButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                credentialManager.addCredential(new Credential(hostField.getText(), new String(passwordField.getPassword())));
                JOptionPane.showMessageDialog(null, "Host added");
                credentialsList.updateUI();
            }
        });
        panel.add(addCredentialButton);


        JScrollPane listScroller = new JScrollPane(credentialsList);
        listScroller.setPreferredSize(new Dimension(700, 150));
        panel.add(listScroller);

        JButton connectFromListButton = new JButton("Connect selected");
        connectFromListButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(credentialsList.getSelectedIndex() < 0)
                {
                    JOptionPane.showMessageDialog(null, "Host not selected!");
                    return;
                }

                Credential credential = (Credential) credentialsList.getSelectedValue();
               SshFrame sshFrame = new SshFrame(credential.getHost(), credential.getPassword());
            }
        });
        panel.add(connectFromListButton);

        getContentPane().add(panel);
        setVisible(true);

        credentialManager.loadCredentials();
        credentialsList.updateUI();
    }
}
