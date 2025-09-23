import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class Decoder {

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
