package controllers;

import java.util.List;

import models.AuthMethod;
import models.Role;
import models.User;
import play.Logger;
import play.data.validation.Valid;
import play.i18n.Messages;
import play.modules.paginate.ValuePaginator;
import play.mvc.Before;
import controllers.deadbolt.Restrict;
import controllers.deadbolt.RestrictedResource;


@Restrict("admin")
public class Roles extends SearchableController {

    @Before
    public static void fillVars() {
        Logger.setUp("DEBUG");
        List<Role> roles = Role.findAll();
        renderArgs.put("roles", roles);
        List<AuthMethod> authMethods = AuthMethod.findAll();
        renderArgs.put("authMethods", authMethods );
    }

    public static void index(int rowCount) {
        List<User> entities = new ValuePaginator(User.findAll());
        rowCount = setRowCount(rowCount);
        render(entities, rowCount);
    }

    public static void create(User entity) {
        render(entity);
    }

    public static void show(java.lang.Long id) {
        User entity = User.findById(id);
        render(entity);
    }

    public static void edit(java.lang.Long id) {
        User entity = User.findById(id);
        render(entity);
    }

    public static void delete(java.lang.Long id) {
        User entity = User.findById(id);
        entity.delete();
        index(0);
    }

    public static void save(@Valid User entity) {
        if (validation.hasErrors()) {
            flash.error(Messages.get("scaffold.validation"));
            render("@create", entity);
        }
        entity.save();
        flash.success(Messages.get("scaffold.created", "Role"));
        index(0);
    }

    public static void update(@Valid User entity) {
        if (validation.hasErrors()) {
            flash.error(Messages.get("scaffold.validation"));
            render("@edit", entity);
        }

        entity = entity.merge();

        entity.save();
        flash.success(Messages.get("scaffold.updated", "Role"));
        index(0);
    }

    @Override
    protected Class getOwnedModel() {
        return Role.class;
    }
}
