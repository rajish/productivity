package models;

import javax.persistence.Column;
import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class Role extends TemporalModel {
    @Column(name = "name", nullable = false)
	public String name;
 
	@Column(name = "privileges", nullable = false)
	public Long privileges;

	public String name() {
		return name;
	}
}
