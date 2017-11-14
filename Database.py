#!/usr/bin/python
import MySQLdb as db
import numpy as np

class Database:

    TABLES_TANGO = ['cell_tower_data', 'tango_imu_raw_data', 'tango_point_cloud', 'wifi_raw_data']
    TABLES_IMU = ['imu_converted', 'imu_mobile', 'imu_mobile_heading_steps', 'imu_rawdata', 'imu_rawdata_temp']

    TANGO_PC = ['user_id', 'pose', 'tango_time', 'point_cloud', 'timestamp', 'id']
    TANGO_IM = ['user_id', 'pose', 'tango_time', 'orientation', 'acceleration', 'gyroscope', 'magnetic_field', 'timestamp', 'id']
    TANGO_WI = ['user_id', 'pose', 'tango_time', 'wifi_scan', 'timestamp', 'id']

    ROWS_IN_imu_mobile_heading_steps = ['map_id', 'user_id', 'steps', 'heading', 'timestamp', 'id']

    host = '202.94.70.33'
    username = 'root'
    password = 'yuenchau'
    database = 'tango'
    CURRENT_TIME = 1508911403692

    def initiate(self, username=username, password=password, host=host, database=database):
        c = db.connect(host, username, password, database)
        return c

    def showDatabases(self):
        """
        Displays all the databases in the server
        """
        c = self.initiate()
        cursor = c.cursor()
        cursor.execute("SHOW DATABASES")
        result = cursor.fetchall()
        DBs = []
        for row in result:
            DBs.append(row[0])
        print(DBs)
        c.close()

    def showTables(self):
        """
        Displays tables in the selected table;
        In 'tango' --> ['cell_tower_data', 'tango_imu_raw_data', 'tango_point_cloud', 'wifi_raw_data']
        """
        c = self.initiate()
        cursor = c.cursor()
        cursor.execute("SHOW TABLES")
        result = cursor.fetchall()
        TABLE = []
        for row in result:
            TABLE.append(row[0])
        print(TABLE)
        c.close()

    def captureStartEnd(self):
        """
        Prints time stamp and pose in the table and clause defined by the SELECT_CLAUSE
        """
        c = self.initiate()
        cursor = c.cursor()
        SELECT_CLAUSE = "SELECT tango_time, pose FROM wifi_raw_data WHERE tango_time > " + str(1508911403692)
        cursor.execute(SELECT_CLAUSE)
        result = cursor.fetchall()        
        c.close()
        for column in result:
            if column[1] == '0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5,0.5':
                print str(column[0]) + " end" 
            elif column[1] == '0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1':
                print str(column[0]) + " start"

    def showPoseAndTime(self):
        c = self.initiate()
        cursor = c.cursor()
        SHOW_POSE = "SELECT pose, tango_time FROM wifi_raw_data"
        cursor.execute(SHOW_POSE)
        result = cursor.fetchall()        
        c.close()
        for column in result:
            print column

    def viewMACs(self):
        c = self.initiate()
        cursor = c.cursor()
        SHOW_MACs = "SELECT wifi_scan FROM wifi_raw_data WHERE tango_time > " + str(1508911403692)
        cursor.execute(SHOW_MACs)
        result = cursor.fetchall()
        c.close()
        MAC_index = 3
        SSI_index = 0
        OFFSET = 4
        MACS = {}
        
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

    def showColumnsInWiFi(self):
        c = self.initiate()
        cursor = c.cursor()
        cursor.execute("SELECT wifi_scan FROM wifi_raw_data WHERE tango_time > " + str(1508911403692))
        """
        cursor.execute("SELECT wifi_scan FROM wifi_raw_data WHERE tango_time > " + str(1508911403692))
        # N
        # SUTD_Guest
        # 5240
        # -80
        # 18:64:72:56:3a:b5
        # ...
        """
        result = cursor.fetchall()
        TABLE = []
        for row in result:
            TABLE.append(row[0])
        #print(TABLE)
        c.close()

    def printTableContent(self):
        """
        Prints all the content in the table and clause defined by the SELECT_CLAUSE
        """
        c = self.initiate()
        cursor = c.cursor()
        SELECT_CLAUSE = "SELECT tango_time, pose, wifi_scan FROM wifi_raw_data WHERE tango_time > " + str(1508911403692)
        cursor.execute(SELECT_CLAUSE)
        result = cursor.fetchall()
        c.close()
        for column in result:
            print column

    def populateWiFi(self, MAC, PARAM):
        # Populate Pose data
        c = self.initiate()
        cursor = c.cursor()
        cursor.execute(PARAM)
        result = cursor.fetchall()
        c.close()
        X_AXIS = []
        Y_AXIS = []
        Z_AXIS = []
        TIME = []
        RSSI = []
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
        return X_AXIS, Y_AXIS, Z_AXIS, TIME, RSSI

    def generateResultSets(self):
        # Read all wifi scan results from database
        c = self.initiate()
        cursor = c.cursor()
        POP_WI_SET1 = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1508911403693 AND 1508911870731"
        POP_WI_SET2 = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1510554636779 AND 1510555206600"
        POP_WI_SET3 = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1510556500867 AND 1510557034437"
        # Create three cursors with three data set
        cursor.execute(POP_WI_SET1)
        result_set1 = cursor.fetchall()
        cursor.execute(POP_WI_SET2)
        result_set2 = cursor.fetchall()
        cursor.execute(POP_WI_SET3)
        result_set3 = cursor.fetchall()
        c.close()
        return result_set1, result_set2, result_set3

    ##############################################################################################################################################################################

    def getXYZCoordinates(self, MAC):
        #PARAM = "SELECT wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1508911403693 AND 1508911870731"
        #PARAM = "SELECT wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1510554636779 AND 1510555206600"
        PARAM = "SELECT wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1510556500867 AND 1510557034437"
        # Populate Pose data
        c = self.initiate()
        cursor = c.cursor()
        cursor.execute(PARAM)
        result = cursor.fetchall()
        c.close()
        XYR = []
        XY = []
        X = []
        AllX = []
        AllY = []
        AllZ = []
        Y = []
        R = []
        for column in result:
            pose = column[1].split(",")
            wifi_data = column[0].split(",")[1:]
            try:
                AllX.append(np.float32(pose[7]))
                AllY.append(np.float32(pose[9]))
                AllZ.append(100)
                SUTD_Staff_index = wifi_data.index(MAC)
                XYR.append([np.float32(pose[7]), np.float32(pose[9]), np.float32(wifi_data[SUTD_Staff_index - 1])])
                XY.append([np.float32(pose[7]), np.float32(pose[9])])
                X.append(np.float32(pose[7]))
                Y.append(np.float32(pose[9]))
                R.append(np.float32(wifi_data[SUTD_Staff_index - 1]))
                F = np.float32(wifi_data[SUTD_Staff_index - 2])
            except ValueError:
                continue
        return XYR, XY, R, F, X, Y, [AllX, AllY, AllZ]