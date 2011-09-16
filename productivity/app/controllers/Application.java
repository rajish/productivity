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

import models.Activity;
import models.Portlet;
import models.Project;
import models.Task;
import play.Logger;
import play.mvc.Before;
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

    @Before
    public static void beforeAction() {
        Logger.setUp("DEBUG");
    }

    public static void index() {
        long unassigned = Activity.count("byUserAndTaskIsNull", Security.getCurrentUser());
        List<Portlet> portlets = session.get("layout");
        if(portlets == null) {
            portlets = new ArrayList();
            portlets.add(new Portlet("unfolded", "Statistics", "stats"));
            portlets.add(null);
            portlets.add(new Portlet("folded", "Timeline", "timeline"));
        }
        render(unassigned, portlets);
    }

    public static void storeConfig(List<Portlet> widgets) {
        Logger.debug("storeConfig: params: " + params.allSimple());
        Logger.debug("widgets: " + widgets);
        session.remove("layout");
        session.put("layout", widgets);
        renderJSON("OK");
    }

    public static void timeline()
    {
        List<Activity> unassigned = Activity.findByUser("byTaskIsNull");
        List<Project> projects = Project.find("byIsActive", new Boolean(true)).fetch();
        List<Timeline> timeline = new ArrayList<Timeline>();
        timeline.add(new Timeline("Unassigned", "Currently unassigned activities", null, 20, unassigned));
        for (Project proj : projects) {
            List plist = new ArrayList();
            // plist.add(proj);
            List<Task> tasks = Task.findByUser("byIsActiveAndProject", new Boolean(true), proj);
            for (Task task : tasks) {
                plist.add(task);
                plist.addAll(Activity.findByUser("byTask", task));
            }
            timeline.add(new Timeline(proj.name, proj.description, null, 20, plist ));
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

    public static class EventSerializer {
        private int importance;

        public EventSerializer(int imp) {
            importance = imp;
        }

        void addLink(JsonObject o, String controller, Long id) {
            Map m = Collections.synchronizedMap(new HashMap<String, Object>());
            m.put("id", id);
            o.addProperty("link", Router.getFullUrl(controller + ".edit", m));
        }

        void rotateImportance(JsonObject o, int min, int max) {
            o.addProperty("importance", importance++);
            if(importance > max)
                importance = min;
        }
    }

    public static class ActivitySerializer extends EventSerializer implements JsonSerializer<Activity> {

        public ActivitySerializer() {
            super(1);
        }

        @Override
            public JsonElement serialize(Activity act, Type actType,
                                         JsonSerializationContext ctx) {
            JsonObject retval = new JsonObject();
            retval.addProperty("id", "act-" + act.id);
            retval.addProperty("title", act.name);
            retval.addProperty("description", act.title);
            retval.addProperty("startdate", Timeline.DF.format(act.timestamp));
            retval.addProperty("enddate", Timeline.DF.format(act.time_end));
            retval.addProperty("date_display", "hour");
            retval.addProperty("high_threshold", 6);
            retval.addProperty("importance", 5);
            //            rotateImportance(retval, 1, 5);
            addLink(retval, "Activities", act.id);
            return retval;
        }
    }

    public static class TaskSerializer extends EventSerializer implements JsonSerializer<Task> {

        public TaskSerializer() {
            super(10);
        }

        @Override
            public JsonElement serialize(Task task, Type taskType,
                                         JsonSerializationContext ctx) {
            JsonObject retval = new JsonObject();
            retval.addProperty("id", "task-" + task.id);
            retval.addProperty("title", task.name);
            retval.addProperty("description", task.description);
            retval.addProperty("startdate", Timeline.DF.format(task.plannedStart));
            retval.addProperty("enddate", Timeline.DF.format(task.plannedEnd));
            retval.addProperty("date_display", "day");
            retval.addProperty("low_threshold", 5);
            retval.addProperty("high_threshold", 21);
            retval.addProperty("importance", 10);
            // rotateImportance(retval, 10, 20);
            addLink(retval, "Tasks", task.id);
            return retval ;
        }
    }

    public static class ProjectSerializer extends EventSerializer implements JsonSerializer<Project> {

        public ProjectSerializer() {
            super(30);
        }

        @Override
            public JsonElement serialize(Project proj, Type arg1,
                                         JsonSerializationContext arg2) {
            JsonObject retval = new JsonObject();
            retval.addProperty("id", "proj-" + proj.id);
            retval.addProperty("title", proj.name);
            retval.addProperty("description", proj.description);
            retval.addProperty("startdate", Timeline.DF.format(proj.plannedStart));
            retval.addProperty("enddate", Timeline.DF.format(proj.plannedEnd));
            retval.addProperty("date_display", "week");
            retval.addProperty("low_threshold", 18);
            retval.addProperty("high_threshold", 46);
            retval.addProperty("importance", 20);
            // rotateImportance(retval, 30, 45);
            addLink(retval, "Projects", proj.id);
            return retval ;
        }
    }
}
