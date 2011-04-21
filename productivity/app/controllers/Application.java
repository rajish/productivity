package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

public class Application extends Controller {

    public static void index() {
    	long unassigned = Activity.count("task_id = 0");
        render(unassigned);
    }
}