package utils.amine;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import io.github.cdimascio.dotenv.Dotenv;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class GoogleCallbackServer {
    static Dotenv dotenv = Dotenv.load();
    private static final String REDIRECT_URI = "http://localhost:8081/callback";
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public static void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);

        server.createContext("/callback", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String response = "Google authentication successful! You can close this tab.";

            if (query != null && query.contains("code=")) {
                String code = query.split("code=")[1].split("&")[0];

                try {
                    // Exchange authorization code for access token
                    TokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                            HTTP_TRANSPORT,
                            JSON_FACTORY,
                            "https://oauth2.googleapis.com/token",
                            dotenv.get("CLIENT_ID"),
                            dotenv.get("CLIENT_SECRET"),
                            code,
                            REDIRECT_URI
                    ).execute();

                    String accessToken = tokenResponse.getAccessToken();
                    System.out.println("Access Token: " + accessToken);

                    // Fetch user info using access token
                    String userInfo = getUserInfo(accessToken);
                    System.out.println("User Info: " + userInfo);

                    response = "User Info: " + userInfo;

                } catch (Exception e) {
                    response = "Error retrieving user info: " + e.getMessage();
                    e.printStackTrace();
                }
            }

            exchange.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        server.setExecutor(Executors.newFixedThreadPool(5));
        server.start();
        System.out.println("Server started at http://localhost:8081/callback");
    }

    private static String getUserInfo(String accessToken) throws IOException {
        HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory();
        HttpRequest request = requestFactory.buildGetRequest(new com.google.api.client.http.GenericUrl(
                "https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + accessToken));
        HttpResponse response = request.execute();
        return response.parseAsString(); // Returns JSON with user info
    }
}
