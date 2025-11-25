/**
 * TextPreprocessor
 * ----------------------------------------------------
 * Responsible for cleaning HTML content and converting
 * it into plain searchable text.
 * 
 * Processing steps include:
 *   1. Removing <script> and <style> blocks
 *   2. Removing ALL HTML tags
 *   3. Removing special characters
 *   4. Returning cleaned text
 */
public class TextPreprocessor {

    /**
     * Process raw HTML content into cleaned searchable text.
     *
     * @param html Raw HTML string
     * @return Cleaned plain text
     */
    public static String cleanHTML(String html) {

        if (html == null) return "";

        // 1. Remove <script>...</script>
        html = html.replaceAll("(?is)<script.*?>.*?</script>", " ");

        // 2. Remove <style>...</style>
        html = html.replaceAll("(?is)<style.*?>.*?</style>", " ");

        // 3. Remove all HTML tags <...>
        html = html.replaceAll("<[^>]+>", " ");

        // 4. Remove non-alphanumeric symbols except spaces
        html = html.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5 ]", " ");

        // 5. Normalize multiple spaces
        html = html.replaceAll("\\s{2,}", " ");

        return html.trim();
    }
}
