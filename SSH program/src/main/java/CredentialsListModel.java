import lombok.AllArgsConstructor;

import javax.swing.*;
import javax.swing.event.ListDataListener;

@AllArgsConstructor
public class CredentialsListModel implements ListModel {
    private CredentialManager credentialManager;

    @Override
    public int getSize() {
        return credentialManager.getCredentials().size();
    }

    @Override
    public Object getElementAt(int index) {
        return credentialManager.getCredentials().get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {

    }

    @Override
    public void removeListDataListener(ListDataListener l) {

    }
}
