package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.joda.time.Period;

import play.data.validation.InPast;
import play.data.validation.Required;
import play.db.jpa.Model;
import controllers.Security;

@Entity
@Indexed
public class Activity extends Model {
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "timestamp", nullable = false)
    @InPast
    public Date   timestamp;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "time_end", nullable = false)
    @InPast
    public Date   time_end;

    @Required
    @Field(index = Index.TOKENIZED)
    public String name;

    @Required
    @Field(index = Index.TOKENIZED)
    public String title;

    @ManyToOne(targetEntity = Task.class)
    public Task   task;

    @Required
    @ManyToOne(targetEntity = User.class)
    public User   user;

    /**
     * Find all activities for the currently logged in user
     * 
     * @return
     */
    public static List<Activity> findByUser(String otherConditions, Object... params) {
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

    public Period duration() {
        long duration = time_end.getTime() - timestamp.getTime();
        return new Period(duration);
    }

    public String toString() {
        return "Activity[" + id + "] {" + timestamp + ", " + time_end + ", " + name + ", " + title + ", " + (task != null ? (task.name + ", ") : "(no task), ")
                + (user != null ? (user.getName()) : "(no user)") + "}";
    }
}
