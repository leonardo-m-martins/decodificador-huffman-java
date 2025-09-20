import java.io.ByteArrayOutputStream;
import java.util.*;

public class HuffmanTree {
    private final Map<Character, BitCode> binaryCodeTable;

    public HuffmanTree(FrequencyTableMap frequencyTableMap) {
        PriorityQueue<Node> minHeap = new PriorityQueue<>();
        frequencyTableMap.entrySet().stream()
                .map(e -> new Node(e.getKey(), e.getValue()))
                .forEach(minHeap::add);

        while (!minHeap.isEmpty() && minHeap.size() >= 2) {
            Node node = minHeap.poll(), node1 = minHeap.poll(),
                internalNode = new Node(null, node.getFrequency() + node1.getFrequency(), node1, node);
            minHeap.offer(internalNode);
        }
        Node raiz = minHeap.poll();
        assert raiz != null;
        binaryCodeTable = raiz.createBinaryCodeTable();
    }

    public HuffmanTree(String text) {
        this(new FrequencyTableMap(text));
    }

    public void printBinaryCodeTable() {
        binaryCodeTable.forEach(((character, bCode) -> System.out.println("CHARACTER: " + character + " BITS: " + bCode.length() + " BYTES: " + bCode)));
    }

    private byte[] codifyText(String text) {
        final BitArray bitArray = new BitArray();

        char[] chars = text.toCharArray();
        for (char c : chars) {
            BitCode bitCode = binaryCodeTable.get(c);
            bitArray.add(bitCode);
        }

        return bitArray.toByteArray();
    }

    private byte[] codifyBinaryTable() {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(binaryCodeTable.size());

        for (Map.Entry<Character, BitCode> entry : binaryCodeTable.entrySet()) {
            char c = entry.getKey();
            BitCode bitCode = entry.getValue();
            int length = bitCode.length();
            int value = bitCode.value();

            baos.write((c >> 8) & 0xFF);
            baos.write(c & 0xFF);


            baos.write(length);

            int byteNumber = (int) Math.ceil(bitCode.length() / 8.0);
            for (int j = 0; j < byteNumber; j++) {
                int shift = Math.max(0, length - 8 * (j + 1));
                baos.write(value >>> shift);

                int mask = (1 << shift) - 1;
                value &= mask;
            }
        }

        return baos.toByteArray();
    }

    public byte[] toCodifiedBytes(String text) {
        final byte[] codifiedBinaryTable = codifyBinaryTable(), codifiedText = codifyText(text);
        int len = codifiedText.length + codifiedBinaryTable.length;
        final byte[] codifiedBytes = new byte[len];

        System.arraycopy(codifiedBinaryTable, 0, codifiedBytes, 0, codifiedBinaryTable.length);

        System.arraycopy(codifiedText, 0, codifiedBytes, codifiedBinaryTable.length, codifiedText.length);

        return codifiedBytes;
    }

    public static byte[] codify(String text) {
        HuffmanTree huffmanTree = new HuffmanTree(text);
        return huffmanTree.toCodifiedBytes(text);
    }

}
