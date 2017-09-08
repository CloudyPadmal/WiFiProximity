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
POP_PC_DICT = "SELECT tango_time, pose FROM tango_point_cloud"
POP_WI_DICT = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1501741597600 AND 1501745362100"
#POP_WI_DIC1 = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1501743266045 AND 1501744128590" #1501743154005
#POP_WI_DIC2 = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1501744128595 AND 1501744564745"
#POP_WI_DIC3 = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1501744913890 AND 1501745362100"
POP_WI_DIC1 = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1504170033615 AND 1504171133610" #1504171143238
POP_WI_DIC2 = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1504171133615 AND 1504171864880"
POP_WI_DIC3 = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1504171864881 AND 1504172566224"
POP_WI_DICF = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1504170033615 AND 1504172566224"

#POP_WI_ANAY = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1504161566869 AND 1504172574241" # All data within 08-31
#POP_WI_ANAY = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1504169223063 AND 1504172574241" # Data set filtered from 05:00 PM success
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
    print("Fetching data ...")
    #cursor.execute(POP_WI_DICF) #2
    # tango_time [0], wifi_scan [1], pose [2]
    cursor.execute(PARAM)
    result = cursor.fetchall()
    for column in result:
    	pose = column[2].split(",")
    	"""
    	POSE[column[0]] = {'Tx':pose[0], 'Ty':pose[1], 'Tz':pose[2],
                           'Qx':pose[3], 'Qy':pose[4], 'Qz':pose[5],
                           'Qw':pose[6], 'Ux':pose[7], 'Uy':pose[8],
                           'Uz':pose[9], 'Rx':pose[10], 'Ry':pose[11],
                           'Rz':pose[12]}
		"""
        wifi_data = column[1].split(",")[1:]
        #print pose[7] + "\t" + pose[8] + "\t" + pose[9] + "\t" + column[0] + " -- " + datetime.datetime.fromtimestamp(long(column[0]) / 1000).strftime("%Y-%m-%d %H:%M:%S")
        """
        X_AXIS.append(np.float32(pose[7]))
        Y_AXIS.append(np.float32(pose[8]))
        Z_AXIS.append(np.float32(pose[9]))
        """
        #print wifi_data
        try:
            SUTD_Staff_index = wifi_data.index(MAC)
            #print pose[7] + "\t" + pose[8] + "\t" + pose[9] + "\t" + column[0] + " -- " + datetime.datetime.fromtimestamp(long(column[0]) / 1000).strftime("%Y-%m-%d %H:%M:%S")
            X_AXIS.append(np.float32(pose[7]))
            Y_AXIS.append(np.float32(pose[9]))
            Z_AXIS.append(np.float32(pose[8]))
            
            TIME.append(column[0])
            RSSI.append(np.float32(wifi_data[SUTD_Staff_index - 1]))
        except ValueError:
            continue

    XYZR = open("XYZRSSI_3", "w+")
    for X, Y, Z, R in zip(X_AXIS, Y_AXIS, Z_AXIS, RSSI):
    	line = str(X) + " " + str(Y) + " " + str(Z) + " " + str(R)
    	XYZR.write(line + "\n")
    XYZR.close()
    print("Completed ...")

def plotWiFi(MAC):
    # Generate WiFi scan data
    #populateWiFi("18:64:72:56:28:74")
    #populateWiFi("18:64:72:56:29:d4")
    """
    for time, R in zip(TIME, RSSI):
        output = time + " -- " + datetime.datetime.fromtimestamp(long(time) / 1000).strftime("%Y-%m-%d %H:%M:%S") + " -- " + str(R)
        print (output)
    """
    # Plot
    populateWiFi(MAC, POP_WI_SET1)
    newRSSI = []
    for i in RSSI:
    	newRSSI.append(i - min(RSSI))
    #fig = plt.figure()
    plt.subplot(3, 1, 1)
    plt.scatter(X_AXIS, Y_AXIS, c=newRSSI)
    for i, txt in enumerate(RSSI):
    	plt.annotate(txt, (X_AXIS[i], Y_AXIS[i]), fontsize=8)
    #ax = fig.gca(projection='3d')
    #ax.scatter(X_AXIS, Y_AXIS, c=newRSSI)
    #ax.plot_wireframe(X_AXIS, Y_AXIS, RSSI)
    plt.xlabel('X', fontsize=5)
    plt.ylabel('Y', fontsize=5)

    plt.subplot(3, 1, 2)
    plt.scatter(X_AXIS, Y_AXIS, c=newRSSI)
    for i, txt in enumerate(RSSI):
    	plt.annotate(txt, (X_AXIS[i], Y_AXIS[i]), fontsize=8)
    #ax = fig.gca(projection='3d')
    #ax.scatter(X_AXIS, Y_AXIS, c=newRSSI)
    #ax.plot_wireframe(X_AXIS, Y_AXIS, RSSI)
    plt.xlabel('X', fontsize=5)
    plt.ylabel('Y', fontsize=5)

    plt.subplot(3, 1, 3)
    plt.scatter(X_AXIS, Y_AXIS, c=newRSSI)
    for i, txt in enumerate(RSSI):
    	plt.annotate(txt, (X_AXIS[i], Y_AXIS[i]), fontsize=8)
    #ax = fig.gca(projection='3d')
    #ax.scatter(X_AXIS, Y_AXIS, c=newRSSI)
    #ax.plot_wireframe(X_AXIS, Y_AXIS, RSSI)
    plt.xlabel('X', fontsize=5)
    plt.ylabel('Y', fontsize=5)
    plt.show()

# Plot graphs
plotWiFi("18:64:72:56:4d:94")

# Close connection
c.close()
