package controllers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.naming.NameClassPair;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import models.AuthMethod;
import models.Role;
import models.User;
import models.deadbolt.AccessResult;
import models.deadbolt.ExternalizedRestrictions;
import models.deadbolt.RoleHolder;

import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.NameClassPairCallbackHandler;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;

import play.Logger;
import play.modules.spring.Spring;
import play.mvc.Before;
import play.mvc.Util;
import util.Config;
import controllers.deadbolt.DeadboltHandler;
import controllers.deadbolt.ExternalizedRestrictionsAccessor;
import controllers.deadbolt.RestrictedResourcesHandler;

public class Security extends Secure.Security implements DeadboltHandler {

    @Before
    public static void initVars() {
        //Logger.setUp("DEBUG");
    }

    @Util
    static boolean authenticate(String userID, String password) {
        User user = User.connect(userID, password);
        if (user == null) {
            flash.error("Invalid userid or password.");
            return false;
        }
        return true;
    }

    @Util
    public static String md5(String password) {
        byte[] bytesOfMessage = password.getBytes();
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Logger.fatal(e, "System configuration error");
            return null;
        }
        byte[] thedigest = md.digest(bytesOfMessage);
        String passwordHash = new String(thedigest);
        return passwordHash;
    }

    @Util
    static boolean check(String profile) {
        User user = User.getByUserName(connected());
        return user.role != null && user.role.name.equalsIgnoreCase(profile);
    }

    @Override
    public void beforeRoleCheck() {
        if (!Security.isConnected()) {
            try {
                if (!session.contains("username")) {
                    flash.put("url", "GET".equals(request.method) ? request.url : "/");
                    Secure.login();
                }
            } catch (Throwable t) {
                // handle exception
            }
        }
    }

    @Util
    public static User getCurrentUser() {
        String userName = connected();
        return User.getByUserName(userName);
    }

    @Util
    @Override
    public RoleHolder getRoleHolder() {
        return (RoleHolder) getCurrentUser();
    }

    @Util
    @Override
    public void onAccessFailure(String controllerClassName) {
        forbidden();
    }

    @Util
    @Override
    public ExternalizedRestrictionsAccessor getExternalizedRestrictionsAccessor() {
        return new ExternalizedRestrictionsAccessor() {
            public ExternalizedRestrictions getExternalizedRestrictions(String name) {
                return null;
            }
        };
    }

    @Override
    @Util
    public RestrictedResourcesHandler getRestrictedResourcesHandler() {
        return new RestrictedResourcesHandler() {

            @Override
            public AccessResult checkAccess(List<String> resourceNames) {
                Logger.debug("Security.getRestrictedResourcesHandler().new RestrictedResourcesHandler() {...}.checkAccess() resourceNames = %s", resourceNames);
                if (resourceNames.get(0).equals("current_user")) {
                    String name = request.params.get("name");
                    if (connected().equals(name)) {
                        Logger.debug("Security.getRestrictedResourcesHandler().new RestrictedResourcesHandler() {...}.checkAccess() ALLOWED");
                        return AccessResult.ALLOWED;
                    }
                }
                Logger.debug("Security.getRestrictedResourcesHandler().new RestrictedResourcesHandler() {...}.checkAccess() NOT_SPECIFIED");
                return AccessResult.NOT_SPECIFIED;
            }

        };
    }
}
