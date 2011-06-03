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
    
    //@Temporal(TemporalType.TIMESTAMP)
    public Date plannedStart;
    
    //@Temporal(TemporalType.TIMESTAMP)
    public Date plannedEnd;
    
    public boolean isActive;
    
    public String toString() {
    	return "Task: { "
    		+ "id: "           + id + ","
    		+ "created: "      + created + ","
    		+ "updated: "      + updated + ","
    		+ "name: "         + name + ","
    		+ "description: "  + description + ","
    		+ "project: "      + project + ","
    		+ "plannedStart: " + plannedStart + ","
    		+ "plannedEnd: "   + plannedEnd + ","
    		+ "isActive: "     + isActive + ","
    		+ "}";
    }
}
