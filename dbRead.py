import numpy as np
import MySQLdb as db
import matplotlib.pyplot as plt
from matplotlib import cm
from mpl_toolkits.mplot3d import Axes3D
import datetime
from math import log10, pow, sin, cos, radians
from Calculator import Calculator
from Database import Database
from Plotter import Plotter
from ParticleFilter import ParticleFilter

MACADD = "18:64:72:56:84:b3"

DB = Database()
CA = Calculator()
PL = Plotter()
PF = ParticleFilter()
PF.gridCoordinates()
"""
(X, Y, Z) = CA.generateCircle(RSSI=-70, x=1, y=2, z=1)
plt.scatter(X, Y, s=Z)
plt.show()
"""

def iteratePlotting():
    # List out all the MAC addresses
    SUTD_Staff_MACs = DB.viewMACs()
    # Read all wifi scan results from database
    (result_set1, result_set2, result_set3) = DB.generateResultSets()
    # Process three result sets
    for address in SUTD_Staff_MACs:
        if address != MACADD or address == MACADD:
            annotate = True
            ax1 = plt.subplot(221)
            ax1.set_title("Set 1")
            ax2 = plt.subplot(222, sharex=ax1, sharey=ax1)
            ax2.set_title("Set 2")
            ax3 = plt.subplot(223, sharex=ax1, sharey=ax1)
            ax3.set_title("Set 3")
            groundTruth = plt.subplot(224, sharex=ax1, sharey=ax1)

            (min_RSSI_index1, three_points1, three_points_RSSIs1) = PL.processResultSet(ax1, result_set1, address, annotate)
            (min_RSSI_index2, three_points2, three_points_RSSIs2) = PL.processResultSet(ax2, result_set2, address, annotate)
            (min_RSSI_index3, three_points3, three_points_RSSIs3) = PL.processResultSet(ax3, result_set3, address, annotate)

            #print (min_RSSI_index1, three_points1, three_points_RSSIs1)
            #print (min_RSSI_index2, three_points2, three_points_RSSIs2)
            #print (min_RSSI_index3, three_points3, three_points_RSSIs3)
            offset = PL.plotGroundTruth(groundTruth, result_set1, min_RSSI_index1, three_points1, three_points_RSSIs1)
            #PL.firstApproach(groundTruth, result_set1, min_RSSI_index1)
            
            plt.title(str(address) + " at a distance of " + str(offset) + " m")
            #plt.title(str(address))

            mng = plt.get_current_fig_manager()
            mng.full_screen_toggle()
            plt.show()

#iteratePlotting()


"""
First run viewMACs() to obtain MAC addresses of SUTD_Staff
Then select one of the MAC addresses and run plotWiFi(MAC, annotate)
"""