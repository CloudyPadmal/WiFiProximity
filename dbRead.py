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
#database = 'IMU'
CURRENT_TIME = 1508911403692

# Useful queries
SHOW_COLUMNS = "SHOW COLUMNS FROM "

SHOW_POSE = "SELECT pose, tango_time FROM wifi_raw_data"

POP_PC_DICT = "SELECT tango_time, pose FROM tango_point_cloud"
POP_WI_DICT = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1501741597600 AND 1501745362100"

"""
POP_WI_SET1 = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1504172574241 AND 1504172674241" # Assumed 1st data set
POP_WI_SET2 = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1504172674261 AND 1504172874261" # Assumed 2nd data set
POP_WI_SET3 = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1504172874241 AND 1504171074261" # Assumed 3rd data set
"""
TABLES_TANGO = ['cell_tower_data', 'tango_imu_raw_data', 'tango_point_cloud', 'wifi_raw_data']
TABLES_IMU = ['imu_converted', 'imu_mobile', 'imu_mobile_heading_steps', 'imu_rawdata', 'imu_rawdata_temp']

TANGO_PC = ['user_id', 'pose', 'tango_time', 'point_cloud', 'timestamp', 'id']
TANGO_IM = ['user_id', 'pose', 'tango_time', 'orientation', 'acceleration', 'gyroscope', 'magnetic_field', 'timestamp', 'id']
TANGO_WI = ['user_id', 'pose', 'tango_time', 'wifi_scan', 'timestamp', 'id']

ROWS_IN_imu_mobile_heading_steps = ['map_id', 'user_id', 'steps', 'heading', 'timestamp', 'id']

POSE = {}
RSSI = []
TIME = []
X_AXIS = []
Y_AXIS = []
Z_AXIS = []
MACS = {}

MACADD = "18:64:72:56:85:b3"#raw_input("MAC : ")
# Open a connection
print("Creating connection ...")
c = db.connect(host, username, password, database)
print("Connection created ...")

def showDatabases():
    """
    Displays all the databases in the server --> Result is the following;
    ['information_schema', 'IMU', 'PozyxInformation', 'beacontrack', 'bluetoothtrack', 'fmtrack', 'gpstrack', 
    'kobuki_robot', 'mysql', 'nus_library', 'performance_schema', 'psatrack', 'sport_hub', 'tango', 'wifitrack']
    """
    cursor = c.cursor()
    cursor.execute("SHOW DATABASES")
    result = cursor.fetchall()
    DBs = []
    for row in result:
        DBs.append(row[0])
    print(DBs)

def showTables():
    """
    Displays tables in the selected table;
    In 'tango' --> ['cell_tower_data', 'tango_imu_raw_data', 'tango_point_cloud', 'wifi_raw_data']
    """
    cursor = c.cursor()
    cursor.execute("SHOW TABLES")
    result = cursor.fetchall()
    TABLE = []
    for row in result:
        TABLE.append(row[0])
    print(TABLE)

def populateTable(index):
    """
    Displays column names in the specific table;
    In 'wifi_raw_data' --> ['user_id', 'pose', 'tango_time', 'wifi_scan', 'timestamp', 'id']
    """
    cursor = c.cursor()
    cursor.execute(SHOW_COLUMNS + TABLES_TANGO[index])
    result = cursor.fetchall()
    ROW = []
    for column in result:
        ROW.append(column[0])
    print (ROW)

def printTableContent():
    """
    Prints all the content in the table and clause defined by the SELECT_CLAUSE
    """
    cursor = c.cursor()
    SELECT_CLAUSE = "SELECT tango_time, pose, wifi_scan FROM wifi_raw_data WHERE tango_time > " + str(CURRENT_TIME)
    cursor.execute(SELECT_CLAUSE)
    result = cursor.fetchall()
    for column in result:
        print column

def captureStartEnd():
    """
    Prints time stamp and pose in the table and clause defined by the SELECT_CLAUSE
    """
    cursor = c.cursor()
    SELECT_CLAUSE = "SELECT tango_time, pose FROM wifi_raw_data WHERE tango_time > " + str(1508911403692)
    cursor.execute(SELECT_CLAUSE)
    result = cursor.fetchall()
    for column in result:
        if column[1] == '0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5':
            print str(column[0]) + " end" 
        elif column[1] == '0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1':
            print str(column[0]) + " start" 

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
    SHOW_MACs = "SELECT wifi_scan FROM wifi_raw_data WHERE tango_time > " + str(CURRENT_TIME)
    cursor.execute(SHOW_MACs)
    result = cursor.fetchall()
    MAC_index = 3
    SSI_index = 0
    OFFSET = 4
    for column in result:
        wifi_data = column[0].split(",")[1:]
        for i in range(len(wifi_data) / 4):
            MACS[wifi_data[MAC_index]] = wifi_data[SSI_index]
            MAC_index = MAC_index + OFFSET
            SSI_index = SSI_index + OFFSET
        MAC_index = 3
        SSI_index = 0

    MACss = []
    for key in MACS.keys():
        if MACS[key] == "SUTD_Staff":
            MACss.append(key)
    return MACss

def plotWiFi(MAC, annotate): 
    # Plot
    f, (ax1, ax2, ax3) = plt.subplots(3, sharex=True, sharey=True)

    POP_WI_SET1 = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1508911403693 AND 1508911870731"
    POP_WI_SET2 = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1508911870732 AND yy"
    POP_WI_SET3 = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN yy AND zz"

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
    populateWiFi(MAC, POP_WI_SET1)
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
    populateWiFi(MAC, POP_WI_SET1)
    newRSSI = []
    for i in RSSI:
    	newRSSI.append(i - min(RSSI))
    ax3.scatter(X_AXIS, Y_AXIS, c=newRSSI)
    if annotate:
    	for i, txt in enumerate(RSSI):
    		ax3.annotate(txt, (X_AXIS[i], Y_AXIS[i]), fontsize=6)
    f.subplots_adjust(hspace=0)
    plt.show()

def showPoseAndTime():
	cursor = c.cursor()
	cursor.execute(SHOW_POSE)
	result = cursor.fetchall()
	for column in result:
		print column

def iteratePlotting():
    # List out all the MAC addresses
    SUTD_Staff_MACs = viewMACs()
    # Read all wifi scan results from database
    cursor = c.cursor()
    POP_WI_SET1 = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1508911403693 AND 1508911870731"
    POP_WI_SET2 = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1508911870732 AND yy"
    POP_WI_SET3 = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN yy AND zz"
    # Create three cursors with three data set
    cursor.execute(POP_WI_SET1)
    result_set1 = cursor.fetchall()
    cursor.execute(POP_WI_SET1)
    result_set2 = cursor.fetchall()
    cursor.execute(POP_WI_SET1)
    result_set3 = cursor.fetchall()
    # Process three result sets
    for address in SUTD_Staff_MACs:
        annotate = True
        ax1 = plt.subplot(221)
        ax1.set_title("Set 1")
        ax2 = plt.subplot(222, sharex=ax1, sharey=ax1)
        ax2.set_title("Set 2")
        ax3 = plt.subplot(223, sharex=ax1, sharey=ax1)
        ax3.set_title("Set 3")
        groundTruth = plt.subplot(224, sharex=ax1, sharey=ax1)
        plt.title(str(address))

        Set_X = []
        Set_Y = []
        Set_Z = []
        Set_T = []
        Set_R = []

        # Sub plot set 1
        for column in result_set1:
            pose = column[2].split(",")
            wifi_data = column[1].split(",")[1:]
            try:
                SUTD_Staff_index = wifi_data.index(address)
                Set_X.append(np.float32(pose[7]))
                Set_Y.append(np.float32(pose[9]))
                Set_Z.append(np.float32(pose[8]))
                Set_T.append(column[0])
                Set_R.append(np.float32(wifi_data[SUTD_Staff_index - 1]))
            except ValueError:
                continue
        newRSSI_1 = []
        for i in Set_R:
            newRSSI_1.append(i - min(Set_R))
        ax1.scatter(Set_X, Set_Y, c=newRSSI_1) # Interchanging y and z
        if annotate:
            for i, txt in enumerate(Set_R):
                ax1.annotate(txt, (Set_X[i], Set_Y[i]), fontsize=6)
        del Set_X[:]
        del Set_Y[:]
        del Set_Z[:]
        del Set_T[:]
        del Set_R[:]

        # Sub plot set 2
        for column in result_set2:
            pose = column[2].split(",")
            wifi_data = column[1].split(",")[1:]
            try:
                SUTD_Staff_index = wifi_data.index(address)
                Set_X.append(np.float32(pose[7]))
                Set_Y.append(np.float32(pose[9]))
                Set_Z.append(np.float32(pose[8]))
                Set_T.append(column[0])
                Set_R.append(np.float32(wifi_data[SUTD_Staff_index - 1]))
            except ValueError:
                continue
        newRSSI_2 = []
        for i in Set_R:
            newRSSI_2.append(i - min(Set_R))
        ax2.scatter(Set_X, Set_Y, c=newRSSI_2)
        if annotate:
            for i, txt in enumerate(Set_R):
                ax2.annotate(txt, (Set_X[i], Set_Y[i]), fontsize=6)
        del Set_X[:]
        del Set_Y[:]
        del Set_Z[:]
        del Set_T[:]
        del Set_R[:]

        # Sub plot set 3
        for column in result_set3:
            pose = column[2].split(",")
            wifi_data = column[1].split(",")[1:]
            try:
                SUTD_Staff_index = wifi_data.index(address)
                Set_X.append(np.float32(pose[7]))
                Set_Y.append(np.float32(pose[9]))
                Set_Z.append(np.float32(pose[8]))
                Set_T.append(column[0])
                Set_R.append(np.float32(wifi_data[SUTD_Staff_index - 1]))
            except ValueError:
                continue
        newRSSI_3 = []
        for i in Set_R:
            newRSSI_3.append(i - min(Set_R))
        ax3.scatter(Set_X, Set_Y, c=newRSSI_3)
        if annotate:
            for i, txt in enumerate(Set_R):
                ax3.annotate(txt, (Set_X[i], Set_Y[i]), fontsize=6)
        del Set_X[:]
        del Set_Y[:]
        del Set_Z[:]
        del Set_T[:]
        del Set_R[:]

        X_Axis = []
        Y_Axis = []
        Z_Axis = []
        # Ground Truth
        for column in result_set1:
            pose = column[2].split(",")
            try:
                X_Axis.append(np.float32(pose[7]))
                Y_Axis.append(np.float32(pose[9]))
                Z_Axis.append(np.float32(pose[8]))
            except ValueError:
                continue
        newZ = []
        for i in Z_Axis:
            newZ.append(i - min(Z_Axis))
        groundTruth.scatter(X_Axis, Y_Axis, c=newZ)

        mng = plt.get_current_fig_manager()
        mng.full_screen_toggle()
        plt.show()

def plotPose():
    cursor = c.cursor()
    POP_POSE = "SELECT tango_time, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1508911403693 AND 1508911870731"
    # Create three cursors with three data set
    cursor.execute(POP_POSE)
    result_set = cursor.fetchall()
    X_Axis = []
    Y_Axis = []
    Z_Axis = []
    # Sub plot set 1
    for column in result_set:
        pose = column[1].split(",")
        try:
            X_Axis.append(np.float32(pose[7]))
            Y_Axis.append(np.float32(pose[9]))
            Z_Axis.append(np.float32(pose[8]))
        except ValueError:
            continue
    newRSSI_1 = []
    for i in Z_Axis:
        newRSSI_1.append(i - min(Z_Axis))
    plt.scatter(X_Axis, Y_Axis, c=newRSSI_1)
    plt.show()


# Plot graphs
#plotWiFi("18:64:72:56:38:74", True)
#plotWiFi("18:64:72:56:25:14", True)
#plotWiFi("18:64:72:56:26:d4", True)
#plotWiFi("18:64:72:56:28:74", True)
#plotWiFi("18:64:72:56:26:c4", True)
#viewMACs()
#plotWiFi(MACADD, True);
#showPoseAndTime()

#0 to view times
#captureStartEnd()
#1
#viewMACs()
#2
#plotWiFi(MACADD, True)

#plotPose()
iteratePlotting()

#printTableContent()
#showTables()
#populateTable(3)
#showDatabases()

# Close connection
c.close()

"""
First run viewMACs() to obtain MAC addresses of SUTD_Staff
Then select one of the MAC addresses and run plotWiFi(MAC, annotate)
"""