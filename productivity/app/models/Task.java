package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity(name="tasks")
public class Task extends TemporalModel {
	
	public String name;
	public String description;	
    
}
