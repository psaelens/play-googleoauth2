package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class User extends Model {

    public String email;
    public String access_token;

    public User(String email) {
        this.email = email;
    }

    public static User get(String email) {
        return find("email", email).first();
    }
    
    public static User getOrCreate(String email) {
    	User first = find("email", email).first();
    	if (first == null)
    		return new User(email).save();
		return first;
    }
}
