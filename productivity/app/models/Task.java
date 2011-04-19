package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class Task extends TemporalModel {
	
	public String name;
	public String description;	
    
}
