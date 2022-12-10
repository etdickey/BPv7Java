import re
import time
import datetime
import sys


z = 0
sims = []
sims.append((1000, "000"))
sims.append((1001, "001"))
sims.append((1010, "010"))
sims.append((1011, "011"))
sims.append((1100, "100"))
sims.append((1101, "101"))
sims.append((1110, "110"))
sims.append((1111, "111"))
x = []
y = []
from matplotlib import pyplot as plt  #plotting library

for f in range(len(sims)):
    fig, ax = plt.subplots()
    file = open("logger_b.log.LONG." + str(sims[f][0]))
    bundles = []
    for line in file: # or file or whatever, could redirect file into stdin
        if "[NetStats]" in line:
            #if z < 100:
            #    print("Found [NetStats]")
            #    z += 1
            #else:
            #    exit(1)
            parts = line.strip().split("[NetStats]")
            timeinfo = parts[0].split(' ')
            currTimeSec = time.strptime(timeinfo[1].split('.')[0], '%H:%M:%S')
            currTimeMilli = int(timeinfo[1].split('.')[1]) / 1000
            currTime = currTimeSec[4] * 60 + currTimeSec[5] + currTimeMilli
            results = re.split(';|:', parts[1])
            header = results[0].split(' ')
            bundle = results[1]
            #print("  Header: " + header[1] + " " + header[2])
            #print(int(results[13]))
            if header[1] == "Bundle": 
                if header[2] == "Arrived" or header[2] == "Deleted":
        #            print("  Info: " + str(currTime) + " " + str(int(results[13])))
                    bundles.append((currTime, int(results[13])))
            #todo:: split line, grab the time since creation
            #todo:: grab which layer (ClientHandler (receiving), DTCP (sending), BPADispatcher (sending), BPAReceiver (receiving))
            #todo:: most importantly, save time since creation at BPAReceiver in an array

        #todo: plot using matplotlib
        
    xf = []
    yf = []
    min = 1000000000
    bundles.sort()
    for value in bundles:
        #print(value)
        xf.append(value[0] * 1000)
        if value[0] * 1000 < min:
            min = value[0] * 1000
        yf.append(value[1])
    for i in range(len(xf)):
        xf[i] -= min
    #print(xf)
    #print(yf)
    x.append(xf)
    y.append(yf)
    #print(x)
    #print(y)
    file.close()
    ax.plot(xf, yf, label=sims[f][1])#format plot
    ax.set(xlabel='Time from first bundle arrival (ms)', ylabel='Delay from creation to end (ms)', title='Time vs Delay, Long Running For Scenario ' + sims[f][1])
    ax.grid()
    ax.legend()
    #show plot
    #plt.show()
    fig.savefig("EndToEndLONG" + sims[f][1] + ".png")#uncomment out this line to save to a file (recommended for command line)
