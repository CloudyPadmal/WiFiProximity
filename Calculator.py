#!/usr/bin/python
from math import log10, sin, cos, radians, pow, sqrt

class Calculator:

    def calculateDistance(self, RSSI):
        return pow(10 ,((27.55 - (20 * log10(2412)) - RSSI) / 20))
        #return pow(10 ,((36.58 - (20 * log10(2412)) - RSSI) / 20))

    def generateCircle(self, RSSI, x, y, z=100):

        radius = self.calculateDistance(RSSI)
        print radius
        X_Coordinates = []
        Y_Coordinates = []
        Z_Coordinates = []

        for theta in range(360):
            X_Coordinates.append(x + radius * sin(radians(theta)))
            Y_Coordinates.append(y + radius * cos(radians(theta)))
            Z_Coordinates.append(z)

        return X_Coordinates, Y_Coordinates, Z_Coordinates

    def calculateEuclideanDistance(self, pointA, pointB):
        dev_x = pow((pointA[0] - pointB[0]), 2)
        dev_y = pow((pointA[1] - pointB[1]), 2)
        return sqrt(dev_x + dev_y)
