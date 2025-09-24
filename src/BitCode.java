public record BitCode(long value, int length) {
    @Override
    public String toString() {
        return String.format("%" + Math.max(1, length) + "s", Long.toBinaryString(value))
                .replace(' ', '0');
    }

    @Override
    public int hashCode() {
        return 31 * Long.hashCode(value) + Integer.hashCode(length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof BitCode(long v, int l)) {
            return value == v && length == l;
        }
        return false;
    }
}
