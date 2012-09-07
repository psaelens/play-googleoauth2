package controllers;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import controllers.Secure.Security;
import libs.GoogleOAuth2;
import models.Userinfo;
import play.Play;
import play.exceptions.UnexpectedException;
import play.libs.OAuth2;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.Router.ActionDefinition;
import play.mvc.results.BadRequest;
import play.mvc.results.Result;
import play.utils.Java;

/**
 * 
 * @author Pierre Saelens
 * @see https://developers.google.com/accounts/docs/OAuth2Login
 * @see https://developers.google.com/accounts/docs/OAuth2WebServer
 * 
 */
public class OAuth2Secure extends Controller {

	private static final class OAuth2Error extends Result {
		private final play.libs.OAuth2.Response response;

		private OAuth2Error(play.libs.OAuth2.Response response) {
			this.response = response;
		}

		@Override
		public void apply(Request req, Response resp) {
			resp.status = response.httpResponse.getStatus();
			resp.contentType = response.httpResponse.getContentType();
			try {
				resp.out.write(response.httpResponse.getString().getBytes(
						getEncoding()));
			} catch (Exception e) {
				throw new UnexpectedException(e);
			}
		}
	}

	public static GoogleOAuth2 GOOGLE = new GoogleOAuth2(
			Play.configuration.getProperty("google.oauth2.authorizationURL"),
			Play.configuration.getProperty("google.oauth2.accessTokenURL"),
			Play.configuration.getProperty("google.oauth2.clientid"),
			Play.configuration.getProperty("google.oauth2.secret"), 
			Play.configuration.getProperty("google.oauth2.responseType"),
			Play.configuration.getProperty("google.oauth2.scope"));

	@Before(unless = { "login", "authenticate", "logout" })
	static void checkAccess() throws Throwable {
		// Authent
		if (!session.contains("username")) {
			flash.put("url", "GET".equals(request.method) ? request.url : "/"); // seems a good default
			forbidden("Not logged");
		}
		// Checks
		Check check = getActionAnnotation(Check.class);
		if (check != null) {
			check(check);
		}
		check = getControllerInheritedAnnotation(Check.class);
		if (check != null) {
			check(check);
		}
	}

	private static void check(Check check) throws Throwable {
		for (String profile : check.value()) {
			boolean hasProfile = (Boolean) Security.invoke("check", profile);
			if (!hasProfile) {
				Security.invoke("onCheckFailed", profile);
			}
		}
	}

	// ~~~ Login

	public static void login() throws Throwable {
		// Forming the URL
		GOOGLE.retrieveVerificationCode(authURL());
	}

	/**
	 * Callback method for oauth2
	 * 
	 * @throws Throwable
	 */
	public static void authenticate() throws Throwable {
		// Check tokens
		Boolean allowed = false;
		String accessToken = null;

		if (OAuth2.isCodeResponse()) {
			// Validating the Token
			final OAuth2.Response response = GOOGLE
					.retrieveAccessToken(authURL());

			if (response.httpResponse.success()) {
				// handle response
				JsonObject json = response.httpResponse.getJson()
						.getAsJsonObject();
				accessToken = json.get("access_token").getAsString();
			} else {
				throw new OAuth2Error(response);
			}

			// Obtaining User Profile Information
			Userinfo userinfo = GOOGLE
					.retrieveUserInformation(accessToken);

			allowed = (Boolean) Security.invoke("authenticate", accessToken, userinfo);

			if (!allowed) {
				flash.keep("url");
				flash.error("secure.error");
				redirectToOriginalURL();
			}

			// Mark user as connected
			session.put("username", userinfo.email);
			
			Security.invoke("onAuthenticated");

			// Redirect to the original URL (or /)
			redirectToOriginalURL();
		}
	}
	
    public static void logout() throws Throwable {
        Security.invoke("onDisconnect");
        session.clear();
        Security.invoke("onDisconnected");
        flash.success("secure.logout");

		// Redirect to the original URL (or /)
		redirectToOriginalURL();
    }

	// ~~~ Utils

	static String authURL() {
		return getFullUrl("OAuth2Secure.authenticate");
	}

    public static String getFullUrl(String action, Map<String, Object> args) {
        ActionDefinition actionDefinition = play.mvc.Router.reverse(action, args);
        String base = Play.configuration.getProperty("application.baseUrl", "application.baseUrl");
        if (actionDefinition.method.equals("WS")) {
            return base.replaceFirst("https?", "ws") + actionDefinition;
        }
        return base + actionDefinition;
    }

    public static String getFullUrl(String action) {
        // Note the map is not <code>Collections.EMPTY_MAP</code> because it will be copied and changed.
        return getFullUrl(action, new HashMap<String, Object>(16));
    }
    
	static void redirectToOriginalURL() throws Throwable {
		String url = flash.get("url");
		if (url == null) {
			url = "/";
		}
		redirect(url);
	}

	public static class Security extends Controller {

		/**
		 * This method is called during the authentication process. This is
		 * where you check if the user is allowed to log in into the system.
		 * This is the actual authentication process against a third party
		 * system (most of the time a DB).
		 * 
		 * @param accessToken
		 *            retrieve from previous google oauth2 request
		 * @param userinfo
		 *            return by Google API
		 * @return true if the authentication process succeeded
		 */
		static boolean authenticate(String accessToken, Userinfo userinfo) {
			return true;
		}

		/**
		 * This method checks that a profile is allowed to view this
		 * page/method. This method is called prior to the method's controller
		 * annotated with the @Check method.
		 * 
		 * @param profile
		 * @return true if you are allowed to execute this controller method.
		 */
		static boolean check(String profile) {
			return true;
		}

		/**
		 * This method returns the current connected username
		 * 
		 * @return
		 */
		static String connected() {
			return session.get("username");
		}

		/**
		 * Indicate if a user is currently connected
		 * 
		 * @return true if the user is connected
		 */
		static boolean isConnected() {
			return session.contains("username");
		}

		/**
		 * This method is called after a successful authentication. You need to
		 * override this method if you with to perform specific actions (eg.
		 * Record the time the user signed in)
		 */
		static void onAuthenticated() {
		}

		/**
		 * This method is called before a user tries to sign off. You need to
		 * override this method if you wish to perform specific actions (eg.
		 * Record the name of the user who signed off)
		 */
		static void onDisconnect() {
		}

		/**
		 * This method is called after a successful sign off. You need to
		 * override this method if you wish to perform specific actions (eg.
		 * Record the time the user signed off)
		 */
		static void onDisconnected() {
		}

		/**
		 * This method is called if a check does not succeed. By default it
		 * shows the not allowed page (the controller forbidden method).
		 * 
		 * @param profile
		 */
		static void onCheckFailed(String profile) {
			forbidden();
		}

		private static Object invoke(String m, Object... args) throws Throwable {
			Class security = null;
			List<Class> classes = Play.classloader
					.getAssignableClasses(Security.class);
			if (classes.size() == 0) {
				security = Security.class;
			} else {
				security = classes.get(0);
			}
			try {
				return Java.invokeStaticOrParent(security, m, args);
			} catch (InvocationTargetException e) {
				throw e.getTargetException();
			}
		}

	}
}
