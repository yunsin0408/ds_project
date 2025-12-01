import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;



/**
 * GoogleQuery
 * ----------------------------------------------------
 * 1. Sends a query to Google Custom Search API
 * 2. Fetches titles and links from the JSON response
 */
public class GoogleQuery {

    private String apiKey;
    private String cx;
    private static final Pattern LINK_TITLE_PATTERN = Pattern.compile("\"title\"\\s*:\\s*\"([^\"]*)\"[^\\}]*\"link\"\\s*:\\s*\"([^\"]*)\"");


    public GoogleQuery() {
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream("GoogleAPI.properties"));
            apiKey = prop.getProperty("google.api_key");
            cx = prop.getProperty("google.cx_id");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Google API properties");
        }

    }

    public HashMap<String, String> query(String query, int num) throws Exception {

        // 1. Construct the URL and Fetch Content        
        String encoded_keyword = java.net.URLEncoder.encode(query, "UTF-8");
        String urlStr = "https://www.googleapis.com/customsearch/v1?key=" + apiKey + "&cx=" + cx + "&num=" + num + "&q=" + encoded_keyword;

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");

        // 2. Read the response
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        conn.disconnect();

        // 3. Parse JSON to extract titles and links
        String json = sb.toString();

        HashMap<String, String> results = new HashMap<>();

        Matcher matcher = LINK_TITLE_PATTERN.matcher(json);
        int count = 0;
        
        while (matcher.find() && count < num) {
            String title = matcher.group(1);
            String link = matcher.group(2);
            
            if (!title.isEmpty() && !link.isEmpty()) {
                results.put(title, link);
                count++;
            }
        }

        return results;
    }

}