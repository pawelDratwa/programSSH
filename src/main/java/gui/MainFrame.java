package gui;

import command.Command;
import command.CommandManager;
import command.CommandsListModel;
import credential.Credential;
import credential.CredentialManager;
import credential.CredentialsListModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {
    private final CredentialManager credentialManager;
    private final CommandManager commandManager;

    public MainFrame(CredentialManager credentialManager, CommandManager commandManager){
        this.credentialManager = credentialManager;
        this.commandManager = commandManager;
        setTitle("Choose connection");

        setSize(800, 600);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        //Panel główny
        JPanel panel = new JPanel();

        //Panel nr 1 na elementy związane z ddoawaniem hosta/hasła i łączeniem
        JPanel connectPanel = new JPanel();
        panel.add(connectPanel);
        connectPanel.setLayout(new BoxLayout(connectPanel, BoxLayout.PAGE_AXIS));

        JLabel hostLabel = new JLabel("Host");
        connectPanel.add(hostLabel);

        JTextField hostField = new JTextField(30);
        connectPanel.add(hostField);

        JLabel passwordLabel = new JLabel("Password");
        connectPanel.add(passwordLabel);

        JPasswordField passwordField = new JPasswordField(30);
        connectPanel.add(passwordField);

        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SshFrame sshFrame = new SshFrame(commandManager, hostField.getText(), new String(passwordField.getPassword()));
            }
        });

        //Panel nr 2 na liste zapisanych polaczen oraz przyciski usuwania polaczenia i jego ustanowienia

        JList credentialsList = new JList(new CredentialsListModel(this.credentialManager));

        JButton addCredentialButton = new JButton("Save");
        addCredentialButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.credentialManager.addCredential(new Credential(hostField.getText(), new String(passwordField.getPassword())));
                credentialsList.updateUI();
            }
        });

        JPanel connectButtons = new JPanel();
        connectPanel.add(connectButtons);
        connectButtons.add(connectButton);
        connectButtons.add(addCredentialButton);

        JScrollPane listScroller = new JScrollPane(credentialsList);
        listScroller.setPreferredSize(new Dimension(700, 150));

        //Tworzenie panelu z dwoma dolnymi przyciskami - Connect selected oraz Delete

        JPanel credentialsAddPanel = new JPanel();
        panel.add(credentialsAddPanel);
        credentialsAddPanel.setLayout(new BoxLayout(credentialsAddPanel, BoxLayout.PAGE_AXIS));
        credentialsAddPanel.add(listScroller);

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
               SshFrame sshFrame = new SshFrame(commandManager, credential.getHost(), credential.getPassword());
            }
        });


        JButton deleteCredentialButton = new JButton("Delete");
        deleteCredentialButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(credentialsList.getSelectedIndex() < 0)
                {
                    JOptionPane.showMessageDialog(null, "Credential not selected!");
                    return;
                }

                Credential credential = (Credential) credentialsList.getSelectedValue();
                credentialManager.removeCredential(credential);
                credentialsList.updateUI();
            }
        });

        JPanel credentialsButtonsPanel = new JPanel();
        credentialsAddPanel.add(credentialsButtonsPanel);
        credentialsButtonsPanel.add(connectFromListButton);
        credentialsButtonsPanel.add(deleteCredentialButton);

        //Panel nr 3 zwiazany z lista komend

        JList commandsList = new JList(new CommandsListModel(this.commandManager));
        JScrollPane listScroller2 = new JScrollPane(commandsList);
        listScroller2.setPreferredSize(new Dimension(700, 150));

        JPanel commandsPanel = new JPanel();
        panel.add(commandsPanel);
        commandsPanel.setLayout(new BoxLayout(commandsPanel, BoxLayout.PAGE_AXIS));
        commandsPanel.add(listScroller2);

        JLabel commandLabel = new JLabel("Command");
        commandsPanel.add(commandLabel);

        JTextField commandField = new JTextField(30);
        commandsPanel.add(commandField);

        //Dodatkowy panel na przyciski - Delete oraz Add

        JButton addCommandButton = new JButton("Add");
        addCommandButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainFrame.this.commandManager.addCommand(new Command(commandField.getText()));
                commandsList.updateUI();
            }
        });

        JPanel commandsButtonsPanel = new JPanel();
        commandsPanel.add(commandsButtonsPanel);
        commandsButtonsPanel.add(addCommandButton);

        JButton deleteCommandButton = new JButton("Delete");
        deleteCommandButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(commandsList.getSelectedIndex() < 0)
                {
                    JOptionPane.showMessageDialog(null, "Command not selected!");
                    return;
                }

                Command command = (Command) commandsList.getSelectedValue();
                commandManager.removeCommand(command);
                commandsList.updateUI();
            }
        });
        commandsButtonsPanel.add(deleteCommandButton);


        getContentPane().add(panel);
        setVisible(true);

        //ladowanie z pamieci danych logowania oraz komend a pozniej ich wyswietlenie w odpowiednich listach (updateUI)
        this.credentialManager.loadCredentials();
        this.commandManager.loadCommands();

        credentialsList.updateUI();
        commandsList.updateUI();
    }
}
