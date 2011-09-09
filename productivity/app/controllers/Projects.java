package controllers;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import models.Activity;
import models.Project;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;

import play.data.validation.Valid;
import play.db.jpa.JPA;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;


public class Projects extends SearchableController {
    @Before
    public static void beforeAction() {
        List<Project> projects =
            Project.find("byIsActive", new Boolean(true)).fetch();
        renderArgs.put("projects", projects);
    }

    public static void index() {
        List<Project> entities = models.Project.all().fetch();
        render(entities);
    }

    public static void create(Project entity) {
        render(entity);
    }

    public static void show(java.lang.Long id) {
        Project entity = Project.findById(id);
        render(entity);
    }

    public static void edit(java.lang.Long id) {
        Project entity = Project.findById(id);
        render(entity);
    }

    public static void delete(java.lang.Long id) {
        Project entity = Project.findById(id);
        entity.delete();
        index();
    }

    public static void save(@Valid Project entity) {
        if (validation.hasErrors()) {
            flash.error(Messages.get("scaffold.validation"));
            render("@create", entity);
        }
        entity.save();
        flash.success(Messages.get("scaffold.created", "Project"));
        index();
    }

    public static void update(@Valid Project entity) {
        if (validation.hasErrors()) {
            flash.error(Messages.get("scaffold.validation"));
            render("@edit", entity);
        }

        entity = entity.merge();

        entity.save();
        flash.success(Messages.get("scaffold.updated", "Project"));
        index();
    }

    @Override
    @Util
    protected Class getOwnedModel() {
        return Project.class;
    }

    public static void getStatistics() {
        List<Project> projects = Project.find("byIsActive", new Boolean(true)).fetch();
        renderJSON(projects, new ProjectSerializer());
    }

    public static class ProjectSerializer implements JsonSerializer<Project> {

        @Override
        public JsonElement serialize(Project proj, Type projType, JsonSerializationContext ctx) {
            JsonObject retval = new JsonObject();
            retval.addProperty("projName", proj.name);
            retval.addProperty("projStart", proj.plannedStart.toString());
            retval.addProperty("projEnd", proj.plannedEnd.toString());

            TypedQuery<Date> q1 = JPA.em().createQuery(
                    "select min(act.timestamp) from Activity as act, Task as task where act.task = task and task.project = :proj", Date.class);
            q1.setParameter("proj", proj);
            Date d = q1.getSingleResult();
            retval.addProperty("realStart", d.toString());

            Period deadline = new Period(proj.plannedEnd.getTime() - (new Date()).getTime());
            PeriodFormatter pf = PeriodFormat.getDefault();
            retval.addProperty("daysToDeadline", deadline.toString(pf));

            List<Activity> acts = JPA.em().createQuery(
                    "select act from Activity as act, Task as task where act.task = task and task.project = :proj",
                    Activity.class).setParameter("proj", proj).getResultList();
            Period wh = new Period();
            for(Activity act : acts) {
                wh.plus(act.duration());
            }
            retval.addProperty("workHours", wh.toString(pf));
            return retval;
        }
    }

}
