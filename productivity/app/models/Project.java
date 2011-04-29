package models;

import java.util.Date;

import javax.persistence.Entity;

@Entity(name="projects")
public class Project extends TemporalModel {
    public String name;
    public String description;
    public Date plannedStart;
    public Date plannedEnd;
    public boolean isActive;
}
