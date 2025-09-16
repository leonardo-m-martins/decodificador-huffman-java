import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        Path path = Paths.get("text.txt");
        String text = null;
        try {
            text = Files.readString(path);

        } catch (IOException ex) {
            System.err.println("Erro ao ler o arquivo" + ex.getMessage());
        }
        assert text != null;
        FrequencyTableMap frequencyTableMap = new FrequencyTableMap(text);
        HuffmanTree huffmanTree = new HuffmanTree(frequencyTableMap);
        huffmanTree.printBinaryCodeTable();
        byte[] bytes = huffmanTree.toCodifiedBytes(text);
        String decodified = Decoder.decodify(bytes);
        System.out.println(decodified.equals(text));
    }
}