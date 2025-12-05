package com.example.stage3;

/**
 * TextPreprocessor
 * Cleans HTML content and converts it into plain searchable text.
 */
public class TextPreprocessor {

    /**
     * Process raw HTML content into cleaned searchable text.
     */
    public static String cleanHTML(String html) {
        if (html == null) return "";

        html = html.replaceAll("(?is)<script.*?>.*?</script>", " ");
        html = html.replaceAll("(?is)<style.*?>.*?</style>", " ");
        html = html.replaceAll("<[^>]+>", " ");
        html = html.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5 ]", " ");
        html = html.replaceAll("\\s{2,}", " ");

        return html.trim();
    }
}
