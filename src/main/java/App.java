import command.CommandManager;
import credential.CredentialManager;
import gui.MainFrame;

public class App {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainFrame mainFrame = new MainFrame(new CredentialManager(), new CommandManager());
            }
        });
    }
}
