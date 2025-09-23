import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HuffmanTree {
    private final Map<Character, BitCode> codeTable;
    private final List<HuffmanEntry> huffmanEntries;

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
        PriorityQueue<HuffmanEntry> huffmanEntryPriorityQueue = raiz.getHuffmanEntries();
        huffmanEntries = huffmanEntryPriorityQueue.stream().toList();
        codeTable = HuffmanTree.createCodeTable(huffmanEntryPriorityQueue);
    }

    public HuffmanTree(String text) {
        this(new FrequencyTableMap(text));
    }

    public void printBinaryCodeTable() {
        codeTable.forEach(((character, bCode) -> System.out.println("CHARACTER: " + character + " BITS: " + bCode.length() + " BYTES: " + bCode)));
    }

    private byte[] codifyText(String text) {
        final BitArray bitArray = new BitArray();

        char[] chars = text.toCharArray();
        for (char c : chars) {
            BitCode bitCode = codeTable.get(c);
            bitArray.add(bitCode);
        }

        return bitArray.toByteArray();
    }

    private byte[] codifyBinaryTable() {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(huffmanEntries.size());

        for (HuffmanEntry entry : huffmanEntries) {
            char c = entry.character();
            int length = entry.bits();

            byte[] bytesUtf8 = String.valueOf(c).getBytes(StandardCharsets.UTF_8);
            baos.write(bytesUtf8, 0, bytesUtf8.length);

            baos.write(length);
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

    public static Map<Character, BitCode> createCodeTable(PriorityQueue<HuffmanEntry> minHeap) {
        Map<Character, BitCode> codeMap = new LinkedHashMap<>(minHeap.size());
        int lastValue = -1;
        int lastLength = 0;
        while (!minHeap.isEmpty()) {
            HuffmanEntry huffmanEntry = minHeap.poll();
            int length = huffmanEntry.bits();
            BitCode bitCode = new BitCode(++lastValue << (length - lastLength), length);
            codeMap.put(huffmanEntry.character(), bitCode);

            lastLength = length;
            lastValue = bitCode.value();
        }

        return codeMap;
    }

    public static Map<BitCode, Character> createInvertedCodeTable(PriorityQueue<HuffmanEntry> minHeap) {
        Map<BitCode, Character> codeMap = new LinkedHashMap<>(minHeap.size());
        int lastValue = -1;
        int lastLength = 0;
        while (!minHeap.isEmpty()) {
            HuffmanEntry huffmanEntry = minHeap.poll();
            int length = huffmanEntry.bits();
            BitCode bitCode = new BitCode(++lastValue << (length - lastLength), length);
            codeMap.put(bitCode, huffmanEntry.character());

            lastLength = length;
            lastValue = bitCode.value();
        }

        return codeMap;
    }

    public record MapAndIndex(Map<BitCode, Character> binaryTable, int index) {}

    public static String decodify(byte[] codifiedBytes) {
        MapAndIndex mapAndIndex = decodifyHeader(codifiedBytes);
        Map<BitCode, Character> binaryTable = mapAndIndex.binaryTable();

        int index = mapAndIndex.index();
        BitArray bitArray = BitArray.fromByteArray(codifiedBytes, index);
        return bitArray.decode(binaryTable);
    }

    private static int getNumberOfBytesInChar(byte first) {
        int value = Byte.toUnsignedInt(first);
        switch (value & 0b1111_0000) {
            case 0b1100_0000, 0b1101_0000 -> {return 2;}
            case 0b1110_0000 -> {return 3;}
            case 0b1111_0000 -> {return 4;}
            default -> {return 1;}
        }
    }

    private static MapAndIndex decodifyHeader(byte[] codifiedBytes) {
        final short elementsInHeader = codifiedBytes[0];
        PriorityQueue<HuffmanEntry> minHeap = new PriorityQueue<>();

        int index = 1;
        for (int i = 0; i < elementsInHeader; i++) {
            int numBytes = getNumberOfBytesInChar(codifiedBytes[index]);
            String s = new String(codifiedBytes, index, index + numBytes, StandardCharsets.UTF_8);
            char c = s.charAt(0);
            index += numBytes;

            int length = codifiedBytes[index];
            index++;

            HuffmanEntry huffmanEntry = new HuffmanEntry(c, length);
            minHeap.add(huffmanEntry);
        }

        Map<BitCode, Character> invertedBinaryCodeTable = HuffmanTree.createInvertedCodeTable(minHeap);

        return new MapAndIndex(invertedBinaryCodeTable, index);
    }

}
