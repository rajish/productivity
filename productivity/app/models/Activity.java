package models;

import java.sql.Time;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.elasticsearch.annotations.ElasticSearchable;

@ElasticSearchable
@Entity
public class Activity extends Model {
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "timestamp", nullable = false)
	public Date timestamp;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "time_end", nullable = false)
	public Date time_end;
	
	@Required
	public String name;
	
	@Required
	public String title;
	
	@ManyToOne(targetEntity = Task.class)
	public Task task;
	
	@Required
	@ManyToOne(targetEntity = User.class)
	public User user;
	
	public Time duration() {
		long duration = time_end.getTime() - timestamp.getTime();
		return new Time(duration);
	}
	
	public String toString() {
		return "Activity[" + id + "] {" 
			+ timestamp + ", "
			+ time_end + ", "
			+ name + ", "
			+ title + ", "
			+ (task != null ? (task.name + ", ") : "(no task), ")
			+ (user != null ? (user.getName()) : "(no user)")
			+ "}"
		;
	}
}
