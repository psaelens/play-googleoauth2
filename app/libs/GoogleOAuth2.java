package libs;

import java.util.HashMap;
import java.util.Map;

import models.Userinfo;

import com.google.gson.Gson;
import com.sun.xml.internal.bind.v2.model.core.Ref;

import play.libs.OAuth2;
import play.libs.WS;
import play.libs.OAuth2.Response;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import play.mvc.Scope.Params;
import play.mvc.results.Redirect;

/**
 * Extended version of provided OAuth2 lib.
 * 
 * <p>
 * Following parameters added :
 * </p>
 * <ul>
 * <li>response_type.</li>
 * <li>scope.</li>
 * </ul>
 * 
 * @author Pierre Saelens
 * 
 */
public class GoogleOAuth2 extends OAuth2 {

	/**
	 * code or token.<br>
	 * Determines if the Google Authorization Server returns an authorization
	 * code, or an opaque access token.
	 */
	public String responseType;

	/**
	 * space delimited set of permissions the application requests<br>
	 * Indicates the Google API access your application is requesting. The
	 * values passed in this parameter inform the consent page shown to the
	 * user. There is an inverse relationship between the number of permissions
	 * requested and the likelihood of obtaining user consent.
	 */
	public String scope;

	public GoogleOAuth2(String authorizationURL, String accessTokenURL,
			String clientid, String secret, String responseType, String scope) {
		super(authorizationURL, accessTokenURL, clientid, secret);
		this.responseType = responseType;
		this.scope = scope;
	}

	public void retrieveVerificationCode(String callbackURL) {
		throw new Redirect(getAuthorizationURL() + "?client_id="
				+ getClientid() + "&response_type=" + responseType + "&scope="
				+ scope + "&redirect_uri=" + callbackURL);
	}

	public Response retrieveAccessToken(String callbackURL) {
		String accessCode = Params.current().get("code");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("client_id", getClientid());
		params.put("client_secret", getSecret());
		params.put("redirect_uri", callbackURL);
		params.put("code", accessCode);
		params.put("grant_type", "authorization_code");
		WSRequest req = WS.url(getAccessTokenURL()).params(params);
		HttpResponse response = req.post();
		return new Response(response);
	}
	
	/**
	 * TODO handle error
	 * @param access_token
	 * @return
	 */
	public Userinfo retrieveUserInformation(String access_token) {
		WSRequest req = WS.url("https://www.googleapis.com/oauth2/v1/userinfo?access_token=%s", access_token);
		HttpResponse response = req.get();
		return new Gson().fromJson(response.getJson(), Userinfo.class);
	}

	public String getClientid() {
		return clientid;
	}
	
	public String getSecret() {
		return secret;
	}

	public String getAuthorizationURL() {
		return authorizationURL;
	}
	public String getAccessTokenURL() {
		return accessTokenURL;
	}
}
