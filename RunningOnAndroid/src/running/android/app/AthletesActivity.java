package running.android.app;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import running.android.app.ColorPickerDialog.OnColorChangedListener;

import running.android.app.R;
import running.android.util.MutexManager;
import running.domain.Athlete;
import running.domain.Competition;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class AthletesActivity extends Activity implements
		OnColorChangedListener {

	// Domain
	private List<Athlete> athletes;
	private Competition competition;

	// Application
	private WebService webService;
	private CompetitionTabActivity parent;
	private AthletesActivityBroadcastReceiver broadcastReceiver;
	private IntentFilter intentFilter;
	private ArrayAdapter<Athlete> adapter;
	private ListView listView;
	
	// Concurrency
	private MutexManager athletesMutex;

	// Flags
	private int selectedIdAthlete;
	private boolean areAthletesDownloaded;
	private int lastCompetitionState;

	// Finals
	private final int FOLLOWING_COLOR = 0;
	private final int FOLLOWING_FACE = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.athletes);

		// Get the parent
		parent = (CompetitionTabActivity) getParent();

		// Get the list view
		listView = (ListView) this.findViewById(R.id.athletesListView);

		// Listener for the list items
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView av, View v, int index, long arg3) {
				launchFollowingDialog(index);
			}
		});

		// Get the service and the domain from it if its connected
		// Otherwise, it will get it when broadcasted
		if (parent.isConnectedToService()) {
			this.webService = parent.getWebService();
			this.competition = webService.getCompetition();
			this.athletes = webService.getAthletes();
			this.athletesMutex = webService.getAthletesMutex();
			
			this.athletesMutex.startToReadOrModify();
			this.areAthletesDownloaded = this.athletes.size() > 0;
			this.athletesMutex.endReadingOrModifying();
			
			this.lastCompetitionState = competition.getState();
			setAdapter();
		}

		// Set the broadcast receiver
		this.broadcastReceiver = new AthletesActivityBroadcastReceiver();
		this.intentFilter = new IntentFilter();
		this.intentFilter.addAction(WebService.CONNECTION_ESTABLISHED);
		this.intentFilter.addAction(WebService.ATHLETES_DOWNLOADED);
		this.intentFilter.addAction(WebService.DOWNLOAD_SYNC);
		this.intentFilter.addAction(WebService.HAS_ENDED);
	}

	@Override
	public void onResume() {
		super.onResume();

		// if its connected, and the athletes were not still downloaded
		if (parent.isConnectedToService() && !areAthletesDownloaded) {
			// If they are now downloaded, we notify it
			if (athletes.size() > 0) {
				adapter.notifyDataSetChanged();
				areAthletesDownloaded = true;
			}
		}
		registerReceiver(this.broadcastReceiver, this.intentFilter);
		if (competition != null
				&& competition.getState() != this.lastCompetitionState) {
			this.lastCompetitionState = competition.getState();
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * Launches the dialog to determine how to follow an athlete
	 * 
	 * @param index position of the athlete in the list
	 */
	private void launchFollowingDialog(int index) {
		this.athletesMutex.startToReadOrModify();
		final Athlete ath = athletes.get(index);
		selectedIdAthlete = ath.getId();
		String selectedName = ath.getFirstName() + " " + ath.getLastName();
		this.athletesMutex.endReadingOrModifying();

		new AlertDialog.Builder(this).setTitle(selectedName).setItems(
				R.array.athlete_options, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialoginterface, int i) {
						athletesMutex.startToReadOrModify();
						// Follow by color
						if (i == FOLLOWING_COLOR) {
							ath.setFollowing(false);
							ath.setShowFace(false);
							new ColorPickerDialog(AthletesActivity.this,
									AthletesActivity.this, ath.getColor())
									.show();
						}

						// Follow by face
						else if (i == FOLLOWING_FACE) {
							ath.setFollowing(true);
							ath.setShowFace(true);
							ath.setDefaultColor();
						}

						// Don't follow
						else {
							ath.setFollowing(false);
							ath.setShowFace(false);
							ath.setDefaultColor();
						}
						athletesMutex.endReadingOrModifying();
						adapter.notifyDataSetChanged();
					}
				}).show();
	}

	/**
	 * Sets the adapter for the list of competitions
	 */
	private void setAdapter() {
		// Inflater (it will be used inside the adapter)
		final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Adapter
		adapter = new ArrayAdapter<Athlete>(this, R.layout.athlete_item,
				athletes) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// Get the view
				View itemView;
				if (convertView == null) {
					itemView = inflater.inflate(R.layout.athlete_item, null);
				} else {
					itemView = convertView;
				}

				// Get the views
				TextView compTextView = (TextView) itemView
						.findViewById(R.id.athleteName);
				TextView colorView = (TextView) itemView
						.findViewById(R.id.colorView);
				TextView categoryTextView = (TextView) itemView
						.findViewById(R.id.athleteCategory);
				TextView statsTextView = (TextView) itemView
				.findViewById(R.id.athleteStats);
				ImageView faceView = (ImageView) itemView
						.findViewById(R.id.faceView);
				ImageView tickView = (ImageView) itemView
						.findViewById(R.id.tickView);
				tickView.setBackgroundResource(R.drawable.tick);

				// Generate the strings
				String nameStr = "";
				String categoryStr = "";

				// Clone the athlete
				athletesMutex.startToReadOrModify();
				Athlete athlete;
				try {
					athlete = getItem(position).clone();
				} catch (CloneNotSupportedException e) {
					Log.e("ATH", "Clone error");
					athlete = new Athlete();
				}
				
				// Get the time of the first athlete
				long firstAthTime = athletes.get(0).getTime();
				int firstAthDistanceFromStart = athletes.get(0).getDistanceFromStart();
				
				athletesMutex.endReadingOrModifying();

				// Position when taking place or ended
				if (competition.getState() != Competition.NOT_STARTED) {
					nameStr += athlete.getAbsolutePosition() + "º ";
					categoryStr += athlete.getCategoryPosition() + "º ";
				}

				// Name and number always
				nameStr += athlete.getFirstName() + " " + athlete.getLastName()
				 + " (#" + athlete.getNumber() + ")";
				categoryStr += athlete.gtCategory().getName();

				// Distance from start when taking place
				if (competition.getState() == Competition.TAKING_PLACE) {
					if (athlete.isHasArrived()) {
						categoryStr += " - Meta";
					} else {
						categoryStr += " - " + athlete.getDistanceFromStart()
								+ " metros";
					}
				}
				
				
				
				String statsStr = "";

				// Time to first when taking place
				if (competition.getState() != Competition.NOT_STARTED) {
					statsStr += "Tiempo: ";
					if (athlete.getAbsolutePosition() == 1) {
						statsStr += athlete.getTimeStr();
					} else {
						if (athlete.isHasArrived()) {
							// Real final difference
							long timeFromFirst = athlete.getTime()
									- firstAthTime;
							statsStr += " +"
									+ new SimpleDateFormat("m:ss")
											.format(new Date(timeFromFirst));
						} else {
							// Estimation
							statsStr += " +"
									+ athlete.getTimeToFirst(firstAthDistanceFromStart, 
											webService.getMillisFromEnd());
						}
					}
				}

				// Speed when taking place or ended
				if (competition.getState() != Competition.NOT_STARTED) {
					statsStr += " - Velocidad: " + athlete.getSpeedKMH();
				}

				// Get the face
				Bitmap bitmap = webService.getFaces().get(athlete.getId());
				if (bitmap == null) {
					bitmap = webService.getFaces().get(0);
				}

				// Set the values
				compTextView.setText(nameStr);
				categoryTextView.setText(categoryStr);
				statsTextView.setText(statsStr);
				faceView.setImageBitmap(bitmap);

				// Show how is following
				if (athlete.isFollowing()) {
					tickView.setVisibility(View.VISIBLE);
					if (athlete.isShowFace()) {
						colorView.setBackgroundColor(0);
					} else {
						colorView.setBackgroundColor(athlete.getColor());
					}
				} else {
					tickView.setVisibility(View.INVISIBLE);
					colorView.setBackgroundColor(0);
				}
				
				if(competition.getState() == Competition.NOT_STARTED) {
					statsTextView.setVisibility(View.GONE);
				} else {
					statsTextView.setVisibility(View.VISIBLE);
				}

				// Return the view
				return itemView;
			}
		};
		listView.setAdapter(adapter);
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(this.broadcastReceiver);
	}

	/**
	 * Method that the ColorPickerDialog calls after the user selects a color
	 * 
	 * @param color chosen color as integer
	 */
	public void colorChanged(int color) {
		this.athletesMutex.startToReadOrModify();
		Athlete ath = webService.getAthlete(selectedIdAthlete);
		ath.setFollowing(true);
		ath.setColor(color);
		this.athletesMutex.endReadingOrModifying();
		adapter.notifyDataSetChanged();
	}

	/**
	 * Receiver to listen to updates
	 */
	private class AthletesActivityBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("WEBSERVICE", "atletas rec");
			if (!parent.isConnectedToService()) {
				return;
			}
			if (webService == null) {
				webService = parent.getWebService();
				competition = webService.getCompetition();
				athletes = webService.getAthletes();
				setAdapter();
			} else {
				if (adapter != null) {
					Log.w("log", "ath not");
					adapter.notifyDataSetChanged();
				}
			}
		}
	}
}