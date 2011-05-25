package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import play.data.validation.Required;

@Entity
public class Task extends TemporalModel {
	@Required
	public String name;
	
	public String description;
	
	@ManyToOne(targetEntity = Project.class)
    public Project project;
    
    @Temporal(TemporalType.TIME)
    public Date plannedStart;
    
    @Temporal(TemporalType.TIME)
    public Date plannedEnd;
    
    public boolean isActive;
}
