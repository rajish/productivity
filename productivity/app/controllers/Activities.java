package controllers;

import java.util.List;
import models.Activity;
import play.mvc.Controller;
import play.i18n.Messages;
import play.data.validation.Validation;
import play.data.validation.Valid;


public class Activities extends Controller {
	public static void index() {
		List<Activity> entities = models.Activity.all().fetch();
		System.out.println("Activities.index(): entities.size()=" + entities.size());
		render(entities);
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

}
