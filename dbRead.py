import numpy as np
import MySQLdb as db
import matplotlib.pyplot as plt
from matplotlib import cm
from mpl_toolkits.mplot3d import Axes3D
import datetime

"""
 Credentials
"""
host = '202.94.70.33'
username = 'root'
password = 'yuenchau'
database = 'tango'

# Useful queries
SHOW_TABLE = "SHOW TABLES"
SHOW_COLUMNS = "SHOW COLUMNS FROM "

SHOW_MACs = "SELECT wifi_scan FROM wifi_raw_data WHERE tango_time BETWEEN 1504169223063 AND 1504171137624"

POP_PC_DICT = "SELECT tango_time, pose FROM tango_point_cloud"
POP_WI_DICT = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1501741597600 AND 1501745362100"

POP_WI_SET1 = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1504169223063 AND 1504171137624" # Assumed 1st data set
POP_WI_SET2 = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1504171142366 AND 1504171864882" # Assumed 2nd data set
POP_WI_SET3 = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1504171868896 AND 1504172574241" # Assumed 3rd data set

TABLES = ['tango_imu_raw_data', 'tango_point_cloud', 'wifi_raw_data']
TANGO_PC = ['user_id', 'pose', 'tango_time', 'point_cloud', 'timestamp', 'id']
TANGO_IM = ['user_id', 'pose', 'tango_time', 'orientation', 'acceleration', 'gyroscope', 'magnetic_field', 'timestamp', 'id']
TANGO_WI = ['user_id', 'pose', 'tango_time', 'wifi_scan', 'timestamp', 'id']

POSE = {}
RSSI = []
TIME = []
X_AXIS = []
Y_AXIS = []
Z_AXIS = []
MACS = {}

# Open a connection
print("Creating connection ...")
c = db.connect(host, username, password, database)
print("Connection created ...")

def showTables():
    # Displays tables in "database"
    cursor = c.cursor()
    cursor.execute(SHOW_TABLE)
    result = cursor.fetchall()
    TABLE = []
    for row in result:
        TABLE.append(row[0])
    print(TABLE)

def populateTable(index):
    # Populate Table
    cursor = c.cursor()
    cursor.execute(SHOW_COLUMNS + TABLES[index])
    result = cursor.fetchall()
    ROW = []
    for column in result:
        ROW.append(column[0])
    print (ROW)

def populateWiFi(MAC, PARAM):
    # Populate Pose data
    cursor = c.cursor()
    cursor.execute(PARAM)
    result = cursor.fetchall()
    for column in result:
    	pose = column[2].split(",")
        wifi_data = column[1].split(",")[1:]
        try:
            SUTD_Staff_index = wifi_data.index(MAC)
            X_AXIS.append(np.float32(pose[7]))
            Y_AXIS.append(np.float32(pose[9]))
            Z_AXIS.append(np.float32(pose[8]))
            TIME.append(column[0])
            RSSI.append(np.float32(wifi_data[SUTD_Staff_index - 1]))
        except ValueError:
            continue

def viewMACs():
	cursor = c.cursor()
	cursor.execute(SHOW_MACs)
	result = cursor.fetchall()
	MAC_index = 3;
	SSI_index = 0;
	OFFSET = 4
	for column in result:
		wifi_data = column[0].split(",")[1:]
		for i in range(len(wifi_data) / 4):
			MACS[wifi_data[MAC_index]] = wifi_data[SSI_index]
			MAC_index = MAC_index + OFFSET
			SSI_index = SSI_index + OFFSET
		MAC_index = 3;
		SSI_index = 0;

	for key in MACS.keys():
		print MACS[key] + "\t\t\t\t" + key

def plotWiFi(MAC, annotate): 
    # Plot
    f, (ax1, ax2, ax3) = plt.subplots(3, sharex=True, sharey=True)

    # Populate first set
    populateWiFi(MAC, POP_WI_SET1)
    newRSSI = []
    for i in RSSI:
    	newRSSI.append(i - min(RSSI))
    ax1.scatter(X_AXIS, Y_AXIS, c=newRSSI)
    if annotate:
    	for i, txt in enumerate(RSSI):
    		ax1.annotate(txt, (X_AXIS[i], Y_AXIS[i]), fontsize=6)
    del X_AXIS[:]
    del Y_AXIS[:]
    del RSSI[:]

	# Populate second set
    populateWiFi(MAC, POP_WI_SET2)
    newRSSI = []
    for i in RSSI:
    	newRSSI.append(i - min(RSSI))
    ax2.scatter(X_AXIS, Y_AXIS, c=newRSSI)
    if annotate:
    	for i, txt in enumerate(RSSI):
    		ax2.annotate(txt, (X_AXIS[i], Y_AXIS[i]), fontsize=6)
    del X_AXIS[:]
    del Y_AXIS[:]
    del RSSI[:]

	# Populate third set
    populateWiFi(MAC, POP_WI_SET3)
    newRSSI = []
    for i in RSSI:
    	newRSSI.append(i - min(RSSI))
    ax3.scatter(X_AXIS, Y_AXIS, c=newRSSI)
    if annotate:
    	for i, txt in enumerate(RSSI):
    		ax2.annotate(txt, (X_AXIS[i], Y_AXIS[i]), fontsize=6)
    f.subplots_adjust(hspace=0)
    plt.show()

# Plot graphs
plotWiFi("18:64:72:56:38:74", False)
#viewMACs()

# Close connection
c.close()
