package command;

import credential.CredentialManager;
import lombok.AllArgsConstructor;

import javax.swing.*;
import javax.swing.event.ListDataListener;

@AllArgsConstructor
public class CommandsListModel implements ListModel {
    private CommandManager commandManager;

    @Override
    public int getSize() {
        return commandManager.getCommands().size();
    }

    @Override
    public Object getElementAt(int index) {
        return commandManager.getCommands().get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {

    }

    @Override
    public void removeListDataListener(ListDataListener l) {

    }
}
