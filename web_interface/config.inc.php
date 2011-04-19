<?php
$siteCfg = new stdClass();
$siteCfg->validation_cfg = new stdClass();

// ----------------------------------------------------------------------------
/** [User Authentication] */

/**
 * Login authentication method:
 *              'MD5' => use password stored on db
 *    'LDAP' => use password from LDAP Server
 */
$siteCfg->authentication['method'] = 'LDAP';

/** LDAP authentication credentials */
$siteCfg->authentication['ldap_server']       = 'dir.adbgroup.pl';
$siteCfg->authentication['ldap_port']         = 389;
$siteCfg->authentication['ldap_version']      = '3'; // could be '2' in some cases
$siteCfg->authentication['ldap_root_dn']      = 'ou=People,o=Poland,dc=adb,dc=adbgroup';
$siteCfg->authentication['ldap_organization'] = '';    // e.g. '(organizationname=*Traffic)'
$siteCfg->authentication['ldap_uid_field']    = 'uid'; // Use 'sAMAccountName' for Active Directory
$siteCfg->authentication['ldap_bind_dn']      = ''; // Left empty for anonymous LDAP binding
$siteCfg->authentication['ldap_bind_passwd']  = ''; // Left empty for anonymous LDAP binding

/** [Database config] */
$siteCfg->db['db_server'] = 'localhost';
//$siteCfg->db['db_'] = '';
$siteCfg->db['db_engine'] = 'mysql';
$siteCfg->db['db_user'] = 'R.Galler';
$siteCfg->db['db_pass'] = '';


/** Enable/disable Users to create accounts on login page */
$siteCfg->user_self_signup = TRUE;

/** Validating new user login names */
$siteCfg->validation_cfg->user_login_valid_regex='/^[\w .\-]+$/';

/** Validating user email addresses */
/* added final i - to allow also Upper Case - info taken from PHP Manual and Mantis */
// $siteCfg->validation_cfg->user_email_valid_regex = "/^[a-z0-9!#$%&'*+\/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+\/=?^_`" .
//                                      "{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$/i";
//

/* Taken from Ext-js VTypes.js */
$siteCfg->validation_cfg->user_email_valid_regex_js = "/^([\w]+)(.[\w]+)*@([\w-]+\.){1,5}([A-Za-z]){2,4}$/";
$siteCfg->validation_cfg->user_email_valid_regex_php = "/^([\w]+)(.[\w]+)*@([\w-]+\.){1,5}([A-Za-z]){2,4}$/U";

?>
