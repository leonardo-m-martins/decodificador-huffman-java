public record BitCode(int value, int length) {
    @Override
    public String toString() {
        return String.format("%" + Math.max(1, length) + "s", Integer.toBinaryString(value))
                .replace(' ', '0');
    }

    @Override
    public int hashCode() {
        return 31 * Integer.hashCode(value) + Integer.hashCode(length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof BitCode(int v, int l)) {
            return value == v && length == l;
        }
        return false;
    }
}
