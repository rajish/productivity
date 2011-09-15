package models;

import javax.persistence.Entity;

@Entity
public class Portlet extends Widget {
    public String state;
    public String name;
    public String tmpl;

    public Portlet(String s, String n, String t) {
        state = s;
        name = n;
        tmpl = t;
    }
}