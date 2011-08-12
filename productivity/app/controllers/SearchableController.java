package controllers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import models.Activity;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.util.Version;
import org.hibernate.Transaction;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;

import controllers.deadbolt.Deadbolt;
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

        EntityManager em = JPA.entityManagerFactory.createEntityManager();
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);
        try {
            Logger.debug("Starting indexer...");
            fullTextEntityManager.createIndexer().startAndWait();
            Logger.debug("indexer started");
        } catch (InterruptedException ex) {
            Logger.error(ex.getLocalizedMessage());
        }
        org.hibernate.Session s = ((org.hibernate.Session)JPA.em().getDelegate()) ;
        FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(s);
        Transaction tx = fullTextSession.beginTransaction();

        // create native Lucene query
        String[] fields = searchFields.split(" ");
        MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_31, fields, new StandardAnalyzer(Version.LUCENE_31));
        org.apache.lucene.search.Query queryl = parser.parse(search + " user:" + Security.connected());

        // wrap Lucene query in a org.hibernate.Query
        Class cls = getControllerClass(); // need to create an instance to call an overridden method
        Constructor constructor = cls.getConstructor();
        SearchableController controller = (SearchableController) constructor.newInstance();
        org.hibernate.Query hibQuery = fullTextSession.createFullTextQuery(queryl, controller.getOwnedModel());
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
