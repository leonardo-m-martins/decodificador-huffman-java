import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        byte[] bytes = codify("text.txt");
        String text = HuffmanTree.decodify(bytes);

        System.out.println(text);
    }

    public static byte[] codify(String fileName){
        Path path = Paths.get(fileName);
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
        return huffmanTree.toCodifiedBytes(text);
    }
}