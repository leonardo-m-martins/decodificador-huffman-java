import java.util.HashMap;
import java.util.Map;

public class Decoder {

    public record MapAndIndex(Map<BitCode, Character> binaryTable, int index) {}

    public static String decodify(byte[] codifiedBytes) {
        MapAndIndex mapAndIndex = decodifyHeader(codifiedBytes);
        Map<BitCode, Character> binaryTable = mapAndIndex.binaryTable();

        int index = mapAndIndex.index();
        BitArray bitArray = BitArray.fromByteArray(codifiedBytes, index);
        return bitArray.decode(binaryTable);
    }

    private static MapAndIndex decodifyHeader(byte[] codifiedBytes) {
        final short elementsInHeader = codifiedBytes[0];
        Map<BitCode, Character> invertedBinaryCodeTable = new HashMap<>(elementsInHeader);

        int index = 1;
        for (int i = 0; i < elementsInHeader; i++) {
            char c = (char) codifiedBytes[index];
            index++;

            short length = codifiedBytes[index]; // total de bits
            short bitsInLastByte = (short) (length % 8);
            short bytesLen = (short) Math.ceil(length / 8.0);
            index++;

            int value = 0;
            for (int j = 0; j < bytesLen; j++) {
                if (j == bytesLen - 1) {
                    value = (value << bitsInLastByte) | Byte.toUnsignedInt(codifiedBytes[index]);
                }
                else {
                    value <<= 8;
                    value |= Byte.toUnsignedInt(codifiedBytes[index]);
                }
                index++;
            }

            BitCode bitCode = new BitCode(value, length);
            invertedBinaryCodeTable.put(bitCode, c);
        }

        return new MapAndIndex(invertedBinaryCodeTable, index);
    }
}
