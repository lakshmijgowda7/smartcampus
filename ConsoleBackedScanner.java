import java.io.Console;
import java.util.NoSuchElementException;

class ConsoleBackedScanner {
    private final Console console;

    ConsoleBackedScanner(Console console) {
        if (console == null) {
            throw new IllegalArgumentException("An interactive console is required.");
        }
        this.console = console;
    }

    public String nextLine() {
        String line = console.readLine();
        if (line == null) {
            throw new NoSuchElementException("Console input is closed.");
        }
        return line;
    }
}
