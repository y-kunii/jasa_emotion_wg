#! /usr/bin/python

import serial
import time
import subprocess
import os

ser = None
serRapiro = None

subprocess.call("sudo rfcomm listen /dev/rfcomm0 22 &", shell=True)
with open('demo_log.txt', 'w') as f:
    while 1:
        if ser is None:
            f.write("try spp connect\n")
            f.flush()
            try:
                port = "/dev/rfcomm0"
                baudrate = 115200
                ser = serial.Serial(port, baudrate)
                f.write("spp connect : OK!\n")
                f.flush()
            except serial.SerialException as e:
                f.write("could not open serial port '{}':{}\n".format(port,e))
                f.flush()
                time.sleep(1)
                continue
        if serRapiro is None:
            f.write("try RAPIRO connect\n")
            f.flush()
            try:
                serRapiro = serial.Serial('/dev/ttyS0', 57600)
                f.write("RAPIRO connect : OK!\n")
                f.flush()
            except serial.SerialException as e:
                f.write("could not open serial port '{}':{}\n".format(port,e))
                f.flush()
                time.sleep(1)
                continue
        time.sleep(1)
        try:
            if ser.inWaiting() > 0:
                cmd = ser.read(ser.inWaiting())
                if len(cmd) > 2:
                    f.write("over 2 byte read\n")
                    f.flush()
                    f.write("cmd=" + cmd + "\n")
                    #cmd = cmd[-2:]
                    cmd = cmd[:]
                f.write("cmd=" + cmd + "\n")
                f.flush()
                if cmd == "M1":
                    f.write("Start Scenario M1\n")
                    f.flush()
                    subprocess.call("aplay sleeping.wav", shell=True)
                elif cmd == "M2":
                    f.write("Start Scenario M2\n")
                    f.flush()
                    serRapiro.write('#M5')
                elif cmd == "M3":
                    f.write("Start Scenario M3\n")
                    f.flush()
                    serRapiro.write('#M1')
                    subprocess.call("aplay morning.wav", shell=True)
                elif cmd == "M4":
                    f.write("Start Scenario M4\n")
                    f.flush()
                    serRapiro.write('#M0')
                else:
                    pass
        except IOError as e:
            f.write("IOError Occured'{}':{}\n".format(port,e))
            f.flush()
            if ser:
                ser.close()
            ser = None
            f.write("Waiting connnection from Android\n")
            f.flush()
            subprocess.call("sudo rfcomm listen /dev/rfcomm0 22 &", shell=True)

ser.close()
