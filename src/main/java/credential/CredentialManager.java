package credential;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import lombok.Getter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;

public class CredentialManager {
    private static final String CREDENTIALS_FILENAME = "credentials.json";
    private static final Type CREDENTIAL_TYPE = new TypeToken<List<Credential>>() {}.getType();

    @Getter
    private List<Credential> credentials;

    public CredentialManager()
    {
        credentials = new ArrayList<>();
    }

    public void loadCredentials()
    {
        JsonReader jsonReader = null;
        try {
            jsonReader = new JsonReader(new FileReader(CREDENTIALS_FILENAME));
            credentials = new Gson().fromJson(jsonReader, CREDENTIAL_TYPE);
        } catch (FileNotFoundException e) {
            System.out.println("Credentials file not found!");
        }
    }

    public void saveCredentials()
    {
        String json = new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(credentials);
        try(FileWriter fw = new FileWriter(CREDENTIALS_FILENAME))
        {
            fw.write(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addCredential(Credential credential)
    {
        credentials.add(credential);
        saveCredentials();
    }

    public void removeCredential(Credential credential) {
        credentials.remove(credential);
        saveCredentials();
    }
}
