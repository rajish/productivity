package controllers;

import java.util.List;

import models.Project;
import models.Task;
import play.data.validation.Valid;
import play.i18n.Messages;
import play.modules.paginate.ValuePaginator;
import play.mvc.Before;

public class Tasks extends SearchableController {

    @Before
    public static void fillVars() {
        List<Project> projects = Project.find("byIsActive", true).fetch();
        renderArgs.put("projects", projects);
        // setupRowCount();
    }

    public static void index(int rowCount) {
        ValuePaginator<Task> entities = new ValuePaginator(Task.findByUser(null));
        rowCount = setRowCount(rowCount);
        render(entities, rowCount);
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
        index(0);
    }

    public static void save(@Valid Task entity) {
        if (validation.hasErrors()) {
            flash.error(Messages.get("scaffold.validation"));
            render("@create", entity);
        }
        entity.save();
        flash.success(Messages.get("scaffold.created", "Task"));
        index(0);
    }

    public static void update(@Valid Task entity) {
        if (validation.hasErrors()) {
            flash.error(Messages.get("scaffold.validation"));
            render("@edit", entity);
        }

        entity = entity.merge();

        entity.save();
        flash.success(Messages.get("scaffold.updated", "Task"));
        index(0);
    }

    public static void updateAll(List<Task> entity) {
        System.out.println("Tasks.updateAll() params: " + params.allSimple() + "\ntasks: " + entity);
        validation.errors("project").clear();
        if (validation.hasErrors()) {
            flash.error("Validation errors: %s", validation.errorsMap());
            index(0);
        }
        if (entity == null) {
            flash.error("Error updating tasks - none given");
            index(0);
        }
        for (Task task : entity) {
            task = task.merge();
            task.save();
        }
        flash.success("Tasks successfuly updated");
        index(0);
    }

    @Override
    protected Class getOwnedModel() {
        return Task.class;
    }
}
