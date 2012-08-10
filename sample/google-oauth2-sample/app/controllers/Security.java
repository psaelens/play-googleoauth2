package controllers;

import models.User;
import models.Userinfo;

import com.google.gson.JsonObject;

public class Security extends controllers.OAuth2Secure.Security {

    static boolean authenticate(String accessToken, Userinfo userinfo) {
		String email = userinfo.email;
		if (userinfo.verified_email) {
			// remember accessToken for next Google API calls
			User user = User.getOrCreate(email);
			user.access_token = accessToken;
			user.save();
			return true;
		}
        return false;
    }
}
