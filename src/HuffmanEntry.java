public record HuffmanEntry(int codePoint, int bits) implements Comparable<HuffmanEntry> {
    @Override
    public int compareTo(HuffmanEntry other) {
        int cmp = Integer.compare(bits, other.bits);
        if (cmp != 0) return cmp;
        else return Integer.compare(codePoint, other.codePoint);
    }
}
