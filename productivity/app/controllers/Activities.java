package controllers;

import java.util.List;
import models.Activity;
import models.Task;
import models.User;
import play.mvc.Before;
import play.mvc.Controller;
import play.i18n.Messages;
import play.data.binding.As;
import play.data.validation.Validation;
import play.data.validation.Valid;


public class Activities extends Controller {
	@Before
	public static void fillVars() {
		List<Task> tasks = Task.find("byIsActive", true).fetch();
		List<User> users = User.findAll();
		renderArgs.put("tasks", tasks);
		renderArgs.put("users", users);
	}
	
	public static void index() {
		List<Activity> entities = Activity.findAll();
		//System.out.println("Activities.index(): entities.size()=" + entities.size());
		int unassigned = Activity.find("byTask_id", 0).fetch().size();
		render(entities, unassigned);
	}

	public static void unassigned() {
		List<Activity> entities = Activity.find("byTask_id", 0).fetch();
		//System.out.println("Activities.index(): entities.size()=" + entities.size());
		int unassigned = entities.size();
		render(entities, unassigned);    	
    }

	public static void create(Activity entity) {
		render(entity);
	}

	public static void show(java.lang.Long id) {
    Activity entity = Activity.findById(id);
		render(entity);
	}

	public static void edit(java.lang.Long id) {
    Activity entity = Activity.findById(id);
		render(entity);
	}

	public static void delete(java.lang.Long id) {
    Activity entity = Activity.findById(id);
    entity.delete();
		index();
	}
	
	public static void save(@Valid Activity entity) {
		if (validation.hasErrors()) {
			flash.error(Messages.get("scaffold.validation"));
			render("@create", entity);
		}
		entity.save();
		flash.success(Messages.get("scaffold.created", "Activity"));
		index();
	}

	public static void update(@Valid Activity entity) {
		if (validation.hasErrors()) {
			flash.error(Messages.get("scaffold.validation"));
			render("@edit", entity);
		}
		
      		entity = entity.merge();
		
		entity.save();
		flash.success(Messages.get("scaffold.updated", "Activity"));
		index();
	}

	public static void updateAll(List<Activity> entity) {
		validation.errors("task").clear();
		if(validation.hasErrors()) {
			flash.error("Validation errors: %s", validation.errorsMap());
			index();
		}
		if(entity == null) {
			flash.error("Error updating activities - none given");
			index();
		}
		for(Activity act : entity) {
			if(act.task.id == 0) {
				act.task = null;
			}
			act = act.merge();
			act.save();
		}
		flash.success("All activities successfuly updated");
		index();
	}
}
