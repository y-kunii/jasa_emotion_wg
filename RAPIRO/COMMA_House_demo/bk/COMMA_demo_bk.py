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
                    cmd = cmd[-2:]
                f.write("cmd=" + cmd + "\n")
                f.flush()
                if cmd == "M1":
                    #serRapiro.write('#M5')
                    f.write("Start Scenario M1\n")
                    f.flush()
                    subprocess.call("aplay Demo1_A_A_Min_1.wav", shell=True)
                elif cmd == "M2":
                    f.write("Start Scenario M2\n")
                    f.flush()
                    subprocess.call("aplay Demo1_A_A_Min_2.wav", shell=True)
                elif cmd == "M3":
                    f.write("Start Scenario M3\n")
                    f.flush()
                    subprocess.call("aplay Demo1_A_A_Min_3.wav", shell=True)
                elif cmd == "M4":
                    f.write("Start Scenario M4\n")
                    f.flush()
                    subprocess.call("aplay Demo1_A_A_Plus_1.wav", shell=True)
                elif cmd == "M5":
                    f.write("Start Scenario M5\n")
                    f.flush()
                    subprocess.call("aplay Demo1_A_A_Plus_2.wav", shell=True)
                elif cmd == "M6":
                    f.write("Start Scenario M6\n")
                    f.flush()
                    subprocess.call("aplay Demo1_A_A_Plus_3.wav", shell=True)
                elif cmd == "M7":
                    f.write("Start Scenario M7\n")
                    f.flush()
                    subprocess.call("aplay Demo1_A_C_Min_1.wav", shell=True)
                elif cmd == "M8":
                    f.write("Start Scenario M8\n")
                    f.flush()
                    subprocess.call("aplay Demo1_A_C_Min_2.wav", shell=True)
                elif cmd == "M9":
                    f.write("Start Scenario M9\n")
                    f.flush()
                    subprocess.call("aplay Demo1_A_C_Plus_1.wav", shell=True)
                elif cmd == "M10":
                    f.write("Start Scenario M10\n")
                    f.flush()
                    subprocess.call("aplay Demo1_A_C_Plus_2.wav", shell=True)
                elif cmd == "M11":
                    f.write("Start Scenario M11\n")
                    f.flush()
                    subprocess.call("aplay Demo1_C_A_Min_1.wav", shell=True)
                elif cmd == "M12":
                    f.write("Start Scenario M12\n")
                    f.flush()
                    subprocess.call("aplay Demo1_C_A_Min_2.wav", shell=True)
                elif cmd == "M13":
                    f.write("Start Scenario M13\n")
                    f.flush()
                    subprocess.call("aplay Demo1_C_A_Plus_1.wav", shell=True)
                elif cmd == "M14":
                    f.write("Start Scenario M14\n")
                    f.flush()
                    subprocess.call("aplay Demo1_C_A_Plus_2.wav", shell=True)
                elif cmd == "M15":
                    f.write("Start Scenario M15\n")
                    f.flush()
                    subprocess.call("aplay Demo1_C_C_Min_1.wav", shell=True)
                elif cmd == "M16":
                    f.write("Start Scenario M16\n")
                    f.flush()
                    subprocess.call("aplay Demo1_C_C_Min_2.wav", shell=True)
                elif cmd == "M17":
                    f.write("Start Scenario M17\n")
                    f.flush()
                    subprocess.call("aplay Demo1_C_C_Min_3.wav", shell=True)
                elif cmd == "M18":
                    f.write("Start Scenario M18\n")
                    f.flush()
                    subprocess.call("aplay Demo1_C_C_Plus_1.wav", shell=True)
                elif cmd == "M19":
                    f.write("Start Scenario M19\n")
                    f.flush()
                    subprocess.call("aplay Demo1_C_C_Plus_2.wav", shell=True)
                else:
                    f.write("Unknown Command ->" + cmd + "\n")
                    f.flush()
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
