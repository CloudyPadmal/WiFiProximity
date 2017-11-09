from Database import Database
from math import ceil, floor

class ParticleFilter:

	XYR = []
	XY = []
	X = []
	Y = []
	R = []
	F = 1

	def __init__(self):
		DB = Database()
		global XYR, XY, R, F, X, Y
		(XYR, XY, R, F, X, Y) = DB.getXYZCoordinates("18:64:72:56:5e:b4")

	def calculateGridPoints(self):
		global X, Y
		roundedMaxX = int(ceil(max(X) / 10.0)) * 10
		roundedMaxY = int(ceil(max(Y) / 10.0)) * 10
		roundedMinX = int(floor(min(X) / 10.0)) * 10
		roundedMinY = int(floor(min(Y) / 10.0)) * 10
		X_Points_In_Grid = [i for i in range(roundedMinX, roundedMaxX + 10, 10)]
		Y_Points_In_Grid = [i for i in range(roundedMinY, roundedMaxY + 10, 10)]
		return [X_Points_In_Grid, Y_Points_In_Grid]

	def generateSurroundingPoints(self, pointA, pointB):
		grid = []
		grid.append(pointA)
		grid.append([pointA[0] + 10, pointA[1]])
		grid.append(pointB)
		grid.append([pointB[0] - 10, pointB[1]])
		return grid

	def gridCoordinates(self):
		[Xs, Ys] = self.calculateGridPoints()
		GridPoints = []
		for X in Xs:
			for Y in Ys:
				GridPoints.append([X, Y])
		return GridPoints
