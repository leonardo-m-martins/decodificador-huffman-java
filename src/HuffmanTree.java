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

    public void printBinaryCodeTable() {
        binaryCodeTable.forEach(((character, bCode) -> System.out.println("CHARACTER: " + character + " BITS: " + bCode.length() + " BYTES: " + bCode)));
    }

    private byte[] codify(String text) {
        final BitArray bitArray = new BitArray();

        char[] chars = text.toCharArray();
        for (char c : chars) {
            BitCode bitCode = binaryCodeTable.get(c);
            bitArray.add(bitCode);
        }

        return bitArray.toByteArray();
    }

    private short getHeaderSize() {
        short size = 1;

        for (Map.Entry<Character, BitCode> entry : binaryCodeTable.entrySet()) {
            size += 2; // símbolo e tamanho do código
            size += (short) Math.ceil(entry.getValue().length() / 8.0);
        }

        return size;
    }

    private byte[] codifyBinaryTable() {
        short headerSize = getHeaderSize();
        final byte[] codifiedBinarytable = new byte[headerSize];
        codifiedBinarytable[0] = (byte) binaryCodeTable.size();

        int i = 1;
        for (Map.Entry<Character, BitCode> entry : binaryCodeTable.entrySet()) {
            char c = entry.getKey();
            BitCode bitCode = entry.getValue();

            codifiedBinarytable[i] = (byte) c;
            i += 1;

            int length = bitCode.length();
            codifiedBinarytable[i] = (byte) length;
            i += 1;

            int value = bitCode.value();
            int byteNumber = (int) Math.ceil(bitCode.length() / 8.0);
            for (int j = 0; j < byteNumber; j++) {
                int shift = Math.max(0, length - 8 * (j + 1));
                codifiedBinarytable[i] = (byte) (value >>> shift);
                i++;

                int mask = (1 << shift) - 1;
                value &= mask;
            }
        }

        return codifiedBinarytable;
    }

    public byte[] toCodifiedBytes(String text) {
        final byte[] codifiedBinaryTable = codifyBinaryTable(), codifiedText = codify(text);
        int len = codifiedText.length + codifiedBinaryTable.length;
        final byte[] codifiedBytes = new byte[len];

        System.arraycopy(codifiedBinaryTable, 0, codifiedBytes, 0, codifiedBinaryTable.length);

        System.arraycopy(codifiedText, 0, codifiedBytes, codifiedBinaryTable.length, codifiedText.length);

        return codifiedBytes;
    }



}
