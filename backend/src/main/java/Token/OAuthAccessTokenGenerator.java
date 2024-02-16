package Token;


import java.io.IOException;


import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.firebase.auth.FirebaseAuthException;

@SuppressWarnings("deprecation")
public class OAuthAccessTokenGenerator {

    public static String exchangeCodeForAccessToken(String authorizationCode) {
        try {
        	
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                "https://oauth2.googleapis.com/token",
                "549283482423-gkidn4sjbo7p8h606asqtfgeidok4u0r.apps.googleusercontent.com",
                "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",
                authorizationCode,
                "https://composite-shard-372515.firebaseapp.com/__/auth/handler" // Debe coincidir con el `redirect_uri` usado para obtener el code
            ).execute();

            return tokenResponse.getAccessToken();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws FirebaseAuthException {
        String authorizationCode = "4/0AeaYSHCGmx3dcRV1vSrmLtE38T5CPYKIlnzqBsuhPpoY0Kq3QlyUXHha9FiFa9EvIRI6rg";
        String accessToken = exchangeCodeForAccessToken(authorizationCode);
        System.out.println("Access Token: " + accessToken);
    }
}
