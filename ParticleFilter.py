from Database import Database
from Plotter import Plotter
from Calculator import Calculator
from math import ceil, floor

class ParticleFilter:

	XYR = []	# [X, Y, R] of specific MAC Address
	XY = []		# [X, Y] of specific MAC Address
	X = []		# [X] of specific MAC Address 
	Y = []		# [Y] of specific MAC Address
	R = []		# [R] of specific MAC Address
	Alls = []	# All [X, Y, Z] coordinates from pose data
	F = 1

	def __init__(self):
		DB = Database()
		global XYR, XY, R, F, X, Y, Alls
		(XYR, XY, R, F, X, Y, Alls) = DB.getXYZCoordinates("18:64:72:56:5e:b4")

	# --------------------------------------------------------------------------------------------- Calculate min and max points of grid and grid points
	def calculateGridPoints(self):
		global X, Y
		roundedMaxX = int(ceil(max(X) / 10.0)) * 10
		roundedMaxY = int(ceil(max(Y) / 10.0)) * 10
		roundedMinX = int(floor(min(X) / 10.0)) * 10
		roundedMinY = int(floor(min(Y) / 10.0)) * 10
		X_Points_In_Grid = [i for i in range(roundedMinX, roundedMaxX + 10, 10)]
		Y_Points_In_Grid = [i for i in range(roundedMinY, roundedMaxY + 10, 10)]
		return X_Points_In_Grid, Y_Points_In_Grid, [roundedMinX, roundedMinY], [roundedMaxX, roundedMaxY]

	# --------------------------------------------------------------------------------------------- Calculate mid points in each grid
	def middlePointsInTheGrid(self):
		[X_Points_In_Grid, Y_Points_In_Grid, minimumPoint, maximumPoint] = self.calculateGridPoints()
		X_Coordinates = []
		Y_Coordinates = []
		Z_Coordinates = []
		XY_Coordinates = []
		XYZ_Coordinates = []
		minX = minimumPoint[0]
		minY = minimumPoint[1]
		maxX = maximumPoint[0]
		maxY = maximumPoint[1]
		for X in range(minX + 5, maxX + 5, 10):
			for Y in range(minY + 5, maxY + 5, 10):
				X_Coordinates.append(X)
				Y_Coordinates.append(Y)
				Z_Coordinates.append(50)
				XY_Coordinates.append([X, Y])
				XYZ_Coordinates.append([X, Y, 50])
		return X_Coordinates, Y_Coordinates, Z_Coordinates, XY_Coordinates, XYZ_Coordinates

	# --------------------------------------------------------------------------------------------- Calculate distance matrix from middle point to pose point
	def calculateDistanceFromCenterPointsToEachPose(self):
		Distance_Matrix = []
		(Xs, Ys, Zs, XYs, XYZs) = self.middlePointsInTheGrid()
		global XY
		C = Calculator()
		for pointA in XYs:
			GridPointDistance = []
			for pointB in XY:
				GridPointDistance.append(C.calculateEuclideanDistance(pointA, pointB))
			Distance_Matrix.append(GridPointDistance)
		return Distance_Matrix
	def calculateDistanceFromCenterPointsToEachPoseReturningXYs(self):
		Distance_Matrix = []
		(Xs, Ys, Zs, XYs, XYZs) = self.middlePointsInTheGrid()
		global XY
		C = Calculator()
		for pointA in XYs:
			GridPointDistance = []
			for pointB in XY:
				GridPointDistance.append(C.calculateEuclideanDistance(pointA, pointB))
			Distance_Matrix.append(GridPointDistance)
		return Distance_Matrix, Xs, Ys

	# --------------------------------------------------------------------------------------------- Generate the coefficient map
	def compareDistancesWithSignalStrength(self):
		(Distances, Xs, Ys) = self.calculateDistanceFromCenterPointsToEachPoseReturningXYs()
		"""
		[
		 [Grid0 to P1, Grid0 to P2, Grid0 to P3, ... , Grid0 to Pn]
		 [Grid1 to P1, Grid1 to P2, Grid1 to P3, ... , Grid1 to Pn]
		 ...
		 [Gridn to P1, Gridn to P2, Gridn to P3, ... , Gridn to Pn]
		]
		"""
		global R
		RSSIofEachPoint = R
		"""
		[
		 RSSI of P1, RSSI of P2, RSSI of P3, ... , RSSI of Pn
		]
		"""
		coefficientMatrix = []
		for gridPoint in Distances:
			coefficientMatrix.append(self.calculateSimilarityScoreOfAGridPoint(gridPoint, RSSIofEachPoint))
		PL = Plotter()
		#PL.printCompleteMapInColor([Xs, Ys, coefficientMatrix])
		maximumCoefficient = coefficientMatrix.index(max(coefficientMatrix))
		coefficientMatrix[maximumCoefficient] = 900
		Xs.extend(X)
		Ys.extend(Y)
		coefficientMatrix.extend(R)
		PL.printCompleteMapInColor([Xs, Ys, coefficientMatrix])


	def calculateSimilarityScoreOfAGridPoint(self, distanceMatrix, signalStrengths):
		TotalPoints = len(distanceMatrix)
		Similarities = 0
		for i in range(TotalPoints - 1):
			if (((distanceMatrix[i] - distanceMatrix[i+1]) * (signalStrengths[i] - signalStrengths[i+1])) > 0):
				Similarities = Similarities + 1
		return 50 * (1-(Similarities / float(TotalPoints)))

	def generateSurroundingPoints(self, pointA, pointB):
		grid = []
		grid.append(pointA)
		grid.append([pointA[0] + 10, pointA[1]])
		grid.append(pointB)
		grid.append([pointB[0] - 10, pointB[1]])
		return grid

	def gridCoordinates(self):
		[Xs, Ys, minimumPoint, maximumPoint] = self.calculateGridPoints()
		GridPoints = []
		X_ = []
		Y_ = []
		for X in Xs:
			for Y in Ys:
				GridPoints.append([X, Y])
				X_.append(X)
				Y_.append(Y)

		return GridPoints, X_, Y_

	def printAll(self):
		(G, X, Y) = self.gridCoordinates()
		(Xs, Ys, Zs, XYs, XYZs) = self.middlePointsInTheGrid()
		PL = Plotter()
		Z = [100 for i in range(len(Alls[0]))]
		Alls[0].extend(X)
		Alls[1].extend(Y)
		Alls[2].extend(Z)
		Alls[0].extend(Xs)
		Alls[1].extend(Ys)
		Alls[2].extend(Zs)
		PL.printCompleteMap(Alls)

