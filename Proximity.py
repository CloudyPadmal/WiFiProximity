class Proximity:

	def calculateDistance(self, RSSI):
    	return pow(10 ,((27.55 - (20 * log10(2412)) - RSSI) / 20))