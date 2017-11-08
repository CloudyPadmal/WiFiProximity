class Calculator:

    def generateCircle(self, RSSI, m, n):

        radius = calculateDistance(RSSI)
        X_Coordinates = []
        Y_Coordinates = []

        for theta in range(360):
            X_Coordinates.append(m + radius * radians(sin(theta)))
            Y_Coordinates.append(n + radius * radians(cos(theta)))

        return X_Coordinates, Y_Coordinates

    def calculateDistance(RSSI):
        return pow(10 ,((27.55 - (20 * log10(2412)) - RSSI) / 20))