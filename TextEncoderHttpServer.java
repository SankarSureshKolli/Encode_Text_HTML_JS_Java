import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList; 
import java.util.Base64;


public class TextEncoderHttpServer {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/encode", new EncodeHandler());
        server.setExecutor(null); // Use the default executor

        server.start();
        System.out.println("Server is listening on port " + port);
    }
}

class EncodeHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            // Parse the query parameters
            Map<String, List<String>> queryParams = queryToMap(exchange.getRequestURI().getQuery());
            if (queryParams.containsKey("text")) {
                String textToEncode = queryParams.get("text").get(0);
                System.out.println(textToEncode);
                // Perform encoding (e.g., URL encoding)
                String encodedText = java.net.URLEncoder.encode(textToEncode, StandardCharsets.UTF_8.toString());
                byte[] encodedBytes=Base64.getEncoder().encode (encodedText.getBytes (StandardCharsets.UTF_8));
                encodedText=new String (encodedBytes, StandardCharsets.UTF_8);
                System.out.println(encodedText);
                //encodedText="hello";
                // Add the Access-Control-Allow-Origin header
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, encodedText.length());
                OutputStream os = exchange.getResponseBody();
                os.write(encodedText.getBytes());
                os.close();
            } else {
                // Handle missing 'text' parameter
                exchange.sendResponseHeaders(400, 0); // Bad Request
                exchange.getResponseBody().close();
            }
        } else {
            // Handle non-GET requests
            exchange.sendResponseHeaders(405, 0); // Method Not Allowed
            exchange.getResponseBody().close();
        }
    }

    private Map<String, List<String>> queryToMap(String query) {
        Map<String, List<String>> result = new HashMap<>();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = java.net.URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                    result.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
                }
            }
        }
        return result;
    }
}