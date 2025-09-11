import java.util.HashMap;
import java.util.Map;

public class Node implements Comparable<Node> {
    private final Character character;
    private final int frequency;
    private transient String binaryCode;
    private final Node dir;
    private final Node esq;

    public Node(Character value, int frequency) {
        this.character = value;
        this.frequency = frequency;
        dir = esq = null;
    }

    public Node(Character value, int frequency, Node child1, Node child2) {
        this.character = value;
        this.frequency = frequency;
//        this.dir = max(child1, child2);
//        this.esq = min(child1, child2);
        this.dir = child1;
        this.esq = child2;
    }

    private Node max(Node child1, Node child2) {
        if (child1.frequency > child2.frequency) return child1;
        return child2;
    }

    private Node min(Node child1, Node child2) {
        if (child1.frequency < child2.frequency) return child1;
        return child2;
    }

    public Character getCharacter() {
        return character;
    }

    public int getFrequency() {
        return frequency;
    }

    @Override
    public int compareTo(Node other) {
        return Integer.compare(this.frequency, other.frequency);
    }

    public Map<Character, String> setBinaryCode(String binaryCode) {
        Map<Character, String> binaryCodeTable = new HashMap<>();
        this.binaryCode = binaryCode;
        if (character != null) binaryCodeTable.put(character, binaryCode);

        if (esq != null) {
            binaryCodeTable.putAll(esq.setBinaryCode(binaryCode + "0"));
        }

        if (dir != null) {
            binaryCodeTable.putAll(dir.setBinaryCode(binaryCode + "1"));
        }
        return binaryCodeTable;
    }

    public Map<Character, String> createBinaryCodeTable() {
        binaryCode = null;
        Map<Character, String> binaryCodeTable = new HashMap<>();

        if (esq != null) {
            binaryCodeTable.putAll(esq.setBinaryCode(Integer.toBinaryString(0)));
        }

        if (dir != null) {
            binaryCodeTable.putAll(dir.setBinaryCode(Integer.toBinaryString(1)));
        }
        return binaryCodeTable;
    }
}
