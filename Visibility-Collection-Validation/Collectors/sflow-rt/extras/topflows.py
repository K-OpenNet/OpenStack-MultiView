#!/usr/bin/env python
import sys
import requests
import signal
import curses
import time
import math

def eng_str( x, format='%s', si=False):
  sign = ''
  if x < 0:
    x = -x
    sign = '-'
  exp = int( math.floor( math.log10( x)))
  exp3 = exp - ( exp % 3)
  x3 = x / float( 10 ** exp3)

  if si and exp3 >= -24 and exp3 <= 24 and exp3 != 0:
    exp3_text = 'yzafpnum KMGTPEZY'[ ( exp3 - (-24)) / 3]
  elif exp3 == 0:
    exp3_text = ''
  else:
    exp3_text = 'e%s' % exp3

  return ( '%s'+format+'%s') % ( sign, x3, exp3_text)

def endSession():
  curses.nocbreak(); stdscr.keypad(0); curses.echo()
  curses.endwin()

def sig_handler(signal,frame):
  endSession()
  exit(0)
signal.signal(signal.SIGINT, sig_handler)

if __name__ == '__main__':
    import optparse
    import json
    import os.path
    parser = optparse.OptionParser()
    parser.add_option("", "--flow",  dest="flow",  default="flows",
                      help="name of sFlow-RT flow defintion")
    parser.add_option("", "--server",  dest="server",  default="localhost",
                      help="name or IP address of sFlow-RT server")
    (options,args) = parser.parse_args()

specurl = 'http://' + options.server + ':8008/flow/' + options.flow + '/json'

r = requests.get(specurl)
if r.status_code != 200:
    print 'Cannot retrieve flow definition for ' + options.flow
    sys.exit(1)
spec = r.json()
keyfields = str(spec['keys']).split(',')
valuefield = str(spec['value'])
fieldsep = str(spec['fs'])

stdscr = curses.initscr()
curses.noecho()
curses.cbreak()
stdscr.keypad(1)
stdscr.nodelay(1)
pad = None

try:
  while True:

    ch = -1
    ch = stdscr.getch()
    if ch == ord('q'): break
    if ch == ord('Q'): break
    if ch == curses.KEY_RESIZE or pad is None:
      (maxY,maxX) = stdscr.getmaxyx()
      cw = maxX / (len(keyfields) + 1)
      flowsurl = 'http://' + options.server + ':8008/activeflows/ALL/' + options.flow + '/json?maxFlows=' + str(maxY - 2)
      pad = curses.newpad(maxY,maxX)

    # get latest flow data
    r = requests.get(flowsurl)
    if r.status_code != 200: break
    flows = r.json()
    if len(flows) == 0: continue

    # write headers
    pad.clear()
    for h in range(0,len(keyfields)):
        pad.addstr(0,h * cw,format(keyfields[h],"<"+str(cw)),curses.A_STANDOUT)
    pad.addstr(0,len(keyfields)*cw,format(valuefield,">"+str(cw)), curses.A_STANDOUT)

    # write rows
    for r in range(0, len(flows)):
      keys = flows[r]['key'].split(',')
      value = int(flows[r]['value'])
      if value == 0: continue

      for c in range(0,len(keys)):
        pad.addstr(1+r,c * cw,format(keys[c],"<"+str(cw)))
      # pad.addstr(1+r,len(keys)*cw,format(value,">"+str(cw)+".6g"))
      pad.addstr(1+r,len(keys)*cw,format(eng_str(value,"%.3f",True),">"+str(cw)))

    # sync to screen - may fail during resize
    try: pad.refresh(0,0,0,0,maxY,maxX)
    except: pass

    # sleep may be interrupted - e.g. during resize
    # so put this in a loop to make sure we don't
    # thrash and send too many requests
    wake = time.time() + 1.9
    while True:
      time.sleep(2)
      if time.time() >= wake: break
finally:
   endSession()

