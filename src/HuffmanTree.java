import java.util.*;

public class HuffmanTree {
    private final Map<Character, String> binaryCodeTable;

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

    public HuffmanTree(Map<Character, String> binaryCodeTable) {
        this.binaryCodeTable = binaryCodeTable;
    }

    public void printBinaryCodeTable() {
        binaryCodeTable.forEach(((character, bCode) -> System.out.println("CHARACTER: " + character + " BINARY: " + bCode)));
    }

    private byte[] codify(String text) {
        final byte[] codifiedText;
        String binaryString = "";

        for (char c : text.toCharArray()) {
            binaryString = binaryString.concat(binaryCodeTable.get(c));
        }


        List<String> chunks = divideStringIntoChunks(binaryString);
        codifiedText = new byte[chunks.size() + 1];
        String lastByte = chunks.getLast();
        short bitsInLastByte = (short) lastByte.length();

        lastByte = String.format("%-8s", lastByte).replace(" ", "0");
        chunks.removeLast();
        chunks.add(lastByte);

        codifiedText[0] = (byte) bitsInLastByte;

        short i = 1;
        for (String chunk : chunks) {
            int temp = Integer.parseInt(chunk, 2);
            codifiedText[i] = (byte) temp;
            i++;
        }

        return codifiedText;
    }

    private List<String> divideStringIntoChunks(String s) {
        List<String> chunks = new ArrayList<>();

        int index = 0;
        while (index < s.length()) {
            int end = Math.min(index + 8, s.length());
            chunks.add(s.substring(index, end));
            index = end;
        }

        return chunks;
    }

    private short getHeaderSize() {
        short size = 1;

        for (Map.Entry<Character, String> entry : binaryCodeTable.entrySet()) {
            size += 2; // símbolo e tamanho do código
            size += (short) Math.ceil(entry.getValue().length() / 8.0); // código
        }

        return size;
    }

    private byte[] codifyBinaryTable() {
        short headerSize = getHeaderSize();
        final byte[] codifiedBinarytable = new byte[headerSize];
        codifiedBinarytable[0] = (byte) binaryCodeTable.size();

        short i = 1;
        for (Map.Entry<Character, String> entry : binaryCodeTable.entrySet()) {
            char c = entry.getKey();
            String s = entry.getValue();
            codifiedBinarytable[i] = (byte) c;
            i += 1;
            codifiedBinarytable[i] = (byte) s.length();
            i += 1;

            List<String> chunks = divideStringIntoChunks(s);
            for (String chunk : chunks) {
                int temp = Integer.parseInt(chunk, 2);
                codifiedBinarytable[i] = (byte) temp;
                i += 1;
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

    public static String decodify(byte[] codifiedBytes) {
        final short elementsInHeader = codifiedBytes[0];
        Map<String, Character> invertedBinaryCodeTable = new HashMap<>(elementsInHeader);

        int index = 1;
        for (int i = 0; i < elementsInHeader; i++) {
            char c = (char) codifiedBytes[index];
            index += 1;

            short bitsLen = codifiedBytes[index];
            short realBits = (short) (8 - bitsLen % 8);
            short bytesLen = (short) Math.ceil(bitsLen / 8.0);
            index += 1;

            List<String> chunks = new ArrayList<>();
            for (int j = 0; j < bytesLen - 1; j++) {
                String s = String.format("%8s", Integer.toBinaryString(codifiedBytes[index] & 0xFF)).replace(' ', '0');
                chunks.add(s);
                index += 1;
            }

            {
                String s = String.format("%8s", Integer.toBinaryString(codifiedBytes[index] & 0xFF)).replace(' ', '0').substring(realBits);
                chunks.add(s);
                index += 1;
            }

            String s = "";
            for (String chunk : chunks) {
                s = s.concat(chunk);
            }

            invertedBinaryCodeTable.put(s, c);
        }

        short bitsInLastByte = codifiedBytes[index];
        short bitsToRemove = (short) (8 - bitsInLastByte);
        index += 1;

        String binaryText = "";
        for (int i = index; i < codifiedBytes.length; i++) {
            byte b = codifiedBytes[i];
            String s = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            binaryText = binaryText.concat(s);
        }
        binaryText = binaryText.substring(0, binaryText.length() - bitsToRemove);


        int sIndex = 0;
        String substringBinaryText = "";
        StringBuilder text = new StringBuilder();
        while (sIndex < binaryText.length()) {
            if (invertedBinaryCodeTable.containsKey(substringBinaryText)) {
                char c = invertedBinaryCodeTable.get(substringBinaryText);
                text.append(c);
                substringBinaryText = "";
            }
            else {
                char s = binaryText.charAt(sIndex);
                substringBinaryText = substringBinaryText + s;
                sIndex++;
            }
        }

        if (invertedBinaryCodeTable.containsKey(substringBinaryText)) {
            char c = invertedBinaryCodeTable.get(substringBinaryText);
            text.append(c);
        }

        return text.toString();
    }

}
