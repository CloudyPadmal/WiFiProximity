import matplotlib.pyplot as plt
import numpy as np
from math import ceil, floor

from Calculator import Calculator
from Plotter import Plotter
from Database import Database
from ParticleFilter import ParticleFilter

SELECTED_MACs = ["18:64:72:56:84:b3", "18:64:72:56:38:a4", "18:64:72:56:5e:b4", "18:64:72:56:52:34", "18:64:72:56:5f:74",
"18:64:72:56:5e:a4", "18:64:72:56:45:14", "18:64:72:56:84:a3", "18:64:72:56:5d:24", "18:64:72:56:6b:c4", "18:64:72:56:3a:d3",
"18:64:72:56:5d:34", "18:64:72:56:6b:d4", "18:64:72:56:45:04"]

def fetchDataSetsRecorded():
	DB = Database()
	return DB.generateResultSets()

def fetchAllMACAddresses():
	DB = Database()
	return DB.viewMACs()

def extractDataRelatedToMACAddress(MAC, DataSet):
	X_Coordinates = [] # -- All X coordinates
	x_Coordinates = [] # -- MAC X coordinates
	Y_Coordinates = [] # -- All Y coordinates
	y_Coordinates = [] # -- All Y coordinates
	R_Coordinates = [] # -- All RSSI Values
	r_Coordinates = [] # -- MAC RSSI Values

	for column in DataSet:
		# Pose data set
		POSE = column[2].split(",")
		# WiFi scan results containing all MAC scan results
		WIFI = column[1].split(",")[1:]
		# Extract all pose data and specific pose data both
		try:
			X_Coordinates.append(np.float32(POSE[7]))
			Y_Coordinates.append(np.float32(POSE[9]))
			R_Coordinates.append(np.float32(-100))
			MAC_Index = WIFI.index(MAC) # This will throw a value error if the MAC is not in the list
			x_Coordinates.append(np.float32(POSE[7]))
			y_Coordinates.append(np.float32(POSE[9]))
			r_Coordinates.append(np.float32(WIFI[MAC_Index - 1]))
		except ValueError:
			continue

	return (X_Coordinates, x_Coordinates, Y_Coordinates, y_Coordinates, R_Coordinates, r_Coordinates)

def calculateMinimumAndMaximumXYCoordinates(Xs, Ys):
	roundedMaxX = int(ceil(max(Xs) / 10.0)) * 10
	roundedMaxY = int(ceil(max(Ys) / 10.0)) * 10
	roundedMinX = int(floor(min(Xs) / 10.0)) * 10
	roundedMinY = int(floor(min(Ys) / 10.0)) * 10
	return roundedMinX, roundedMinY, roundedMaxX, roundedMaxY

def calculateMidPointsInGrid(minX, minY, maxX, maxY, factor):
	X_Mid_Points = []
	Y_Mid_Points = []
	for X in range(minX - 25, maxX + 25, factor):
			for Y in range(minY - 25, maxY + 25, factor):
				X_Mid_Points.append(X)
				Y_Mid_Points.append(Y)
	return X_Mid_Points, Y_Mid_Points

def calculateDistanceMatrix(X_Mid_Points, Y_Mid_Points, Pose_X, Pose_Y):
	Distance_Matrix = []
	C = Calculator()
	for X, Y in zip(X_Mid_Points, Y_Mid_Points):
		DistanceFromOneGridToAllPoints = []
		for x, y in zip(Pose_X, Pose_Y):
			DistanceFromOneGridToAllPoints.append(C.calculateEuclideanDistance([X, Y], [x, y]))
		Distance_Matrix.append(DistanceFromOneGridToAllPoints)
	return Distance_Matrix

def calculateSimilarityScoreOfAPoint(Distance_Matrix, RSSI_Values):
	TotalPoints = len(Distance_Matrix)
	Similarities = 0
	for i in range(TotalPoints - 1):
		if (((Distance_Matrix[i] - Distance_Matrix[i+1]) * (RSSI_Values[i] - RSSI_Values[i+1])) > 0):
			Similarities = Similarities + 1
	try:
		score = 10 * (1 - (Similarities / float(TotalPoints)))
	except ZeroDivisionError:
		score = 0
	return score

def calculateCoefficients(Distance_Matrix, RSSI_Values):
	Coefficients = []
	for gridPoint in Distance_Matrix:
		Coefficients.append(calculateSimilarityScoreOfAPoint(gridPoint, RSSI_Values))
	return Coefficients

def scatterSubPlot(plot, Xs, Ys, Zs):
	plot.scatter(Xs, Ys, c=Zs)

def StartProcess():
	print("Process started ...")
	# Step 1
	DataSets = fetchDataSetsRecorded()
	# Step 2
	#MAC_Addresses = fetchAllMACAddresses()
	MAC_Addresses = SELECTED_MACs
	print(len(MAC_Addresses)),
	print("MAC Addresses found ...")
	# Process all the MAC Addresses in the vicinity
	PL = Plotter()
	print("Ready to plot ...")
	for eachMAC in MAC_Addresses:
		print("MAC"),
		print(MAC_Addresses.index(eachMAC))
		Max_Coefficient_Indices = [0, 0, 0]
		
		set1 = plt.subplot(221)
		set1.set_title("Data Set 1")
		set2 = plt.subplot(222, sharex=set1, sharey=set1)
		set2.set_title("Data Set 2")
		set3 = plt.subplot(223, sharex=set1, sharey=set1)
		set3.set_title("Data Set 3")
		set4 = plt.subplot(224, sharex=set1, sharey=set1)
		set4.set_title("Ground Truth for " + eachMAC + " (Grid Size : 2x2)")
		Sub_Plots = [set1, set2, set3, set4]

		Big_X = []
		Big_Y = []
		Big_Z = []

		for iteration in range(len(DataSets)):
			(X_, x_, Y_, y_, R_, r_) = extractDataRelatedToMACAddress(eachMAC, DataSets[iteration])
			if (iteration == 0):
				Big_X = X_
				Big_Y = Y_
				Big_Z = R_
			(minX, minY, maxX, maxY) = calculateMinimumAndMaximumXYCoordinates(X_, Y_)
			(X_Mid_Points, Y_Mid_Points) = calculateMidPointsInGrid(minX, minY, maxX, maxY, 10)
			Distances = calculateDistanceMatrix(X_Mid_Points, Y_Mid_Points, x_, y_)
			Coefficients = calculateCoefficients(Distances, r_)
			# Calculate index for maximum similarity
			Max_Coefficient_Indices[iteration] = Coefficients.index(max(Coefficients))
			# Create sub plot for all 3 data sets and ground truth
			if (iteration == 2):
				x_.append(X_Mid_Points[Max_Coefficient_Indices[iteration]])
				y_.append(Y_Mid_Points[Max_Coefficient_Indices[iteration]])
				r_.append(-150)
				scatterSubPlot(Sub_Plots[iteration], x_, y_, r_)
				Big_X.extend([X_Mid_Points[Max_Coefficient_Indices[0]], X_Mid_Points[Max_Coefficient_Indices[1]], X_Mid_Points[Max_Coefficient_Indices[2]]])
				Big_Y.extend([Y_Mid_Points[Max_Coefficient_Indices[0]], Y_Mid_Points[Max_Coefficient_Indices[1]], Y_Mid_Points[Max_Coefficient_Indices[2]]])
				Big_Z.extend([-150, -150, -150])
				Mean_X = (X_Mid_Points[Max_Coefficient_Indices[0]] + X_Mid_Points[Max_Coefficient_Indices[1]] + X_Mid_Points[Max_Coefficient_Indices[2]]) / 3
				Mean_Y = (Y_Mid_Points[Max_Coefficient_Indices[0]] + Y_Mid_Points[Max_Coefficient_Indices[1]] + Y_Mid_Points[Max_Coefficient_Indices[2]]) / 3
				Big_X.append(Mean_X)
				Big_Y.append(Mean_Y)
				Big_Z.append(-200)
				scatterSubPlot(Sub_Plots[iteration + 1], Big_X, Big_Y, Big_Z)
			else:
				x_.append(X_Mid_Points[Max_Coefficient_Indices[iteration]])
				y_.append(Y_Mid_Points[Max_Coefficient_Indices[iteration]])
				r_.append(-150)
				scatterSubPlot(Sub_Plots[iteration], x_, y_, r_)

		print(Max_Coefficient_Indices)
		mng = plt.get_current_fig_manager()
		mng.full_screen_toggle()
		plt.show()

	print("Complete !!!")

# Let's do this!!
StartProcess()