package com.smartcommunities.xdengue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MapView.LayoutParams;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.smartcommunities.xdengue.dataModel.CustomerData;
import com.smartcommunities.xdengue.net.SearchAddressTask;

public class myLocationActivity extends MapActivity {

	private MapView mapView;
	private EditText searchBox1;
	private MapController mapController;
	private MyLocationOverlay myLocOverlay;
	private List<Overlay> mapOverlays;
	final Context cont = this;
	final Activity currentActivity = this;
	private GeoPoint markerPosition;

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mylocation);
		
		searchBox1 = (EditText) findViewById(R.id.searchText1);
		
		searchBox1.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					performSearch();
		            return true;
		        }
		        return false;
		    }			
		});

		mapView = (MapView) findViewById(R.id.mapview1);
		mapView.setBuiltInZoomControls(true);
		LinearLayout zoomLayout = (LinearLayout) findViewById(R.id.zoomOption);
		View zoomView = mapView.getZoomControls();

		zoomLayout.addView(zoomView, new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		mapView.displayZoomControls(true);

		mapController = mapView.getController();

		mapOverlays = mapView.getOverlays();
		myLocOverlay = new MyLocationOverlay(this, mapView);
		myLocOverlay.enableMyLocation();
		mapOverlays.add(myLocOverlay);

		myLocOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				mapController.setZoom(17);
				mapController.animateTo(myLocOverlay.getMyLocation());
			}
		});
		
		Button centerButton = (Button) findViewById(R.id.centerButton);
		centerButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mapController.setZoom(17);
				mapController.animateTo(myLocOverlay.getMyLocation());
			}
		});
		
		Button newPlaceButton = (Button) findViewById(R.id.newPlaceButton);
		newPlaceButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent newPlaceIntent = new Intent(cont,	newPlaceActivity.class);
				newPlaceIntent.putExtra("latitude", markerPosition.getLatitudeE6());
				newPlaceIntent.putExtra("longitude", markerPosition.getLongitudeE6());
				startActivity(newPlaceIntent);
			}
		});

		CustomerData cd = null;
		try {
			cd = ((XdengueGlobalState) getApplication()).getCustomerData();
		} catch (Exception e) {
			e.printStackTrace();
			Log.v("cutomer data: ", e.getMessage());
		}

		for (int i = 0; i < cd.getOpenReports().size(); i++) {
			int severity = cd.getOpenReports().get(i).getSeverity();
			ArrayList<GeoPoint> affected = new ArrayList<GeoPoint>();
			for (int j = 0; j < cd.getOpenReports().get(i).getLocationContainer().getAreaLocation().getPoints().size(); j++) {
				affected.add(new GeoPoint((int) (cd.getOpenReports().get(i).getLocationContainer().getAreaLocation().getPoints().get(j).getLatitude() * 1E6),(int) (cd.getOpenReports().get(i).getLocationContainer().getAreaLocation().getPoints().get(j).getLongitude() * 1E6)));
			}
			GeoPoint centerPoint = new GeoPoint((int)(cd.getOpenReports().get(i).getLocationContainer().getAreaLocation().getAreaCenter().getLatitude() * 1E6),(int)(cd.getOpenReports().get(i).getLocationContainer().getAreaLocation().getAreaCenter().getLongitude() * 1E6));
			Bitmap bmp = null;
			if(severity==0){
				bmp = BitmapFactory.decodeResource(getResources(), R.drawable.mosquitoyellow);
			}
			else if(severity==2)
				bmp = BitmapFactory.decodeResource(getResources(), R.drawable.mosquitored);
			mapOverlays.add(new MapOverlay(affected,severity,bmp,centerPoint));
		}
	}

	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void performSearch() {
		CustomerData cd = null;
		try {
			cd = ((XdengueGlobalState) getApplication()).getCustomerData();
		} catch (Exception e) {
			e.printStackTrace();
			Log.v("cutomer data: ", e.getMessage());
		}
		String emailAddress = cd.getCustomer().getEmailAddress();
		String passwordString = XdenguePreferences.readString(cont, XdenguePreferences.PASS, "");
		String address = searchBox1.getText().toString();
		String maxRadius = "10";
		List<NameValuePair> params = new LinkedList<NameValuePair>();
		String url = "http://www.x-dengue.com/mobilev1/SearchAddress";
		params.add(new BasicNameValuePair("email", emailAddress));
		params.add(new BasicNameValuePair("password", passwordString));
		params.add(new BasicNameValuePair("address", address));
		params.add(new BasicNameValuePair("maxRadius", maxRadius));
		String paramString = URLEncodedUtils.format(params, "utf-8");
		url += "?" + paramString;
		SearchAddressTask searchTask = new SearchAddressTask(cont, currentActivity);
		searchTask.execute(url);
	}

}
