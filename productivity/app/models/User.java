package models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import play.data.validation.MinSize;

import controllers.Security;

@Entity
public class User extends TemporalModel {
	@Column(unique = true, name = "name", nullable = false)
    private String name;
	
	@ManyToOne
	public Role role;

	public int loginCount;
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date lastSuccessfulLogin;
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date lastFailedLogin;

	@MinSize(8)
	public String passwordHash;
	
	public String salt;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setPassword(String string) {
		if(salt == null || salt.isEmpty()) {
			salt = Security.md5((new Date()).toString());
		}
		passwordHash = Security.md5(salt + string);
	}
    
	@PrePersist
	@PreUpdate
	public void encryptPassword() {
	    String pass = passwordHash;
	    setPassword(pass);
	}

	public boolean hasPassword(String submittedPassword) {
	    return passwordHash.equals(Security.md5(submittedPassword));
	}

	public static User connect(String login, String password) {
		User user = find("byLogin", login).first();
		System.out.println("User.connect() user = " + user);
		if (user != null && user.hasPassword(password))
			return user;
		else
			return null;
	}

	public String toString() {
		return "User {"
			+ "id: "                   + id + ","
			+ "created: "              + created + ","
			+ "updated: "              + updated + ","
			+ "name: "                 + name + ","
			+ "role: "                 + role + ","
			+ "loginCount: "           + loginCount + ","
			+ "lastSuccessfulLogin: "  + lastSuccessfulLogin + ","
			+ "lastFailedLogin: "      + lastFailedLogin + ","
			+ "passwordHash: "         + passwordHash.substring(0, 10) + ","
			+ "salt: "                 + salt.substring(0, 10)
			+ "}";
	}
}
