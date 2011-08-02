package controllers;

import java.util.List;

import javax.persistence.EntityManager;

import models.Activity;
import models.Task;
import models.User;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.util.Version;
import org.hibernate.Transaction;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;

import play.Logger;
import play.data.validation.Valid;
import play.db.Model;
import play.db.jpa.JPA;
import play.i18n.Messages;
import play.modules.paginate.ValuePaginator;
import play.mvc.Before;

public class Activities extends SearchableController {

    @Before
    public static void fillVars() {
        //Logger.setUp("DEBUG");
        List<Task> tasks = Task.findByUser("byIsActive", new Boolean(true));
        List<User> users = User.findAll();
        renderArgs.put("tasks", tasks);
        renderArgs.put("users", users);
    }

    public static void index(int rowCount) {
        Logger.debug("Activities.index() params: " + params.allSimple()
                     + " rowCount=" + rowCount);
        ValuePaginator<Activity> entities = new ValuePaginator(Activity.findByUser(null));
        int unassigned = Activity.findByUser("byTaskIsNull").size();
        rowCount = setRowCount(rowCount);
        entities.setPageSize(rowCount);
        render(entities, unassigned, rowCount);
    }

    public static void unassigned(int rowCount) {
        ValuePaginator<Activity> entities = new ValuePaginator(Activity.findByUser("byTaskIsNull"));
        int unassigned = entities.size();
        rowCount = setRowCount(rowCount);
        entities.setPageSize(rowCount);
        render(entities, unassigned, rowCount);
    }

    public static void create(Activity entity) {
        render(entity);
    }

    public static void show(java.lang.Long id) {
        Activity entity = Activity.findById(id);
        render(entity);
    }

    public static void edit(java.lang.Long id) {
        Activity entity = Activity.findById(id);
        render(entity);
    }

    public static void delete(java.lang.Long id) {
        Activity entity = Activity.findById(id);
        entity.delete();
        index(0);
    }

    public static void save(@Valid Activity entity) {
        if (validation.hasErrors()) {
            flash.error(Messages.get("scaffold.validation"));
            render("@create", entity);
        }
        entity.save();
        flash.success(Messages.get("scaffold.created", "Activity"));
        index(0);
    }

    public static void update(@Valid Activity entity) {
        if (validation.hasErrors()) {
            flash.error(Messages.get("scaffold.validation"));
            render("@edit", entity);
        }
        entity = entity.merge();
        entity.save();
        flash.success(Messages.get("scaffold.updated", "Activity"));
        index(0);
    }

    public static void updateAll(List<Activity> entity) {
        validation.errors("task").clear();
        if (validation.hasErrors()) {
            flash.error("Validation errors: %s", validation.errorsMap());
            index(0);
        }
        if (entity == null) {
            flash.error("Error updating activities - none given");
            index(0);
        }
        for (Activity act : entity) {
            if (act.task.id == 0) {
                act.task = null;
            }
            act = act.merge();
            act.save();
        }
        flash.success("All activities successfuly updated");
        index(0);
    }

    public static void search(int page, String search, String searchFields,
                              String orderBy, String order, int rowCount) throws ParseException {
        Logger.debug("Activities.search() params: "
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
        org.apache.lucene.search.Query queryl = parser.parse(search);

        // wrap Lucene query in a org.hibernate.Query
        org.hibernate.Query hibQuery = fullTextSession.createFullTextQuery(queryl, Activity.class);
        // execute search
        List result = hibQuery.list();
        Logger.debug("hibQuery = '%s'", hibQuery.getQueryString());
        tx.commit();

//        List<Activity> result = Activity.find(sql).fetch(page, rowCount);
        Logger.debug("Activities.search() result.count = " + result.size());
        ValuePaginator<Model> entities = new ValuePaginator(result);
        Long count = (long) result.size();
//        Long totalCount = Activity.count(sql);//, "%" + search + "%");
        Long totalCount = count;
        rowCount = SearchableController.setRowCount(rowCount);
        entities.setPageSize(rowCount);
        String type = "Activities";
        render(type, entities, count, totalCount, page, orderBy, order, rowCount);
    }
}
