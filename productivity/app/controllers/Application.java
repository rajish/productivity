package controllers;

import java.util.List;

import models.Activity;
import models.Task;
import models.User;

import controllers.deadbolt.Deadbolt;

import play.mvc.Controller;
import play.mvc.With;
import play.mvc.Before;

@With(Deadbolt.class)
public class Application extends Controller {

    @Before
    public static void fillVars() {
        // Logger.setUp("DEBUG");
        List<Task> tasks = Task.findByUser("byIsActive", new Boolean(true));
        List<User> users = User.findAll();
        renderArgs.put("tasks", tasks);
        renderArgs.put("users", users);
    }

    public static void index() {
        long unassigned = Activity.count("task_id = null");
        render(unassigned);
    }
}
