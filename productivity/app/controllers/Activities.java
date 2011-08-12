package controllers;

import java.util.List;

import models.Activity;
import models.Task;
import models.User;

import play.Logger;
import play.data.validation.Valid;
import play.i18n.Messages;
import play.modules.paginate.ValuePaginator;
import play.mvc.Before;

public class Activities extends SearchableController {

    @Before
    public static void fillVars() {
        // Logger.setUp("DEBUG");
        List<Task> tasks = Task.findByUser("byIsActive", new Boolean(true));
        List<User> users = User.findAll();
        renderArgs.put("tasks", tasks);
        renderArgs.put("users", users);
    }

    public static void index(int rowCount) {
        Logger.debug("Activities.index() params: " + params.allSimple() + " rowCount=" + rowCount);
        ValuePaginator<Activity> entities = new ValuePaginator(Activity.findByUser(null));
        int unassigned = Activity.findByUser("byTaskIsNull").size();
        rowCount = setRowCount(rowCount);
        entities.setPageSize(rowCount);
        render(entities, unassigned, rowCount);
    }

    public static void unassigned(int rowCount) {
        ValuePaginator<Activity> entities = new ValuePaginator(Activity.findByUser("byTaskIsNull"));
        int unassigned = entities.size();
        rowCount = setRowCount(rowCount);
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
        index(0);
    }

    public static void save(@Valid Activity entity) {
        if (validation.hasErrors()) {
            flash.error(Messages.get("scaffold.validation"));
            render("@create", entity);
        }
        entity.save();
        flash.success(Messages.get("scaffold.created", "Activity"));
        index(0);
    }

    public static void update(@Valid Activity entity) {
        if (validation.hasErrors()) {
            flash.error(Messages.get("scaffold.validation"));
            render("@edit", entity);
        }
        entity = entity.merge();
        entity.save();
        flash.success(Messages.get("scaffold.updated", "Activity"));
        index(0);
    }

    public static void updateAll(List<Activity> entity) {
        validation.errors("task").clear();
        if (validation.hasErrors()) {
            flash.error("Validation errors: %s", validation.errorsMap());
            index(0);
        }
        if (entity == null) {
            flash.error("Error updating activities - none given");
            index(0);
        }
        for (Activity act : entity) {
            if (act.task.id == 0) {
                act.task = null;
            }
            act = act.merge();
            act.save();
        }
        flash.success("All activities successfuly updated");
        index(0);
    }

    @Override
    protected Class getOwnedModel() {
        return Activity.class;
    }
}
