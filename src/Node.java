import java.util.*;

public class Node implements Comparable<Node> {
    private final Integer codePoint;
    private final int frequency;
    private final Node dir;
    private final Node esq;

    public Node(Integer value, int frequency) {
        this.codePoint = value;
        this.frequency = frequency;
        dir = esq = null;
    }

    public Node(Integer value, int frequency, Node child1, Node child2) {
        this.codePoint = value;
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

    public List<HuffmanEntry> getLeafNodes(int depth) {
        if (codePoint != null) {
            if (depth == 0) depth++;
            return List.of(new HuffmanEntry(codePoint, depth));
        }

        List<HuffmanEntry> leafNodes = new ArrayList<>();

        if (esq != null) {
            leafNodes.addAll(esq.getLeafNodes(depth + 1));
        }

        if (dir != null) {
            leafNodes.addAll(dir.getLeafNodes(depth + 1));
        }

        return leafNodes;
    }

    public PriorityQueue<HuffmanEntry> getHuffmanEntries() {
        PriorityQueue<HuffmanEntry> minHeap = new PriorityQueue<>();
        minHeap.addAll(getLeafNodes(0));
        return minHeap;
    }
}
