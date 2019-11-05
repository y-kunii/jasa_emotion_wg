#! /usr/bin/python

import serial
import time
import subprocess
import os

try:
    port = "/dev/rfcomm0"
    baudrate = 115200
    ser = serial.Serial(port, baudrate)
    # serRapiro = serial.Serial('/dev/ttyS0', 57600)

except serial.SerialException as e:
    print("could not open serial port '{}':{}".format(port,e))

while 1:
    time.sleep(1)
    if ser.inWaiting() > 0:
        cmd = ser.read(ser.inWaiting())
        print cmd
        if cmd == "M1":
            print "Start Scenario M1"
            # serRapiro.write('#M1')
            # subprocess.call("aplay demo01_01.wav", shell=True)
        elif cmd == "M2":
            print "Start Scenario M2"
            # serRapiro.write('#M5')
            # subprocess.call("aplay demo01_02.wav", shell=True)
        elif cmd == "M3":
            print "Start Scenario M3"
            # serRapiro.write('#M3')
            # subprocess.call("aplay demo01_03.wav", shell=True)
        else:
            pass

ser.close()
