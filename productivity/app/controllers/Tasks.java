package controllers;

import java.util.List;

import models.Project;
import models.Task;
import play.mvc.Before;
import play.mvc.Controller;
import play.i18n.Messages;
import play.data.validation.Validation;
import play.data.validation.Valid;


public class Tasks extends Controller {
	@Before
	public static void fillVars() {
		List<Project> projects= Project.find("byIsActive", true).fetch();
		renderArgs.put("projects", projects);
	}
	
	public static void index() {
		List<Task> entities = models.Task.all().fetch();
		render(entities);
	}

	public static void create(Task entity) {
		render(entity);
	}

	public static void show(java.lang.Long id) {
    Task entity = Task.findById(id);
		render(entity);
	}

	public static void edit(java.lang.Long id) {
    Task entity = Task.findById(id);
		render(entity);
	}

	public static void delete(java.lang.Long id) {
    Task entity = Task.findById(id);
    entity.delete();
		index();
	}
	
	public static void save(@Valid Task entity) {
		if (validation.hasErrors()) {
			flash.error(Messages.get("scaffold.validation"));
			render("@create", entity);
		}
    entity.save();
		flash.success(Messages.get("scaffold.created", "Task"));
		index();
	}

	public static void update(@Valid Task entity) {
		if (validation.hasErrors()) {
			flash.error(Messages.get("scaffold.validation"));
			render("@edit", entity);
		}
		
      		entity = entity.merge();
		
		entity.save();
		flash.success(Messages.get("scaffold.updated", "Task"));
		index();
	}

	public static void updateAll(List<Task> tasks) {
		validation.errors("project").clear();
		if(validation.hasErrors()) {
			flash.error("Validation errors: %s", validation.errorsMap());
			index();
		}
		if(tasks == null) {
			flash.error("Error updating tasks - none given");
			index();
		}
		for(Task task : tasks) {
			if(task.project.id == 0) {
				task.project = null;
			}
			task = task.merge();
			task.save();
		}
		flash.success("Tasks successfuly updated");
		index();
	}
}
