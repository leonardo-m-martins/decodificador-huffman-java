import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class Node implements Comparable<Node> {
    private final Character character;
    private final int frequency;
    private transient BitCode bitCode;
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
        this.dir = max(child1, child2);
        this.esq = min(child1, child2);
    }

    private Node max(Node n1, Node n2) {
        if (n1.frequency >= n2.frequency) return n1;
        else return n2;
    }

    private Node min(Node n1, Node n2) {
        if (n1.frequency < n2.frequency) return n1;
        else return n2;
    }

    public int getFrequency() {
        return frequency;
    }

    @Override
    public int compareTo(Node other) {
        return Integer.compare(this.frequency, other.frequency);
    }

    public Map<Character, BitCode> setBinaryCode(BitCode bitCode) {
        Map<Character, BitCode> binaryCodeTable = new HashMap<>();

        this.bitCode = bitCode;
        if (character != null) binaryCodeTable.put(character, bitCode);

        if (esq != null) {
            BitCode esqBitCode = new BitCode(bitCode.value() << 1, bitCode.length() + 1);
            binaryCodeTable.putAll(esq.setBinaryCode(esqBitCode));
        }

        if (dir != null) {
            BitCode dirBitCode = new BitCode(bitCode.value() << 1 | 1, bitCode.length() + 1);
            binaryCodeTable.putAll(dir.setBinaryCode(dirBitCode));
        }

        return binaryCodeTable;
    }

    public Map<Character, BitCode> createBinaryCodeTable() {
        bitCode = null;
        Map<Character, BitCode> binaryCodeTable = new HashMap<>();


        if (esq != null) {
            BitCode esqBitCode = new BitCode(0, 1);
            binaryCodeTable.putAll(esq.setBinaryCode(esqBitCode));
        }

        if (dir != null) {
            BitCode dirBitCode = new BitCode(1, 1);
            binaryCodeTable.putAll(dir.setBinaryCode(dirBitCode));
        }
        return binaryCodeTable;
    }
}
