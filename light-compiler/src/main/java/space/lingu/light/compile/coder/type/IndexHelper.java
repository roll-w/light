package space.lingu.light.compile.coder.type;

/**
 * @author RollW
 */
final class IndexHelper {
    public static int parseIndex(String indexName) {
        if (indexName == null) {
            return -1;
        }
        try {
            return Integer.parseInt(indexName);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static boolean isNeedCheckIndex(String indexName) {
        return parseIndex(indexName) < 0;
    }

    private IndexHelper() {
    }
}
