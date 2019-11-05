import serial
import time
import subprocess
import os

ser = None
serRapiro = None

def rapiro_connect():
    if serRapiro is None:
        try:
            serRapiro = serial.Serial('/dev/ttyS0', 57600)
        except serial.SerialException as e:
            #f.write("could not open serial port '{}':{}\n".format(port,e))
            #f.flush()
            print('Noooooooooooooo_1St')
            time.sleep(10)
    time.sleep(1)

def rapiro_com(cmd):
    if cmd == "M1":
        serRapiro.write('#M4')
        subprocess.call("aplay Demo1_A_A_Plus_2.wav", shell=True)
    elif cmd == "M2":
        serRapiro.write('#M9')
        subprocess.call("aplay Demo1_A_A_Min_3.wav", shell=True)
    elif cmd == "M3":
        serRapiro.write('#M1')
        subprocess.call("aplay Demo1_A_C_Plus_1.wav", shell=True)
    elif cmd == "M4":
        serRapiro.write('#M7')
        subprocess.call("aplay Demo1_A_C_Min_1.wav", shell=True)
    elif cmd == "M5":
        serRapiro.write('#M1')
        subprocess.call("aplay Demo1_C_A_Plus_2.wav", shell=True)
    elif cmd == "M6":
        serRapiro.write('#M9')
        subprocess.call("aplay Demo1_C_A_Min_1.wav", shell=True)
    elif cmd == "M7":
        serRapiro.write('#M7')
        subprocess.call("aplay Demo1_C_C_Plus_2.wav", shell=True)
    elif cmd == "M8":
        serRapiro.write('#M9')
        subprocess.call("aplay Demo1_C_A_Min_2.wav", shell=True)
    else:
        print("Nooooo!")
        time.sleep(10)

if __name__ == '__main__':
    serRapiro = serial.Serial('/dev/ttyS0', 57600)
    time.sleep(1)
    serRapiro.write(b'#M2')
    serRapiro.close()
        
