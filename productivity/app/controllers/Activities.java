package controllers;

import java.util.List;

import models.Activity;
import models.Task;
import models.User;
import play.data.validation.Valid;
import play.db.Model;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
import play.modules.paginate.ValuePaginator;
import play.mvc.Before;
import util.Config;
import util.ControllerUtil;

@ElasticSearchController.For(Activity.class)
public class Activities extends ElasticSearchController {

	@Before
	public static void fillVars() {
		List<Task> tasks = Task.find("byIsActive", true).fetch();
		List<User> users = User.findAll();
		renderArgs.put("tasks", tasks);
		renderArgs.put("users", users);
		System.out.println("Activities.fillVars() session: " + session.all());
		if (!session.contains("rowCount")) {
			System.out.println("Activities.fillVars() no 'rowCount' in session. Setting default to " + Config.ROW_COUNT);
			session.put("rowCount", new Integer(Config.ROW_COUNT));
		}
	}
	
	public static void index(int rowCount) {
		System.out.println("Activities.index() params: " + params.allSimple() + " rowCount=" + rowCount);
		ValuePaginator<Activity> entities = new ValuePaginator(Activity.findAll());
		int unassigned = Activity.find("byTaskIsNull").fetch().size();
		rowCount = ControllerUtil.setRowCount(rowCount);
		entities.setPageSize(rowCount);
		render(entities, unassigned, rowCount);
	}

	public static void unassigned(int rowCount) {
		ValuePaginator<Activity> entities = new ValuePaginator(Activity.find("byTaskIsNull").fetch());
		int unassigned = entities.size();
		rowCount = ControllerUtil.setRowCount(rowCount);
		entities.setPageSize(rowCount);
		render(entities, unassigned, rowCount);
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
	
	public static void search(int page, String search, String searchFields, String orderBy, String order, int rowCount) {
		System.out.println("ElasticSearchController.search() params: " + params.allSimple() + " page: " + page + " search: " + search + " fields: " + searchFields + " orderBy: " + orderBy + " order: " + order);
		ObjectType type = ObjectType.get(getControllerClass());
		notFoundIfNull(type);
		if (page < 1) {
			page = 1;
		}
		
		ValuePaginator<Model> entities = new ValuePaginator(type.findPage(page, search, searchFields, orderBy, order, (String) request.args.get("where")));
		Long count = type.count(search, searchFields, (String) request.args.get("where"));
		Long totalCount = type.count(null, null, (String) request.args.get("where"));
		
		rowCount = ControllerUtil.setRowCount(rowCount);
		entities.setPageSize(rowCount);
		try {
			render(type, entities, count, totalCount, page, orderBy, order, rowCount);
		} catch (TemplateNotFoundException e) {
			render("ELASTIC_SEARCH/search.html", type, entities, count, totalCount, page, orderBy, order);
		}
	}
}
