package models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

import controllers.Security;

@Entity
public class User extends TemporalModel {
	@Column(unique = true, name = "name", nullable = false)
    private String name;
	
	public Role role;

	public int loginCount;

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
			Security.md5((new Date()).toString());
		}
		passwordHash = Security.md5(salt + string);
	}
    
}
