package controllers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.persistence.EntityManager;

import models.User;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;
import org.hibernate.Criteria;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;

import play.Logger;
import play.db.Model;
import play.db.jpa.JPA;
import play.modules.paginate.ValuePaginator;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Util;
import play.mvc.With;
import util.Config;
import controllers.deadbolt.Deadbolt;

@With(Deadbolt.class)
public abstract class SearchableController extends Controller {

    @Util
    public static int setRowCount(int rowCount) {
        if (rowCount == 0) {
            rowCount = Integer.parseInt(session.get("rowCount"));
            Logger.debug("SearchableController.setRowCount() got from session: "
                         + rowCount);
        } else {
            session.remove("rowCount");
            session.put("rowCount", new Integer(rowCount));
        }
        return rowCount;
    }

    protected abstract Class getOwnedModel() ;

    @Before
    public static void setupRowCount() {
        Logger.setUp("DEBUG");
        Logger.debug("SearchableController.setupRowCount() session: " + session.all());
        if (!session.contains("rowCount")) {
            Logger.info("ControllerUtil.setupRowCount() no 'rowCount' in session. Setting default to "
                        + Config.ROW_COUNT);
            session.put("rowCount", new Integer(Config.ROW_COUNT));
        }

    }

    @After
    public static void cleanUp() {
        Logger.setUp("WARN");
    }

    public static void search(int page, String search, String searchFields, String orderBy, String order, int rowCount) throws ParseException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Logger.debug("SearchableController.search() params: "
                     + params.allSimple() + " page: " + page + " search: " + search
                     + " fields: " + searchFields + " orderBy: " + orderBy
                     + " order: " + order);

        if (page < 1) {
            page = 1;
        }

        org.hibernate.Session s = ((org.hibernate.Session)JPA.em().getDelegate()) ;
        FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(s);
        SearchFactory searchFactory = fullTextSession.getSearchFactory();
        Transaction tx = fullTextSession.beginTransaction();

        // need to create an instance of the controller to call an overridden method
        SearchableController controller = (SearchableController) getControllerClass().getConstructor().newInstance();

        // create native Lucene query
        String[] fields = searchFields.split(" ");
        MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_31, fields, searchFactory.getAnalyzer(controller.getOwnedModel()));
        parser.setDefaultOperator(QueryParser.AND_OPERATOR);
        org.apache.lucene.search.Query queryl = parser.parse(search);

        // wrap Lucene query in a org.hibernate.Query
        Criteria criteria = fullTextSession.createCriteria(controller.getOwnedModel());
        criteria.add(Restrictions.eq("user.id", Security.getCurrentUser().getId()));
        org.hibernate.Query hibQuery = fullTextSession.createFullTextQuery(queryl, controller.getOwnedModel()).setCriteriaQuery(criteria);

        // execute search
        List result = hibQuery.list();
        Logger.debug("hibQuery = '%s'", hibQuery.getQueryString());
        tx.commit();

        Logger.debug("SearchableController.search() result.count = " + result.size());
        ValuePaginator<Model> entities = new ValuePaginator(result);
        Long count = (long) result.size();
        Long totalCount = count;
        rowCount = SearchableController.setRowCount(rowCount);
        entities.setPageSize(rowCount);
        String type = "Activities";
        render(type, entities, count, totalCount, page, orderBy, order, rowCount);
    }

}
