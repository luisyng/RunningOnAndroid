package running.android.app;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import running.android.domain.Circuit;

import running.android.app.R;
import running.domain.Athlete;
import running.domain.Competition;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsoluteLayout;
import android.widget.RelativeLayout;
import android.widget.ArrayAdapter;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class CircuitActivity extends MapActivity {

	// Domain
	private Competition competition;
	private Circuit circuit;
	private List<String> athleteNames;
	private List<Integer> athleteNameIDs;
	
	// Application
	private WebService webService;
	private CompetitionTabActivity parent;
	private CircuitActivityBroadcastReceiver broadcastReceiver;
	private IntentFilter intentFilter;
	private ArrayAdapter<String> adapter;
	private AbsoluteLayout layout;
	
	// Maps
	private MapView mapView;
	private MapController mapController;
	private AthletesOverlay athletesOverlay;

	// Flags and vars
	private boolean isCircuitPainted;
	private int lastCompetitionState;
	private int idBlockedAthlete;
	private boolean chronoShown;
	
	// Final
	private static final int BLOCK_ID = 5;
	private static final int UNBLOCK_ID = 6;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.circuit);

		// Get the parent
		parent = (CompetitionTabActivity) getParent();
		
		// Don't block any athletes
		this.idBlockedAthlete = -1;
		
		// Get the service and the domain from it
		if(parent.isConnectedToService()) {
			this.webService = parent.getWebService();
			this.competition = webService.getCompetition();
			this.lastCompetitionState = competition.getState();
			this.circuit = webService.getCircuit();
			createBlockDialog();
		}
		
		// Get the views
		mapView = (MapView) findViewById(R.id.map);
		layout = (AbsoluteLayout) findViewById(R.id.layout);
		
		// Initialize the map
		initMapView();
		
		// The circuit is not already painted
		this.isCircuitPainted = false;
		this.chronoShown = false;
		
		// Set the broadcast receiver
		this.broadcastReceiver = new CircuitActivityBroadcastReceiver();
		this.intentFilter = new IntentFilter();
		this.intentFilter.addAction(WebService.CONNECTION_ESTABLISHED);
		this.intentFilter.addAction(WebService.CIRCUIT_DOWNLOADED);
		this.intentFilter.addAction(WebService.DOWNLOAD_SYNC);	
		this.intentFilter.addAction(WebService.HAS_STARTED);
		this.intentFilter.addAction(WebService.HAS_ENDED);
	}
	
	@Override
	public void onResume() {
		super.onResume();			
		// If the circuit is already donwloaded and it's not already painted
		if (!isCircuitPainted && circuit != null) {
			paintCircuit();
			if(competition.getState() == Competition.TAKING_PLACE) {
				Log.i("chrono", "chrono a");
				showChrono();
			}
		}
		if(isCircuitPainted && lastCompetitionState == Competition.NOT_STARTED &&
				competition.getState() == Competition.TAKING_PLACE) {
			mapView.getOverlays().add(athletesOverlay);	
			Log.i("chrono", "chrono b");
			showChrono();
		}
		else if(isCircuitPainted && lastCompetitionState == Competition.TAKING_PLACE &&
				competition.getState() == Competition.ENDED) {
			removeChrono();
		}
		if (webService != null) {
			createBlockDialog();
		}
		
		// Otherwise, the broadcast receiver will paint it when ready
		registerReceiver(this.broadcastReceiver, this.intentFilter);
	}
	
	@Override 
	public void onPause() {
		super.onPause();
		unregisterReceiver(this.broadcastReceiver);
	}

	private void createBlockDialog() {
		// Adapter for the dialog that asks whether block the screen
		athleteNames = new ArrayList<String>();
		athleteNameIDs = new ArrayList<Integer>();
		athleteNames.add("No centrar");
		athleteNameIDs.add(-1);
		this.webService.getAthletesMutex().startToReadOrModify();
		for(Athlete ath: webService.getAthletes()) {
			athleteNames.add(ath.getFirstName() + " " + ath.getLastName());
			athleteNameIDs.add(ath.getId());
		}
		this.webService.getAthletesMutex().endReadingOrModifying();
		this.adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, athleteNames) {	
		};
	}
	
	/** 
	 * Find and initialize the map view. 
	 */
	private void initMapView() {
		mapView = (MapView) findViewById(R.id.map);
		mapController = mapView.getController();
		mapView.setSatellite(true);
		mapView.setStreetView(false);
		mapView.setTraffic(false);
		mapView.setBuiltInZoomControls(true);
		MyLocationOverlay overlay = new MyLocationOverlay(this, mapView);
		overlay.enableMyLocation();
		mapView.getOverlays().add(overlay);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false; // Required by MapActivity
	}

	private void paintCircuit() {
		Log.i("EVENT_UI", "Paint the circuit");
		
		// Draw the circuit
		CircuitOverlay circuitOverlay = new CircuitOverlay(circuit.getCircuitPoints(), getResources());
		mapView.getOverlays().add(circuitOverlay);
		
		// Draw the athletes only if the competition is taking place
		athletesOverlay = new AthletesOverlay(webService);
		if (competition.getState() == Competition.TAKING_PLACE) {
			mapView.getOverlays().add(athletesOverlay);
		}
		
		// Create the overlay
		Drawable drawable = getResources().getDrawable(R.drawable.comment);
	    MyItemizedOverlay commentsOverlay = new MyItemizedOverlay(drawable, mapView);
	    webService.setCommentsOverlay(commentsOverlay);
	    mapView.getOverlays().add(commentsOverlay);
	    
	    // Draw the comments
	    webService.getCommentsMutex().startToReadOrModify();
	    commentsOverlay.addComments(webService.getComments());
	    webService.getCommentsMutex().endReadingOrModifying();
	    
		// Center the map
		mapController.animateTo(circuit.getCenter());
		mapController.zoomToSpan(circuit.getLatSpanE6(),circuit.getLonSpanE6());
		
		// Update isCircuitPainted
		this.isCircuitPainted = true;
	}

	private void updateAthletesInCircuit() {
		Log.i("EVENT_UI", "Update the athletes in circuit");
		mapView.invalidate();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		if(idBlockedAthlete == -1) {
			menu.add(Menu.NONE, BLOCK_ID, Menu.NONE, R.string.block);
		}
		else {
			menu.add(Menu.NONE, UNBLOCK_ID, Menu.NONE, R.string.unblock);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case (BLOCK_ID): {
			new AlertDialog.Builder(this)
	        .setTitle(getString(R.string.block_dialog))
	        .setAdapter(adapter, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialoginterface,
	                  int i) {
	            	idBlockedAthlete = athleteNameIDs.get(i);               
	            }
	         })
	        .show();
			return true;
		}
		}
		return false;
	}
	
	private void showChrono() {
		if(!chronoShown) {
			layout.addView(webService.getChrono(), 
					new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, 
							AbsoluteLayout.LayoutParams.WRAP_CONTENT, 150, 284));
			chronoShown = true;
		}
	}
	
	private void removeChrono() {
		if(chronoShown) {
			layout.removeView(webService.getChrono());
			chronoShown = false;
		}
	}
	
	/**
	 * Receiver to listen to updates
	 */
	private class CircuitActivityBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {		
			if(webService == null) {
				webService = parent.getWebService();
				competition = webService.getCompetition();
				circuit = webService.getCircuit();
				createBlockDialog();
			}
			
			if (intent.getAction().equals(WebService.CIRCUIT_DOWNLOADED)) {			
				circuit = webService.getCircuit();
				paintCircuit();
			}
			
			else if (intent.getAction().equals(WebService.DOWNLOAD_SYNC)) {
				updateAthletesInCircuit();
				if(idBlockedAthlete != -1) {
					webService.getAthletesMutex().startToReadOrModify();
					for(Athlete ath: webService.getAthletes()) {
						if(ath.getId() == idBlockedAthlete) {
							GeoPoint center = circuit.getCircuitPoints().get(ath.getDistanceFromStart());
							mapController.animateTo(center);
							break;
						}
					}
					webService.getAthletesMutex().endReadingOrModifying();
				}
			}
			
			else if (intent.getAction().equals(WebService.HAS_STARTED)) {
				mapView.getOverlays().add(athletesOverlay);
				lastCompetitionState = Competition.TAKING_PLACE;
				
				// Show the chrono
				showChrono();
			}
			else if (intent.getAction().equals(WebService.HAS_ENDED)) {
				// Stop showing the chrono
				removeChrono();
			}
		}
	}
}
