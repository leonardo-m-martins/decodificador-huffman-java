import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HuffmanTree {
    public record MapAndIndex(Map<BitCode, Integer> binaryTable, int index) {}

    private final Map<Integer, BitCode> codeTable;
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

        text.codePoints().forEach(codePoint -> {
            BitCode bitCode = codeTable.get(codePoint);
            bitArray.add(bitCode);
        });

        return bitArray.toByteArray();
    }

    private byte[] codifyBinaryTable() {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int elementsInHeader = huffmanEntries.size();
        byte[] bytesFromInt = ByteBuffer.allocate(4).putInt(elementsInHeader).array();
        for (byte b : bytesFromInt) {
            baos.write(b);
        }

        for (HuffmanEntry entry : huffmanEntries) {
            int codePoint = entry.codePoint();
            int length = entry.bits();

            byte[] bytesUtf8 = Character.toString(codePoint).getBytes(StandardCharsets.UTF_8);
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

    public static Map<Integer, BitCode> createCodeTable(PriorityQueue<HuffmanEntry> minHeap) {
        Map<Integer, BitCode> codeMap = new LinkedHashMap<>(minHeap.size());
        long lastValue = -1;
        int lastLength = 0;
        while (!minHeap.isEmpty()) {
            HuffmanEntry huffmanEntry = minHeap.poll();
            int length = huffmanEntry.bits();
            BitCode bitCode = new BitCode(++lastValue << (length - lastLength), length);
            codeMap.put(huffmanEntry.codePoint(), bitCode);

            lastLength = length;
            lastValue = bitCode.value();
        }

        return codeMap;
    }

    public static Map<BitCode, Integer> createInvertedCodeTable(PriorityQueue<HuffmanEntry> minHeap) {
        Map<BitCode, Integer> codeMap = new LinkedHashMap<>(minHeap.size());
        long lastValue = -1;
        int lastLength = 0;
        while (!minHeap.isEmpty()) {
            HuffmanEntry huffmanEntry = minHeap.poll();
            int length = huffmanEntry.bits();
            BitCode bitCode = new BitCode(++lastValue << (length - lastLength), length);
            codeMap.put(bitCode, huffmanEntry.codePoint());

            lastLength = length;
            lastValue = bitCode.value();
        }

        return codeMap;
    }

    public static String decodify(byte[] codifiedBytes) {
        MapAndIndex mapAndIndex = decodifyHeader(codifiedBytes);
        Map<BitCode, Integer> binaryTable = mapAndIndex.binaryTable();

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
        final int elementsInHeader = getIntFromBytes(codifiedBytes, 0);
        PriorityQueue<HuffmanEntry> minHeap = new PriorityQueue<>();

        int index = 4;
        for (int i = 0; i < elementsInHeader; i++) {
            int numBytes = getNumberOfBytesInChar(codifiedBytes[index]);
            byte[] bytes = new byte[numBytes];
            System.arraycopy(codifiedBytes, index, bytes, 0, numBytes);
            String s = new String(bytes, StandardCharsets.UTF_8);
            int codePoint = s.codePointAt(0);
            index += numBytes;

            int length = codifiedBytes[index];
            index++;

            HuffmanEntry huffmanEntry = new HuffmanEntry(codePoint, length);
            minHeap.add(huffmanEntry);
        }

        Map<BitCode, Integer> invertedBinaryCodeTable = HuffmanTree.createInvertedCodeTable(minHeap);

        return new MapAndIndex(invertedBinaryCodeTable, index);
    }

    private static int getIntFromBytes(byte[] bytes, int offset) {
        return ByteBuffer.wrap(bytes, offset, 4)
                .order(ByteOrder.BIG_ENDIAN) // ou LITTLE_ENDIAN
                .getInt();
    }

}
