package models;

import javax.persistence.Column;
import javax.persistence.Entity;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class Role extends TemporalModel implements models.deadbolt.Role {
    @Column(name = "name", nullable = false)
    @Required
    public String name;

    @Column(name = "privileges", nullable = false)
    public Long privileges;

    public Role(String n, int p) {
        name = n;
        privileges = new Long(p);
    }
    
    @Override
    public String getRoleName() {
        return name;
    }

    public static Role getByName(String name) {
        return find("byName", name).first();
    }
}
