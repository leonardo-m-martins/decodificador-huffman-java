import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FrequencyTableMap extends HashMap<Integer, Integer> implements Map<Integer, Integer> {

    public FrequencyTableMap() {
        super();
    }

    public FrequencyTableMap(String text) {
        this();

        text.codePoints().forEach(codePoint -> merge(codePoint, 1, Integer::sum));
    }
}
