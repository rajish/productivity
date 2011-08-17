package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;

import play.data.validation.Required;
import controllers.Security;

@Entity
public class Task extends TemporalModel {
    @Required
    @Field(index = Index.TOKENIZED)
    public String  name;

    @Field(index = Index.TOKENIZED)
    public String  description;

    @Required
    @ManyToOne(targetEntity = Project.class)
    public Project project;

    @Required
    @ManyToOne(targetEntity = User.class)
    public User    user;

    // @Temporal(TemporalType.TIMESTAMP)
    public Date    plannedStart;

    // @Temporal(TemporalType.TIMESTAMP)
    public Date    plannedEnd;

    public boolean isActive;

    /**
     * Find all activities for the currently logged in user
     * 
     * @return
     */
    public static List<Task> findByUser(String otherConditions, Object... params) {
        if (otherConditions == null) {
            return find("byUser", Security.getCurrentUser()).fetch();
        } else {
            String oc = otherConditions.startsWith("by") ? otherConditions.substring(2) : otherConditions;
            if (params.length > 0) {
                ArrayList<Object> p = new ArrayList<Object>(Arrays.asList(params));
                p.add(0, Security.getCurrentUser());
                return find("byUserAnd" + oc, p.toArray()).fetch();
            } else {
                return find("byUserAnd" + oc, Security.getCurrentUser()).fetch();
            }
        }
    }

    public String toString() {
        return "Task: { " + "id: " + id + "," + "created: " + created + "," + "updated: " + updated + "," + "name: " + name + "," + "description: " + description + ","
                + "project: " + project + "," + "plannedStart: " + plannedStart + "," + "plannedEnd: " + plannedEnd + "," + "isActive: " + isActive + "," + "}";
    }
}
