#! /usr/bin/python

import serial
import time
import subprocess
import os

ser = None
serRapiro = None

str_rfcomm = "sudo rfcomm listen /dev/rfcomm0 22"
subprocess.Popen(str_rfcomm.split())

with open('demo_log.txt', 'w') as f:
    while 1:
        if ser is None:
            f.write("try spp connect\n")
            f.flush()
            try:
                subprocess.call("./auto_delete_rfcomm.sh", shell=True)
                port = "/dev/rfcomm0"
                baudrate = 115200
                ser = serial.Serial(port, baudrate)
                f.write("spp connect : OK!\n")
                f.flush()
            except serial.SerialException as e:
                f.write("bluetooth could not open serial port '{}':{}\n".format(port,e))
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
                f.write("rapiro could not open serial port '{}':{}\n".format(port,e))
                f.flush()
                time.sleep(1)
                continue
        time.sleep(1)
        try:
            if ser.inWaiting() > 0:
                f.write("Command process start\n")
                f.flush()
                cmd = ser.read(ser.inWaiting())
                if len(cmd) > 2:
                    f.write("over 3 byte read\n")
                    f.flush()
                    f.write("cmd1=" + cmd + "\n")
                    f.flush()
                    cmd = cmd[-3:]
                    f.write("cmd2=" + cmd + "\n")
                    f.flush()
                    if cmd[1] == "M":
                        cmd = cmd[-2:]
                f.write("cmd3=" + cmd + "\n")
                f.flush()
                if cmd == "M1":
                    f.write("Start Scenario M1\n")
                    f.flush()
                    serRapiro.write('#M0')
                    subprocess.call("kisyo_1.wav", shell=True)
                    time.sleep(9)
                elif cmd == "M2":
                    f.write("Start Scenario M2\n")
                    f.flush()
                    serRapiro.write('#M5')
                    subprocess.call("aplay Demo1_A_A_Min_3.wav", shell=True)
                    time.sleep(9)
                elif cmd == "M3":
                    f.write("Start Scenario M3\n")
                    f.flush()
                    serRapiro.write('#M1')
                    subprocess.call("aplay kisyo_1.wav", shell=True)
                    time.sleep(9)
                elif cmd == "M4":
                    f.write("Start Scenario M4\n")
                    f.flush()
                    #serRapiro.write('#M0')
                    #subprocess.call("aplay Demo1_A_C_Min_1.wav", shell=True)
                    subprocess.call("aplay kisyo_1.wav", shell=True)
                    time.sleep(9)
                elif cmd == "M5":
                    f.write("Start Scenario M5\n")
                    f.flush()
                    serRapiro.write('#M1')
                    subprocess.call("aplay kisyo_2.wav", shell=True)
                    time.sleep(9)
                elif cmd == "M6":
                    f.write("Start Scenario M6\n")
                    f.flush()
                    serRapiro.write('#M1')
                    subprocess.call("aplay good_1.wav", shell=True)
                    time.sleep(9)
                elif cmd == "M7":
                    f.write("Start Scenario M7\n")
                    f.flush()
                    serRapiro.write('#M2')
                    subprocess.call("aplay hot_1.wav", shell=True)
                    time.sleep(9)
                elif cmd == "M8":
                    f.write("Start Scenario M8\n")
                    f.flush()
                    serRapiro.write('#M2')
                    subprocess.call("aplay cold_1.wav", shell=True)
                    time.sleep(9)
                elif cmd == "M9":
                    f.write("Start Scenario M9\n")
                    f.flush()
                    serRapiro.write('#M2')
                    subprocess.call("aplay light_1.wav", shell=True)
                    time.sleep(9)
                elif cmd == "M10":
                    f.write("Start Scenario M10\n")
                    f.flush()
                    serRapiro.write('#M2')
                    subprocess.call("aplay dark_1.wav", shell=True)
                    time.sleep(9)
                elif cmd == "M11":
                    f.write("Start Scenario M11\n")
                    f.flush()
                    serRapiro.write('#M2')
                    subprocess.call("aplay humidity_high_1.wav", shell=True)
                    time.sleep(9)
                elif cmd == "M12":
                    f.write("Start Scenario M12\n")
                    f.flush()
                    serRapiro.write('#M2')
                    subprocess.call("aplay humidity_low_1.wav", shell=True)
                    time.sleep(9)
                else:
                    f.write("Unknown Command ->" + cmd + "\n")
                    f.flush()
            else:
                f.write("Command Waiting\n")
                f.write("Command process end\n")
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
