public record HuffmanEntry(char character, int bits) implements Comparable<HuffmanEntry> {
    @Override
    public int compareTo(HuffmanEntry other) {
        int cmp = Integer.compare(bits, other.bits);
        if (cmp != 0) return cmp;
        else return Character.compare(character, other.character);
    }
}
