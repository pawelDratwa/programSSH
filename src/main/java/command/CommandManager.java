package command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import lombok.Getter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CommandManager {
    private static final String COMMANDS_FILENAME = "commands.json";
    private static final Type COMMAND_TYPE = new TypeToken<List<Command>>() {}.getType();

    @Getter
    private List<Command> commands;

    public CommandManager()
    {
        commands = new ArrayList<>();
    }

    public void loadCommands()
    {
        JsonReader jsonReader = null;
        try {
            jsonReader = new JsonReader(new FileReader(COMMANDS_FILENAME));
            commands = new Gson().fromJson(jsonReader, COMMAND_TYPE);
        } catch (FileNotFoundException e) {
            System.out.println("Commands file not found!");
        }
    }

    public void saveCommands()
    {
        String json = new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(commands);
        try(FileWriter fw = new FileWriter(COMMANDS_FILENAME))
        {
            fw.write(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addCommand(Command command)
    {
        commands.add(command);
        saveCommands();
    }

    public void removeCommand(Command command)
    {
        commands.remove(command);
        saveCommands();
    }
}
