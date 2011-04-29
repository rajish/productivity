package controllers;

import java.util.List;

import models.Project;
import play.mvc.Before;
import play.mvc.Controller;

public class Projects extends Controller {

	@Before
	public static void beforeAction() {
		List<Project> projects = Project.find("byIsActive").fetch();
		renderArgs.put("projects", projects);
	}

    public static void index() {
        render();
    }

}
