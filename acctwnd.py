#!/usr/bin/env python

import gtk
import wnck
import glib
import MySQLdb
import sys, os, pwd
from datetime import datetime, timedelta

start = datetime.now()
min_delta = timedelta(seconds = 1)

class Table:
      def __init__(self, db, name):
            self.db = db
            self.name = name
            self.dbc = self.db.cursor()

      def additem(self, item):
            sql = "INSERT INTO `" + self.name + "` ( " + item.getFields() + " ) VALUES( " + str(item) + ")"
            print "SQL> " + sql
            self.dbc.execute(sql)
            return

      def __getitem__(self, item):
            if type(item) is type(str()):
                  sql = "SELECT * FROM %s WHERE name = '%s'" %(self.name, item)
            else:
                  sql = "SELECT * FROM %s LIMIT %s, 1" %(self.name, item)
            print "SQL> " + sql
            self.dbc.execute(sql)
            return self.dbc.fetchone()

      def __len__(self):
            self.dbc.execute("select count(*) from %s" % (self.name))
            l = int(self.dbc.fetchone()[0])
            return l

class ActivityItem:
    def __init__(self, timestamp, app, title, user_id):
       self.ts = timestamp
       self.app = app
       self.title = title
       self.user = user_id

    def __str__(self):
        return "NULL, '" + str(self.ts) + "', '" + self.app + "', '" + self.title + "', '" + str(self.user) + "' "

    def getFields(self):
        return "`id` , `delta` , `name` , `title` , `user_id`"

class WindowTitle(object):
    def __init__(self):
        self.title = None
        users = Table(db, "users")
        self.current_user = pwd.getpwuid( os.getuid() )[0]
        try:
              self.user_id = users[self.current_user][0]
        except TypeError:
              print "Current user has no priviledges to modify the database"
              sys.exit(-1)
        glib.timeout_add(100, self.get_title)

    def get_title(self):
        global start, table, min_delta
        try:
            window = wnck.screen_get_default().get_active_window()
            title = window.get_name()
            if self.title != title:
                app = window.get_application().get_name()
                self.title  = title
                end = datetime.now()
                delta = end - start
                start = end
                if delta >= min_delta:
                    item = ActivityItem(delta, app, title, self.user_id)
                    table.additem(item)
                    print str(delta) + " [" + app + "]: \t" + title
        except AttributeError:
            pass
        return True

def main():
    global db, table
    db = MySQLdb.connect(db="productivity")
    table = Table(db, "activities")
    WindowTitle()
    gtk.main()

if __name__ == "__main__":
    main()
