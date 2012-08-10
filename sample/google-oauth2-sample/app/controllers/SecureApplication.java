package controllers;

import models.User;
import models.Userinfo;
import play.libs.WS.HttpResponse;
import play.mvc.Controller;
import play.mvc.With;

@With(OAuth2Secure.class)
public class SecureApplication extends Controller {


    public static void index() {
    	User user = User.get(Security.connected());
    	Userinfo userinfo = OAuth2Secure.GOOGLE.retrieveUserInformation(user.access_token);
        render(userinfo);
    }

}
