#!/usr/bin/python
#
# simple d-busified evolution mail notifier
#
# notifies you about incoming mail with a sound and 
# displays a tray icon in the notification area until
# you read some message in evolution again
#
# Thomas Perl <thp@thpinfo.com> 2007-03-28
#
# See http://thpinfo.com/2007/hacks/ for updates
#
# Changes:
#
# 2007-04-20
#   + Better unread tracking
#   + new_mail_folders handling
#   + Use pynotify for neat notifications
#   + Customizable wave sounds
#
# 2007-08-29
#   + Add wave file playing limit
#
# 2007-09-13
#   + Add support for getting X11 idle time
#     -> skip sound playback when idle
#   + Add support for getting X11 focused window class
#     -> skip notification display when evolution is focused
#

import dbus
import dbus.glib
import dbus.decorators
import pynotify
import gobject
import gtk
import os
import os.path
import glob
import random
import time
import sys
import ctypes

try:
    import Xlib.display
except:
    print '############################################################'
    print 'Module Xlib.display not found. Please install "python-xlib".'
    print '############################################################'
    raise

#
# Set this variable to the names of folders in which
# new mails you want to be notified about appear
#
new_mail_folders = (
        'INBOX',
        'incoming',
        'incoming/mailing-lists'
)

#
# Configure the wave file(s) to play here. Tilde is 
# expanded to user's home directory and wildcards are
# possible. If using wildcards, a randomly selected
# file will be played from the matches.
#

# Download samples here: http://www.clayloomis.com/mailcall.html
wavefile = '~/lib/privat/newmail/*.wav'
# Or use a sound from /usr/share/sounds, like this:
#wavefile = '/usr/share/sounds/gaim/receive.wav'

# Number of seconds to wait after a sound has been played
# before another sound will be played
sound_timeout = 10

# Number of seconds that the session will be considered 
# inactive (sounds are only played when the session is inactive).
# Set this to "0" if you always want to hear sound notifications.
x11_idle_time = 10


# END OF CONFIGURATION


class XScreenSaverInfo( ctypes.Structure):
    """ typedef struct { ... } XScreenSaverInfo; """
    _fields_ = [('window',      ctypes.c_ulong), # screen saver window
                ('state',       ctypes.c_int),   # off,on,disabled
                ('kind',        ctypes.c_int),   # blanked,internal,external
                ('since',       ctypes.c_ulong), # milliseconds
                ('idle',        ctypes.c_ulong), # milliseconds
                ('event_mask',  ctypes.c_ulong)] # events

class XScreenSaverSession(object):
    def __init__( self):
        self.xlib = ctypes.cdll.LoadLibrary( 'libX11.so')
        self.dpy = self.xlib.XOpenDisplay( os.environ['DISPLAY'])
        if not self.dpy:
            raise Exception('Cannot open display')
        self.root = self.xlib.XDefaultRootWindow( self.dpy)
        self.xss = ctypes.cdll.LoadLibrary( 'libXss.so')
        self.xss.XScreenSaverAllocInfo.restype = ctypes.POINTER(XScreenSaverInfo)
        self.xss_info = self.xss.XScreenSaverAllocInfo()

    def get_idle( self):
        self.xss.XScreenSaverQueryInfo( self.dpy, self.root, self.xss_info)
        return self.xss_info.contents.idle / 1000

class XFocus(object):
    def __init__( self):
        self.display = Xlib.display.Display()

    def get_wm_class( self):
        focus = self.display.get_input_focus()
        return focus.focus.get_wm_class()

x11_session = XScreenSaverSession()
x11_focus = XFocus()

unread_folders = []
last_sound_played = time.time() - sound_timeout

pynotify.init('evolution-newmail')
notification = None

bus = dbus.SessionBus()

icon = gtk.StatusIcon()
icon.set_visible( False)
icon.set_from_icon_name( 'evolution')
icon.set_tooltip( 'New mail arrived!')

def update_tooltip( show_notification = True):
    global notification, icon, unread_folders, x11_focus

    message = 'You have new e-mail messages in %s.' % ( ', '.join( [ '"%s"' % f for f in unread_folders ]), )

    if notification != None:
        notification.close()

    ( wm_instance, wm_class ) = x11_focus.get_wm_class()

    notification = pynotify.Notification( 'New mail received!', message, 'emblem-mail')
    notification.set_property( 'status-icon', icon)

    if len(unread_folders) == 0:
        notification.close()
        icon.set_visible( False)
    else:
        if show_notification and wm_instance != 'evolution':
            # Only display notification when "evolution" is not focused
            notification.show()
        else:
            notification.close()
        icon.set_visible( True)

    return False

def newmail_received( path, folder):
    global notification, unread_folders, new_mail_folders
    global wavefile, last_sound_played, sound_timeout, x11_session, x11_idle_time

    if folder in new_mail_folders and folder not in unread_folders:
        unread_folders.append( folder)
        gobject.idle_add( update_tooltip)
        if time.time() > last_sound_played + sound_timeout:
            if x11_session.get_idle() >= x11_idle_time:
                last_sound_played = time.time()
                wavefiles = glob.glob(os.path.expanduser(wavefile))
                os.system('play "%s" >/dev/null 2>&1 &' % wavefiles[int(random.random()*len(wavefiles))])
            else:
                print 'Session is not idle. Skipping sound playback.'
    else:
        print 'New mail in "%s", but ignored or already notified.' % ( folder, )

def newmail_read( folder):
    global notification, unread_folders

    for f in unread_folders:
        if f.endswith( folder):
            unread_folders.remove( f)
            break

    update_tooltip()
    if len(unread_folders) == 0:
        gobject.idle_add( update_tooltip, False)

bus.add_signal_receiver( newmail_received, signal_name = 'Newmail', dbus_interface = 'org.gnome.evolution.mail.dbus.Signal', path = '/org/gnome/evolution/mail/newmail')

bus.add_signal_receiver( newmail_read, signal_name = 'MessageReading', dbus_interface = 'org.gnome.evolution.mail.dbus.Signal', path = '/org/gnome/evolution/mail/newmail')

loop = gobject.MainLoop()
loop.run()

