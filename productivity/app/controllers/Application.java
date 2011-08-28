package controllers;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

import models.Activity;
import models.Project;
import models.Task;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.With;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import controllers.deadbolt.Deadbolt;

@With(Deadbolt.class)
public class Application extends Controller {

    public static void index() {
        long unassigned = Activity.count("byUserAndTaskIsNull", Security.getCurrentUser());
        render(unassigned);
    }

    public static void timeline()
    {
        List<Activity> unassigned = Activity.findByUser("byTaskIsNull");
        List<Project> projects = Project.find("byIsActive", new Boolean(true)).fetch();
        List<Timeline> timeline = new ArrayList<Timeline>();
        timeline.add(new Timeline("Unassigned", "Currently unassigned activities", null, 20, unassigned));
        for (Project proj : projects) {
            List<Project> plist = new ArrayList<Project>();
            plist.add(proj);
            timeline.add(new Timeline(proj.name, proj.description, null, 20, plist ));
            List<Task> tasks = Task.findByUser("byIsActiveAndProject", new Boolean(true), proj);
            for (Task task : tasks) {
                List<Task> tlist = new ArrayList<Task>();
                tlist.add(task);
                List<Activity> alist = Activity.findByUser("byTask", task);
                timeline.add(new Timeline(task.name, task.description, null, 20, tlist));
                timeline.add(new Timeline(task.name, task.description, null, 20, alist));
            }
        }
        renderJSON(timeline,
                   new TimelineSerializer(),
                   new ActivitySerializer(),
                   new ProjectSerializer(),
                   new TaskSerializer());
    }

    public static class Timeline {
        private static Long lastId = (long) 0;
        private String title;
        private String description;
        private Date focusDate;
        private int initialZoom;
        private List events;
        private Long id;
        public static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
        public Timeline(String title, String description, Date focus_date,
                        int zoom, List events) {
            this.title = title;
            this.description = description;
            this.focusDate = focus_date;
            this.initialZoom = zoom;
            this.events = events;
            this.id = lastId++;
        }
        public List getEvents() {
            return events;
        }
        public String getTitle() {
            return title;
        }
        public String getId() {
            return id.toString();
        }
        public String getFocusDate() {
            if (focusDate == null) {
                return DF.format((new Date()));
            }
            return DF.format(focusDate);
        }
        public String getDescription() {
            return description;
        }
        public int getZoom() {
            return initialZoom;
        }
    }

    public static class TimelineSerializer implements JsonSerializer<Timeline> {

        @Override
            public JsonElement serialize(Timeline timeline, Type timelineType,
                                         JsonSerializationContext context) {
            JsonObject retval = new JsonObject();
            retval.addProperty("id", "tl-" + timeline.getId());
            retval.addProperty("title", timeline.getTitle());
            retval.addProperty("focus_date", timeline.getFocusDate());
            retval.addProperty("description", timeline.getDescription());
            retval.addProperty("initial_zoom", timeline.getZoom());
            JsonArray events = new JsonArray();
            for (Object ev : timeline.getEvents()) {
                events.add(context.serialize(ev, ev.getClass()));
            }
            retval.add("events", events);
            return retval;
        }
    }

    public static class ActivitySerializer implements JsonSerializer<Activity> {

        @Override
            public JsonElement serialize(Activity act, Type actType,
                                         JsonSerializationContext ctx) {
            JsonObject retval = new JsonObject();
            retval.addProperty("id", "act-" + act.id);
            retval.addProperty("title", act.name);
            retval.addProperty("description", act.title);
            retval.addProperty("startdate", Timeline.DF.format(act.timestamp));
            retval.addProperty("enddate", Timeline.DF.format(act.time_end));
            Map m = Collections.synchronizedMap(new HashMap<String, Object>());
            m.put("id", act.id);
            retval.addProperty("link",
                               Router.getFullUrl("Activities.edit", m));
            return retval;
        }
    }

    public static class TaskSerializer implements JsonSerializer<Task> {

        @Override
            public JsonElement serialize(Task task, Type taskType,
                                         JsonSerializationContext ctx) {
            JsonObject retval = new JsonObject();
            retval.addProperty("id", "task-" + task.id);
            retval.addProperty("title", task.name);
            retval.addProperty("description", task.description);
            retval.addProperty("startdate", Timeline.DF.format(task.plannedStart));
            retval.addProperty("enddate", Timeline.DF.format(task.plannedEnd));
            retval.addProperty("link",
                               Router.getFullUrl("Tasks.edit(" + task.id + ")}"));
            return retval ;
        }
    }

    public static class ProjectSerializer implements JsonSerializer<Project> {

        @Override
            public JsonElement serialize(Project proj, Type arg1,
                                         JsonSerializationContext arg2) {
            JsonObject retval = new JsonObject();
            retval.addProperty("id", "proj-" + proj.id);
            retval.addProperty("title", proj.name);
            retval.addProperty("description", proj.description);
            retval.addProperty("startdate", Timeline.DF.format(proj.plannedStart));
            retval.addProperty("enddate", Timeline.DF.format(proj.plannedEnd));
            retval.addProperty("link",
                               Router.getFullUrl("@{Projects.edit(" + proj.id + ")}"));
            return retval ;
        }
    }
}
