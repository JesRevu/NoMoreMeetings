package Token;

import java.awt.Desktop;
import java.net.URI;

public class OAuthAuthorization {

    public static void initiateOAuthFlow() throws Exception {
        String clientId = "549283482423-gkidn4sjbo7p8h606asqtfgeidok4u0r.apps.googleusercontent.com";
        String redirectUri = "https://composite-shard-372515.firebaseapp.com/__/auth/handler";
        String scope = "https://www.googleapis.com/auth/calendar"; // Ajusta el alcance según tus necesidades
        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth?response_type=code"
                       + "&client_id=" + clientId
                       + "&redirect_uri=" + redirectUri
                       + "&scope=" + scope
                       + "&access_type=offline"
                       + "&prompt=consent";

        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(new URI(authUrl));
        } else {
            throw new UnsupportedOperationException("Desktop no soportado. No se puede abrir el navegador.");
        }
    }
}

