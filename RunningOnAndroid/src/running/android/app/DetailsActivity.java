package running.android.app;


import java.text.SimpleDateFormat;

import running.android.app.R;
import running.domain.Category;
import running.domain.Competition;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;

public class DetailsActivity extends Activity {
	
	// Domain
	private Competition competition;
	
	// Application
	private CompetitionTabActivity parent; 
	private DetailsActivityBroadcastReceiver broadcastReceiver;
	private IntentFilter intentFilter;
	private TextView registeredNumberTextView;
	
	// State
	private boolean hasFirstPainted;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details);
		
		// Parent
		parent = (CompetitionTabActivity) getParent();
		if (parent.isConnectedToService()) {
			competition = parent.getWebService().getCompetition();
			setAllTheViews();
		}
		
		// Text view of the number of athletes
		registeredNumberTextView = (TextView) findViewById(R.id.registeredAthletesNumber);
		hasFirstPainted = false;
		
		// Set the broadcast receiver
		this.broadcastReceiver = new DetailsActivityBroadcastReceiver();
		this.intentFilter = new IntentFilter();
		this.intentFilter.addAction(WebService.CONNECTION_ESTABLISHED);
		this.intentFilter.addAction(WebService.ATHLETES_DOWNLOADED);
	}
	
	@Override
	public void onResume() {
		super.onResume();			
		registerReceiver(this.broadcastReceiver, this.intentFilter);
		if(hasFirstPainted) {
			setRegisteredAthletesNumber();
		}
		else if(parent.isConnectedToService()){
			competition = parent.getWebService().getCompetition();
			setAllTheViews();
		}
	}
	
	@Override 
	public void onPause() {
		super.onPause();
		unregisterReceiver(this.broadcastReceiver);
	}

	private void setAllTheViews() {

		// Inflate the view: get the views
		TextView nameTextView = (TextView) findViewById(R.id.competitionName);
		TextView locationAndDateTextView = (TextView) findViewById(R.id.competitionLocationAndDate);
		TextView timeTextView = (TextView) findViewById(R.id.startTime);
		TextView stateTextView = (TextView) findViewById(R.id.state);
		TextView categoriesTextView = (TextView) findViewById(R.id.categories);
		TextView eventTextView = (TextView) findViewById(R.id.event);

		// Inflate the view: generate the values
		String catsStr = getString(R.string.categories) + " ";
		boolean first = true;
		for (int idCat : competition.getIdCategories()) {
			Category cat = Category.getCategory(idCat);
			if (!first) {				
				catsStr += ", ";
			}
			first = false;
			catsStr += cat.getName();
		}
		String locationAndDateStr = competition.gtScheduledDateStrToShow() + " >> "
				+ competition.getLocation();
		
		String eventStr = getString(R.string.event) + " "
				+ competition.getDistance() + "m";

		// Inflate the view: set the values
		nameTextView.setText(competition.getName());
		timeTextView.setText(getString(R.string.time) + " "
				+ new SimpleDateFormat("HH:mm").format(competition.getScheduledDate()));
		stateTextView.setText(getString(R.string.state) + " " + competition.getStateStr());
		locationAndDateTextView.setText(locationAndDateStr);
		categoriesTextView.setText(catsStr);
		
		eventTextView.setText(eventStr);
		
		hasFirstPainted = true;
		setRegisteredAthletesNumber();
	}
	
	/**
	 * Sets the number of athletes
	 */
	private void setRegisteredAthletesNumber() {
		String registeredNumberStr = getString(R.string.registered_number)
		+ " ";
		
		try{
			parent.getWebService().getAthletesMutex().startToReadOrModify();
			int numAthletes = parent.getWebService().getAthletes().size();
			parent.getWebService().getAthletesMutex().endReadingOrModifying();
			if(numAthletes == 0) {
				registeredNumberStr += "aún no disponible";
			}
			else {
				registeredNumberStr += numAthletes;
			}		
		}
		catch(Exception e) {
			registeredNumberStr += "aún no disponible";
		}
		registeredNumberTextView.setText(registeredNumberStr);
	}
	
	
	/**
	 * Receiver to listen to updates
	 */	
	private class DetailsActivityBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(WebService.CONNECTION_ESTABLISHED)) {
				competition = parent.getWebService().getCompetition();
				setAllTheViews();
			}
			else if (intent.getAction().equals(WebService.ATHLETES_DOWNLOADED)) {
				setRegisteredAthletesNumber();
			}
		}
	}
}
