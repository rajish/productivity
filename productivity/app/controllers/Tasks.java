package controllers;

import java.util.List;
import models.Task;
import play.mvc.Controller;
import play.i18n.Messages;
import play.data.validation.Validation;
import play.data.validation.Valid;


public class Tasks extends Controller {
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

}
