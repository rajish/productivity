package controllers;

import play.mvc.Before;
import play.mvc.Controller;
import util.Config;
import controllers.ElasticSearchController;


public class SearchableController extends Controller {

	public static int setRowCount(int rowCount) {
		if (rowCount == 0) {
			rowCount = Integer.parseInt(session.get("rowCount"));
			System.out.println("SearchableController.setRowCount() got from session: "
					+ rowCount);
		} else {
			session.remove("rowCount");
			session.put("rowCount", new Integer(rowCount));
		}
		return rowCount;
	}

	@Before
	public static void setupRowCount() {
		System.out.println("ControllerUtil.setupRowCount() session: " + session.all());
		if (!session.contains("rowCount")) {
			System.out
					.println("ControllerUtil.setupRowCount() no 'rowCount' in session. Setting default to "
							+ Config.ROW_COUNT);
			session.put("rowCount", new Integer(Config.ROW_COUNT));
		}

	}

}
