#@Author: Ethan Dickey
#This file creates the time delay plots for each simulation listed.
#  It creates one plot with all simulations (separate file does separate plots).
#Much of this is done manually due to the limited time given to create
# them.  Thus, it is not very extendible and may require some work for
# different naming conventions/output format.
#A better thing would be to merge this with the other script and set a
# flag for putting them all on one plot vs multiple

#python libs
import re
import time
import datetime
import sys

#matplotlib
from matplotlib import pyplot as plt  #plotting library

#turns on debugging print statements
DEBUG = False

z = 0
sims = []
#IDs have a "1" prepended to them to allow for all 3 meaningful digits to be printed (i.e. leading zeros)
#  (read paper for more information on meaning of each digit)
# sims.append((1000, "000"))
# sims.append((1001, "001"))
# sims.append((1010, "010"))
# sims.append((1011, "011"))
sims.append((1100, "100"))
sims.append((1101, "101"))
# sims.append((1110, "110"))
sims.append((1111, "111"))
x = []
y = []

#create the one figure for all plots
fig, ax = plt.subplots()

#retrieve data for each simulation listed above
for f in range(len(sims)):
    #open the target record file
    file = open("logger_b.log.LONG." + str(sims[f][0]))
    bundles = []
    for line in file: # or stdin or whatever, could redirect file into stdin
        if "[NetStats]" in line:
            #split the line with the relevant information into time
            # information (first part) and header/bundle info (second part)
            parts = line.strip().split("[NetStats]")

            #grab time information in the correct format
            timeinfo = parts[0].split(' ')
            currTimeSec = time.strptime(timeinfo[1].split('.')[0], '%H:%M:%S')
            currTimeMilli = int(timeinfo[1].split('.')[1]) / 1000
            currTime = currTimeSec[4] * 60 + currTimeSec[5] + currTimeMilli

            #grab the header and bundle info
            #sample line after "[NetStats]": "Bundle Received: from:b::to:a::creationTime:227462327::seqNum:0; Time (ms) since creation: 117; Size of bundle payload (bytes):88"
            results = re.split(';|:', parts[1]) #splits on all ":" or ";"
            header = results[0].split(' ') #results[0] == "Bundle Received" or "Bundle Arrived" (assumption)
            #bundle = results[1] #unsure what this is, wrote the code many months ago but this variable doesn't appear to be used

            #useful validation statements
            if DEBUG:
                print("  Header: " + header[1] + " " + header[2])
                print(int(results[13]))#results[13] is time since creation
            #if it is a bundle, otherwise ignore it
            if header[1] == "Bundle":
                #if it is a status report about a bundle arriving or being deleted/dropped in transport
                if header[2] == "Arrived" or header[2] == "Deleted":
                    if DEBUG: print("  Info: " + str(currTime) + " " + str(int(results[13])))
                    bundles.append((currTime, int(results[13])))#results[13] is time since creation

            #FOR A MORE ROBUST IMPLEMENTATION, COMPLETE THE FOLLOWING STEPS:
            #todo:: most importantly, save time since creation at BPAReceiver in an array
            #  (simply to make our lives easier when analyzing the log files).
            #todo:: grab which layer (ClientHandler (receiving), DTCP (sending), BPADispatcher (sending), BPAReceiver (receiving))
            #  and use that for targeted analytics.

    file.close()

    #plot Time From First Bundle Arrival (ms) vs Delay From Creation to End (ms)
    # arrays to hold information about 1 simulation
    xf = []
    yf = []
    min = 1000000000 #should probably be INF, in milliseconds
    bundles.sort() #bundles: [(block timestamp, time since creation)]
    for value in bundles:#find the first
        if DEBUG: print(value)
        timeMS = value[0] * 1000
        xf.append(timeMS)
        if timeMS < min:
            min = timeMS
        yf.append(value[1])
    for i in range(len(xf)):#shift all arrival times to the delta from the first received bundle
        #could've used a lambda, but whatever
        xf[i] -= min
    if DEBUG:
        print(xf)
        print(yf)
    x.append(xf)
    y.append(yf)

#plot all simulations at once (cleaner than doing them each iteration)
for f in range(len(sims)):
    ax.plot(x[f], y[f], label=sims[f][1])#plot with labels

#format plot
ax.set(xlabel='Time from first bundle arrival (ms)', ylabel='Delay from creation to end (ms)', title='Time vs Delay For High Density (No 110), Long Running')
ax.grid()
ax.legend()

#display plot
#plt.show()#uncomment if working with jupyter, etc.
fig.savefig("EndToEndLONGHighDensity_No110.png")#uncomment out this line to save to a file (recommended for command line)
# fig.savefig("EndToEndLONGLowDensity.png")#uncomment out this line to save to a file (recommended for command line)