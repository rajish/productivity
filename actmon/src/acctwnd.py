#!/usr/bin/env python
from datetime import datetime, timedelta
import MySQLdb
import ctypes
import glib
import gtk
import sys
import os
import pwd
import wnck

try:
    import Xlib.display
except:
    print '############################################################'
    print 'Module Xlib.display not found. Please install "python-xlib".'
    print '############################################################'
    raise


min_delta = timedelta(seconds=1)
max_delta = timedelta(minutes=10)
max_idle = 300 # in seconds

class XScreenSaverInfo(ctypes.Structure):
    """ typedef struct { ... } XScreenSaverInfo; """
    _fields_ = [('window', ctypes.c_ulong), # screen saver window
              ('state', ctypes.c_int), # off,on,disabled
              ('kind', ctypes.c_int), # blanked,internal,external
              ('since', ctypes.c_ulong), # milliseconds
              ('idle', ctypes.c_ulong), # milliseconds
              ('event_mask', ctypes.c_ulong)] # events

class XScreenSaverSession(object):
    def __init__(self):
        self.xlib = ctypes.cdll.LoadLibrary('libX11.so')
        self.dpy = self.xlib.XOpenDisplay(os.environ['DISPLAY'])
        if not self.dpy:
            raise Exception('Cannot open display')
        self.root = self.xlib.XDefaultRootWindow(self.dpy)
        self.xss = ctypes.cdll.LoadLibrary('libXss.so.1')
        self.xss.XScreenSaverAllocInfo.restype = ctypes.POINTER(XScreenSaverInfo)
        self.xss_info = self.xss.XScreenSaverAllocInfo()

    def get_idle(self):
        self.xss.XScreenSaverQueryInfo(self.dpy, self.root, self.xss_info)
        return self.xss_info.contents.idle / 1000


class Table:
    def __init__(self, db, name):
        self.db = db
        self.name = name
        self.dbc = self.db.cursor()

    def additem(self, item):
        # sql = "INSERT INTO `" + self.name + "` ( " + item.getFields() + " ) VALUES( " + str(item) + ")"
        sql = "INSERT INTO `%s` ( %s ) VALUES( %s )" % (self.name, item.getFields(), str(item))
        print "SQL> " + sql
        self.dbc.execute(sql)
        return

    def updateitem(self, item, column, value):
        sql = "UPDATE " + self.name + " SET " + column + " = " + value + " WHERE id = " + item
        print "UPDATE SQL> " + sql
        self.dbc.execute(sql)

    def __getitem__(self, item):
        if type(item) is type(str()):
            sql = "SELECT * FROM " + self.name + " WHERE name = %s"
        else:
            sql = "SELECT * FROM  " + self.name + " LIMIT %s, 1"

        print "SQL> " + sql % self.db.literal((item,))
        self.dbc.execute(sql, (item,))
        return self.dbc.fetchone()

    def __len__(self):
        self.dbc.execute("select count(*) from %s", self.name)
        l = int(self.dbc.fetchone()[0])
        return l

class ActivityItem:
    def __init__(self, timestamp, app, title, user_id):
        self.ts = timestamp
        self.time_end = timestamp
        self.app = app
        self.title = title
        self.user = user_id

    def __str__(self):
        global db
        record = "NULL, %s, %s, %s, %s, %s"
        return record % db.literal((str(self.ts), str(self.time_end), self.app, self.title, str(self.user)))

    def getFields(self):
        return "`id` , `timestamp` , `time_end`, `name` , `title` , `user_id`"

    def set_end(self, timestamp):
        self.time_end = timestamp

class WindowTitle(object):
    def __init__(self): #@IndentOk
        self.title = "daemon start"
        users = Table(db, "User")
        self.current_user = pwd.getpwuid(os.getuid())[0]
        # try:
        self.user_id = users[self.current_user][0]
        # except TypeError:
        #    print "Current user has no priviledges to modify the database"
        #   sys.exit(-1)
        self.item = ActivityItem(datetime.now(), "daemon", self.title, self.user_id)
        self.xscsaver = XScreenSaverSession()
        self.idle = False
        self.start = datetime.now()
        glib.timeout_add(100, self.get_title)

    def get_title(self):
        global act_table, min_delta, max_delta, db, max_idle
        try:
            window = wnck.screen_get_default().get_active_window()
            title = window.get_name()
            end = datetime.now()
            delta = end - self.start
            
            if delta >= max_delta:
                print str(end) + "> db.ping()"
                self.start = end
                db.ping()
            
            if self.idle and self.xscsaver.get_idle() < max_idle:
                self.idle = False
            
            if self.title != title or (self.xscsaver.get_idle() >= max_idle and not self.idle):
                app = window.get_application().get_name()
                self.title = title
                self.start = end
                
                if delta >= min_delta:
                    self.item.set_end(end)
                    act_table.additem(self.item)
                    print str(delta) + " [" + app + "]: \t" + title
                
                if self.xscsaver.get_idle() > max_idle:
                    app = "Screensaver"
                    title = "Idle"
                    self.idle = True
                
                self.item = ActivityItem(end, app, title, self.user_id)
        except AttributeError:
            pass
        return True



def window_focused_switch_handler(screen, window):
    # Get the focused window
    application = screen.get_active_window().get_application()
    # Get the name of the focused and running application
    cur_app_name = application.get_name()
    print "####  The name of current application is %s" % cur_app_name


def main():
    global db, act_table
    db = MySQLdb.connect(db="productivity")
    act_table = Table(db, "Activity")
    WindowTitle()
    scr = wnck.screen_get_default()
    # This is a standard GTK mechanism, which is required to capture all existing events
    while gtk.events_pending():
        gtk.main_iteration()
    
    # Set the listener method
    scr.connect("active-window-changed", window_focused_switch_handler)
    
    gtk.main()

if __name__ == "__main__":
    main()
