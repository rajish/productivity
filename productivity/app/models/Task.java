package models;

import java.util.Date;

import javax.persistence.Entity;

import play.data.validation.Required;

@Entity(name="tasks")
public class Task extends TemporalModel {
	@Required
	public String name;
	public String description;	
    public Project project;
    public Date plannedStart;
    public Date plannedEnd;
    public boolean isActive;
}
