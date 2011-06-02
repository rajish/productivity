package util;

import play.mvc.*;

@play.db.jpa.NoTransaction
public class ControllerUtil extends Controller {

	public static int setRowCount(int rowCount) {
		if(rowCount == 0) {
			rowCount = Integer.parseInt(session.get("rowCount"));
			System.out.println("Activities.setRowCount() got from session: " + rowCount);
		} else {
			session.remove("rowCount");
			session.put("rowCount", new Integer(rowCount));
		}
		return rowCount;
	}

}
