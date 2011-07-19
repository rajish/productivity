package jobs;

import models.AuthMethod;
import models.User;
import play.Logger;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

@OnApplicationStart
public class Bootstrap extends Job {
    public void doJob() {
        Logger.info("Bootstrap START");
        if(AuthMethod.count() == 0) {
            new AuthMethod(AuthMethod.LOCAL).create();
            new AuthMethod(AuthMethod.LDAP).create();
            Logger.info("Bootrstrap: created entries in AuthMethod.");
        }
        // create admin user if no users currently exist
        if (User.count() == 0) {
            User adminUser = new User();
            adminUser.setName("admin");
            adminUser.setPassword("admin");
            adminUser.save();
            Logger.info("Bootstrap: INSTALATION MODE!!! created user admin");
        }

    }
}
