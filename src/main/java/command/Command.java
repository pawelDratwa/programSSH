package command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
public class Command {
    private String text;

    @Override
    public String toString() {
        return text;
    }
}
