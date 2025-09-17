import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Número de argumentos inválidos!\nTente \"codify [fileIn] [fileOut]\" ou \"decodify [fileIn] [fileOut]\"");
            return;
        }
        String command = args[0], fileIn = args[1], fileOut = args[2];
        switch (command) {
            case "codify":
                if (fileIn.endsWith(".txt")) codify(fileIn, fileOut);
                else System.out.println("Formato de arquivo não suportado!\nFormatos suportados: .txt");
                break;
            case "decodify":
                if (fileIn.endsWith(".bin") || fileIn.endsWith(".dat")) decodify(fileIn, fileOut);
                else System.out.println("Formato de arquivo não suportado!\nFormatos suportados: .bin, .dat");
                break;
            default:
                System.out.println("Comando inválido!\nComandos válidos: codify, decodify");
        }

    }

    private static void codify(String fileIn, String fileOut) throws IOException {
        Path path = Path.of(fileIn);
        String text = Files.readString(path);
        byte[] bytes = HuffmanTree.codify(text);
        saveFile(fileOut, bytes);;
    }

    private static void saveFile(String fileName, byte[] bytes) throws IOException {
        Path path = Path.of(fileName);
        Files.write(path, bytes);
        System.out.println("Arquivo salvo com sucesso!");
    }

    private static void decodify(String fileIn, String fileOut) throws IOException {
        Path path = Paths.get(fileIn);
        byte[] bytes = Files.readAllBytes(path);
        String text = Decoder.decodify(bytes);
        saveFile(fileOut, text);
    }

    private static void saveFile(String fileName, String text) throws IOException {
        Path path = Path.of(fileName);
        Files.writeString(path, text);
        System.out.println("Arquivo salvo com sucesso!");
    }
}