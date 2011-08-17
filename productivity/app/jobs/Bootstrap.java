package jobs;

import javax.persistence.EntityManager;

import models.AuthMethod;
import models.Role;
import models.User;

import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;

import play.Logger;
import play.Play;
import play.Play.Mode;
import play.db.jpa.JPA;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

@OnApplicationStart
public class Bootstrap extends Job {
    public void doJob() {
        Logger.info("Bootstrap START");
        setupAccounts();
        setupIndexer();
    }

    private void setupIndexer() {
        EntityManager em = JPA.entityManagerFactory.createEntityManager();
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);
        try {
            Logger.debug("Starting indexer...");
            fullTextEntityManager.createIndexer().startAndWait();
            Logger.debug("indexer started");
        } catch (InterruptedException ex) {
            Logger.error(ex.getLocalizedMessage());
        }
    }

    private void setupAccounts() {
        if(AuthMethod.count() == 0) {
            new AuthMethod(AuthMethod.LOCAL).create();
            new AuthMethod(AuthMethod.LDAP).create();
            Logger.info("Bootrstrap: created entries in AuthMethod.");
        }
        // create admin user if no users currently exist
        if (User.count() == 0) {
            User adminUser = new User();
            adminUser.setName("admin");
            adminUser.passwordHash = "admin";
            adminUser.authMethod = AuthMethod.getByName(AuthMethod.LOCAL);
            adminUser.save();
            Logger.info("Bootstrap: INSTALATION MODE!!! created user admin");
        }
        // create default roles
        if(Role.count() == 0) {
            (new Role("admin", 255)).save();
            (new Role("regular user", 15)).save();
            (new Role("quest", 1)).save();
        }
        // Development and test mode specific stuff
        if(Play.mode == Mode.DEV) {
            if(Play.runingInTestMode()) {
                Fixtures.deleteDatabase();
                Logger.info("Bootstrap: TEST MODE!!! Loading data");
                Fixtures.loadModels("data.yml");
            }
        }
    }
}
