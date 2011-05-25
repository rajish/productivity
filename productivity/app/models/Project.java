package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import play.data.validation.Required;

@Entity
public class Project extends TemporalModel {
	@Required
    public String name;
	
    public String description;
    
    @Temporal(TemporalType.DATE)
    public Date plannedStart;
    
    @Temporal(TemporalType.DATE)
    public Date plannedEnd;
    
    public boolean isActive;
}
