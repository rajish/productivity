package models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import play.data.validation.Required;
import play.db.jpa.Model;

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
	
	@OneToOne(targetEntity = Task.class)
	public Task task;
	
	@ManyToOne(targetEntity = User.class)
	public User user;
}
