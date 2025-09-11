import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FrequencyTableMap extends HashMap<Character, Integer> implements Map<Character, Integer> {

    public FrequencyTableMap() {
        super();
    }

    public FrequencyTableMap(String text) {
        this();

        for (Character c : text.toCharArray()) {
            merge(c, 1, Integer::sum);
        }
    }

    public FrequencyTableMap(Path path) {
        this();

        try {
            String text = Files.readString(path);

            for (Character c : text.toCharArray()) {
                merge(c, 1, Integer::sum);
            }

        } catch (IOException ex) {
            System.err.println("Erro ao ler o arquivo" + ex.getMessage());
        }
    }
}
