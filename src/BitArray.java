import java.util.Arrays;
import java.util.Map;

public class BitArray {
    private static final int BITS_PER_WORD = 64;
    private static final int BIT_INDEX_MASK = 63;
    private static final int BYTES_PER_WORD = 8;
    private long[] words;
    private int sizeBits;

    private transient int bitsInLastWord = 0;
    private transient int currentWord = 0;

    public BitArray() {
        this.words = new long[1];
        this.sizeBits = 0;
    }

    public BitArray(int capacityBits) {
        this.words = new long[(capacityBits + BIT_INDEX_MASK) / BITS_PER_WORD];
        this.sizeBits = 0;
    }

    @Override
    public String toString() {
        StringBuilder binaryValue = new StringBuilder();
        for (int i = 0; i <= currentWord; i++) {
            int bits = (i == currentWord) ? bitsInLastWord : BITS_PER_WORD;
            binaryValue.append(String.format("%" + bits + "s", Long.toBinaryString(words[i])).replace(" ", "0"));
        }
        return binaryValue.toString();
    }

    public int getSizeBits() {
        return sizeBits;
    }

    public void add(BitCode bitCode) {
        long value = bitCode.value();
        int length = bitCode.length();
        add(value, length);
    }

    public void add(byte b) {
        add(b, 8);
    }

    public void add(byte b, int length) {
        int value = Byte.toUnsignedInt(b);
        add(value, length);
    }

    public void add(long value, int length) {
        ensureCapacity(sizeBits + length);

        if (length <= BITS_PER_WORD - bitsInLastWord) {
            words[currentWord] = (words[currentWord] << length) | value;
            bitsInLastWord += length;
        }

        else {
            int bitsLeftInWord = BITS_PER_WORD - bitsInLastWord;
            int bitsLeftInValue = length - bitsLeftInWord;
            words[currentWord] = words[currentWord] << bitsLeftInWord | (value >>> bitsLeftInValue);
            currentWord++;

            int mask = (1 << bitsLeftInValue) - 1;
            words[currentWord] = words[currentWord] << bitsLeftInValue | (value & mask);
            bitsInLastWord = bitsLeftInValue;
        }

        sizeBits += length;
    }

    public byte[] toByteArray() {

        final int sizeBytes = (int) Math.ceil(sizeBits / 8.0) + 1;
        final byte[] bytes = new byte[sizeBytes];
        final int bitsInLastByte = (bitsInLastWord % 8 == 0) ? 8 : (bitsInLastWord % 8);
        bytes[0] = (byte) bitsInLastByte; // salvar o número de bits no último byte
        int index = 1;

        for (int i = 0; i <= currentWord; i++) {
            int bitsInWord = (i == currentWord) ? bitsInLastWord : BITS_PER_WORD;
            int bytesInWord = (i == currentWord) ? (int) Math.ceil(bitsInLastWord / 8.0) : BYTES_PER_WORD;


            for (int j = 1; j <= bytesInWord; j++) {
                int shift = Math.max(0, bitsInWord - (8 * j));
                int mask = (shift == 0 && i == currentWord) ? (1 << bitsInLastByte) - 1 : 0xFF;
                bytes[index] = (byte) ((words[i] >>> shift) & mask);
                index++;
            }
        }

        return bytes;
    }

    public static BitArray fromByteArray(byte[] bytes) {
        return fromByteArray(bytes, 0);
    }

    public static BitArray fromByteArray(byte[] bytes, int startIndex) {
        final short bitsInLastByte = bytes[startIndex];
        final int bytesLength = bytes.length - startIndex;
        final BitArray bitArray = new BitArray(bytesLength * 8);

        for (int i = startIndex + 1; i < bytes.length - 1; i++) {
            bitArray.add(bytes[i]);
        }
        bitArray.add(bytes[bytes.length - 1], bitsInLastByte);
        return bitArray;
    }

    public String decode(Map<BitCode, Character> binaryCodeTable) {
        StringBuilder text = new StringBuilder();

        BitCode leftover = new BitCode(0, 0);
        for (int i = 0; i <= currentWord; i++) {
            long word = words[i];
            int bitsInWord;
            if (i == currentWord) bitsInWord = bitsInLastWord;
            else bitsInWord = BITS_PER_WORD;
            for (int j = 1; j <= bitsInWord; j++) {
                long value = leftover.value();
                int length = leftover.length();
                int bit = (int) (word >>> (bitsInWord - j) & 1);
                value = (value << 1) | bit;
                length++;
                BitCode toSearch = new BitCode(value, length);

                if (binaryCodeTable.containsKey(toSearch)) {
                    char c = binaryCodeTable.get(toSearch);
                    text.append(c);
                    leftover = new BitCode(0, 0);
                }
                else {
                    leftover = toSearch;
                }
            }
        }

        return text.toString();
    }

    private void ensureCapacity(int minBits) {
        int neededBytes = (minBits + BIT_INDEX_MASK) / BITS_PER_WORD;
        if (neededBytes > words.length) {
            words = Arrays.copyOf(words, Math.max(words.length * 2, neededBytes));
        }
    }
}
