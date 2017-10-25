package sutd.guo.com.indoorlocation.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;


public class GpsScan {


	private Location location = null;

	private LocationManager locationManager = null;

	private Context context = null;

	public GpsScan(Context ctx) {
		context = ctx;
		locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		location = locationManager.getLastKnownLocation(getProvider());
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
					1000, 8, locationListener);
	}

	private String getProvider() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(true);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(false);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		return locationManager.getBestProvider(criteria, true);
	}

	private LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location l) {
			if (l != null) {
				location = l;
			}
		}

		public void onProviderDisabled(String provider) {
			location = null;
		}

		public void onProviderEnabled(String provider) {

			Location l = locationManager.getLastKnownLocation(provider);
			if (l != null) {
				location = l;
			}
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

	};

	public Location getLocation() {
		return location;
	}


}
