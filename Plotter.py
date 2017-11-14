#!/usr/bin/python

import matplotlib.pyplot as plt
from Database import Database
import numpy as np
from Calculator import Calculator

class Plotter:

    def getMinimumZList(self, RSSI):
        newRSSI = []
        minRSSI = min(RSSI)
        
        for i in RSSI:
            newRSSI.append(i - minRSSI)

        return newRSSI

    def plotWiFi(self, MAC, annotate):
        # Plot
        f, (ax1, ax2, ax3) = plt.subplots(3, sharex=True, sharey=True)

        POP_WI_SET1 = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1508911403693 AND 1508911870731"
        POP_WI_SET2 = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN 1508911870732 AND yy"
        POP_WI_SET3 = "SELECT tango_time, wifi_scan, pose FROM wifi_raw_data WHERE tango_time BETWEEN yy AND zz"

        # Populate first set
        DB = Database()
        
        (X_AXIS, Y_AXIS, Z_AXIS, TIME, RSSI) = DB.populateWiFi(MAC, POP_WI_SET1)
        newRSSI = self.getMinimumZList(RSSI)

        ax1.scatter(X_AXIS, Y_AXIS, c=newRSSI)
        if annotate:
            for i, txt in enumerate(RSSI):
                ax1.annotate(txt, (X_AXIS[i], Y_AXIS[i]), fontsize=6)

        # Populate second set
        (X_AXIS, Y_AXIS, Z_AXIS, TIME, RSSI) = DB.populateWiFi(MAC, POP_WI_SET1)
        newRSSI = self.getMinimumZList(RSSI)

        ax2.scatter(X_AXIS, Y_AXIS, c=newRSSI)
        if annotate:
            for i, txt in enumerate(RSSI):
                ax2.annotate(txt, (X_AXIS[i], Y_AXIS[i]), fontsize=6)

        # Populate third set
        (X_AXIS, Y_AXIS, Z_AXIS, TIME, RSSI) = DB.populateWiFi(MAC, POP_WI_SET1)
        newRSSI = self.getMinimumZList(RSSI)

        ax3.scatter(X_AXIS, Y_AXIS, c=newRSSI)
        if annotate:
            for i, txt in enumerate(RSSI):
                ax3.annotate(txt, (X_AXIS[i], Y_AXIS[i]), fontsize=6)
        f.subplots_adjust(hspace=0)
        plt.show()

    def processResultSet(self, ax, result_set, address, annotate):

        Set_X = []
        Set_Y = []
        Set_Z = []
        Set_T = []
        Set_R = []
        set_r = []
        Set_L = []

        for column in result_set:
            pose = column[2].split(",")
            wifi_data = column[1].split(",")[1:]
            try:
                SUTD_Staff_index = wifi_data.index(address)
                Set_X.append(np.float32(pose[7]))
                Set_Y.append(np.float32(pose[9]))
                Set_Z.append(np.float32(pose[8]))
                Set_T.append(column[0])
                Set_R.append(np.float32(wifi_data[SUTD_Staff_index - 1]))
                set_r.append(np.float32(wifi_data[SUTD_Staff_index - 1]))
            except ValueError:
                set_r.append(np.float32(-100))
        
        Set_L = self.getMinimumZList(Set_R)

        ax.scatter(Set_X, Set_Y, c=Set_L)
        if annotate:
            for i, txt in enumerate(Set_R):
                ax.annotate(txt, (Set_X[i], Set_Y[i]), fontsize=6)

        min_RSSI_index = set_r.index(max(Set_R))
        print len(Set_R)

        min_rssi_index = Set_R.index(max(Set_R))
        
        """
        A = min_rssi_index - 2
        B = min_rssi_index
        C = min_rssi_index + 2
        """
        A = int((len(Set_R) / 6))
        B = int(len(Set_R) / 2)
        C = int((len(Set_R) * 5 / 6))

        three_points = [[Set_X[A], Set_Y[A]], [Set_X[B], Set_Y[B]], [Set_X[C], Set_Y[C]]]
        three_points_RSSIs = [Set_R[A], Set_R[B], Set_R[C]]
        return min_RSSI_index, three_points, three_points_RSSIs

    def plotGroundTruth(self, groundTruth, result_set, _x, points, pointRSSIs):
        X_Axis = []
        Y_Axis = []
        Z_Axis = []
        L_Axis = []
        C = Calculator()
        # Ground Truth
        for column in result_set:
            pose = column[2].split(",")
            try:
                X_Axis.append(np.float32(pose[7]))
                Y_Axis.append(np.float32(pose[9]))
                Z_Axis.append(np.float32(pose[8]))
            except ValueError:
                continue
        L_Axis = self.getMinimumZList(Z_Axis)

        L_Axis[_x] = 100
        minimumRSSIPoint = [X_Axis[_x], Y_Axis[_x]]

        (C1X, C1Y, C1Z) = C.generateCircle(pointRSSIs[0], points[0][0], points[0][1])
        (C2X, C2Y, C2Z) = C.generateCircle(pointRSSIs[1], points[1][0], points[1][1])
        (C3X, C3Y, C3Z) = C.generateCircle(pointRSSIs[2], points[2][0], points[2][1])
        X_Axis.extend(C1X)
        X_Axis.extend(C2X)
        X_Axis.extend(C3X)
        Y_Axis.extend(C1Y)
        Y_Axis.extend(C2Y)
        Y_Axis.extend(C3Y)
        L_Axis.extend(C1Z)
        L_Axis.extend(C2Z)
        L_Axis.extend(C3Z)

        Points = self.analyzeCircles(C1X, C1Y, C2X, C2Y, C3X, C3Y, minimumRSSIPoint)
        X_Axis.extend(Points[0])
        Y_Axis.extend(Points[1])
        L_Axis.extend(Points[2])
        # Access Point Location
        X_Axis.append(Points[3][0])
        Y_Axis.append(Points[3][1])
        L_Axis.append(300)

        
        groundTruth.scatter(X_Axis, Y_Axis, c=L_Axis)

        return Points[3][2]

    def firstApproach(self, groundTruth, result_set, _x):
        X_Axis = []
        Y_Axis = []
        Z_Axis = []
        L_Axis = []
        C = Calculator()
        # Ground Truth
        for column in result_set:
            pose = column[2].split(",")
            try:
                X_Axis.append(np.float32(pose[7]))
                Y_Axis.append(np.float32(pose[9]))
                Z_Axis.append(np.float32(pose[8]))
            except ValueError:
                continue
        L_Axis = self.getMinimumZList(Z_Axis)

        L_Axis[_x] = 100
        minimumRSSIPoint = [X_Axis[_x], Y_Axis[_x]]
        
        groundTruth.scatter(X_Axis, Y_Axis, c=L_Axis)


    def analyzeCircles(self, C1X, C1Y, C2X, C2Y, C3X, C3Y, minRSSIPoint):
        C = Calculator()
        nearSet1X = []
        nearSet1Y = []
        nearSet1Z = []
        nearSet2X = []
        nearSet2Y = []
        nearSet2Z = []
        distances = []
        d = []
        for x1, y1 in zip(C1X, C1Y):
            for x2, y2 in zip(C2X, C2Y):
                if (abs(x1 - x2) < 0.25) and (abs(y1 - y2) < 0.25):
                    nearSet1X.append(x1)
                    nearSet1Y.append(y1)
                    nearSet1Z.append(200)
                    distance = C.calculateEuclideanDistance(minRSSIPoint, [x1, y1])
                    distances.append([x1, y1, distance])
                    d.append(distance)

        for x1, y1 in zip(C1X, C1Y):
            for x3, y3 in zip(C3X, C3Y):
                if (abs(x1 - x3) < 0.25) and (abs(y1 - y3) < 0.25):
                    nearSet2X.append(x3)
                    nearSet2Y.append(y3)
                    nearSet2Z.append(200)
                    distance = C.calculateEuclideanDistance(minRSSIPoint, [x3, y3])
                    distances.append([x3, y3, distance])
                    d.append(distance)

        nearSet1X.extend(nearSet2X)
        nearSet1Y.extend(nearSet2Y)
        nearSet1Z.extend(nearSet2Z)

        try:
            minDistanceIndex = d.index(min(d))
            APCoordinates = distances[minDistanceIndex]
        except ValueError:
            APCoordinates = [0, 0, 0]

        return [nearSet1X, nearSet1Y, nearSet1Z, APCoordinates]

    def printCompleteMap(self, XYZs):
        plt.scatter(XYZs[0], XYZs[1], s=XYZs[2])
        plt.show()

    def printCompleteMapInColor(self, XYZs):
        plt.scatter(XYZs[0], XYZs[1], c=XYZs[2], alpha=0.5)
        for i, txt in enumerate(XYZs[2]):
            plt.annotate(txt, (XYZs[0][i], XYZs[1][i]), fontsize=6)
        plt.show()