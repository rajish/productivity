package models;

import java.util.Arrays;
import java.util.List;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class AuthMethod extends Model {
    public static final String LOCAL = "local";
    public static final String LDAP  = "LDAP";

    public String name;

    public AuthMethod(String name) {
        this.name = name;
    }

    public static AuthMethod getByName(String name) {
        return find("byName", name).first();
    }
}