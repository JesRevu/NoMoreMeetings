package Token;


public class OAuthCallbackServer {
	
	public static void main(String[] args) {
	    try {
	        OAuthAuthorization.initiateOAuthFlow();
	        // Aquí el usuario autorizará tu aplicación y obtendrás el código
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

}

