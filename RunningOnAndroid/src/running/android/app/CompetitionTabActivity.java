package running.android.app;

import running.android.util.AppUtils;
import running.android.app.R;
import running.domain.Competition;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

public class CompetitionTabActivity extends TabActivity {

	// Domain
	private Competition competition;

	// Application
	private WebService webService;
	private CompetitionTabActivityBroadcastReceiver broadcastReceiver;
	private Resources resources;
	private Menu menu;
	private static TabHost tabHost;
	private AlertDialog dialog;
	private Handler handler = new Handler();

	// Flags
	private boolean isConnectedToService;

	// Final
	private static final int LOGIN_ID = 0;
	private static final int LOGOUT_ID = 1;
	private static final int SET_HAS_STARTED_ID = 3;
	private static final int STOP_SIMULATION_ID = 4;
	private static final int MAIN_MENU_GROUP = 1;
	private static final int DIALOG_TIME = 5000;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.competition_tab);

		// Resources
		resources = getResources();

		// Get the competition from the intent
		competition = (Competition) getIntent().getSerializableExtra(
				"competition");

		// Sets the tabs
		tabHost = getTabHost();
		addDetailsTab();
		addAthletesTab();
		addCircuitTab();
		addCommentsTab();
		tabHost.setCurrentTab(0);

		// Tells to log in
		if (AppUtils.getLoggedAthlete(this) == null) {
			Toast.makeText(this, "¿Vas a correr? ¡Inicia sesión en el menú!",
					Toast.LENGTH_SHORT).show();
		}

		// Bind to the service
		getApplicationContext().bindService(new Intent(this, WebService.class),
				serviceConnection, Context.BIND_AUTO_CREATE);

		// Set isn't connected to the service
		this.isConnectedToService = false;

		// Set the broadcast receiver
		this.broadcastReceiver = new CompetitionTabActivityBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WebService.CONNECTION_ESTABLISHED);
		intentFilter.addAction(WebService.DOWNLOAD_SYNC);
		intentFilter.addAction(WebService.HAS_ENDED);
		registerReceiver(this.broadcastReceiver, intentFilter);

	}

	@Override
	public void onResume() {
		super.onResume();
		if (menu != null) {
			setMenu();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Stop listening
		unregisterReceiver(this.broadcastReceiver);

		// Unbind to the service
		getApplicationContext().unbindService(serviceConnection);
	}

	/**
	 * Getter of the webService (for children)
	 */
	public WebService getWebService() {
		return webService;
	}

	/**
	 * Getter of the connection flag (for children)
	 */
	public boolean isConnectedToService() {
		return isConnectedToService;
	}

	private void addDetailsTab() {
		Intent intent = new Intent(this, DetailsActivity.class);
		TabSpec spec = tabHost.newTabSpec("Tab1");
		spec.setIndicator("Detalles", resources
				.getDrawable(android.R.drawable.ic_menu_agenda));
		spec.setContent(intent);
		tabHost.addTab(spec);
	}

	private void addAthletesTab() {
		Intent intent = new Intent(this, AthletesActivity.class);
		TabSpec spec = tabHost.newTabSpec("Tab2");
		spec.setIndicator("Atletas", resources
				.getDrawable(android.R.drawable.ic_menu_myplaces));
		spec.setContent(intent);
		tabHost.addTab(spec);
	}

	private void addCircuitTab() {
		Intent intent = new Intent(this, CircuitActivity.class);
		TabSpec spec = tabHost.newTabSpec("Tab3");
		spec.setIndicator("Circuito", resources
				.getDrawable(android.R.drawable.ic_menu_mapmode));
		spec.setContent(intent);
		tabHost.addTab(spec);
	}

	private void addCommentsTab() {
		Intent intent = new Intent(this, CommentsActivity.class);
		TabSpec spec = tabHost.newTabSpec("Tab4");
		spec.setIndicator("Comentarios", resources
				.getDrawable(android.R.drawable.ic_menu_crop));
		spec.setContent(intent);
		tabHost.addTab(spec);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		this.menu = menu;
		setMenu();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case (LOGIN_ID): {
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			return true;
		}
		case (LOGOUT_ID): {
			AppUtils.removeLoggedAthlete(this);
			setMenu();
			return true;
		}
		case (SET_HAS_STARTED_ID): {
			webService.setHasStarted();
			setMenu();
			return true;
		}
		case (STOP_SIMULATION_ID): {
			webService.stopSimulation();
			setMenu();
			return true;
		}
		}
		return false;
	}

	/**
	 * Sets the menu
	 */
	private void setMenu() {
		menu.removeGroup(MAIN_MENU_GROUP);
		if (AppUtils.getLoggedAthlete(this) == null) {
			menu.add(MAIN_MENU_GROUP, LOGIN_ID, Menu.NONE, R.string.login);
		} else {
			menu.add(MAIN_MENU_GROUP, LOGOUT_ID, Menu.NONE, R.string.logout);
		}
		if (competition.getState() == Competition.NOT_STARTED
				&& isConnectedToService) {
			menu.add(MAIN_MENU_GROUP, SET_HAS_STARTED_ID, Menu.NONE,
					R.string.set_has_started);
		}
		else if (competition.getState() == Competition.TAKING_PLACE
				&& isConnectedToService) {
			menu.add(MAIN_MENU_GROUP, STOP_SIMULATION_ID, Menu.NONE,
					R.string.stop_simulation);
		}
	}

	/**
	 * Connection to the service
	 */
	private ServiceConnection serviceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder binder) {
			Log.i("LOG", "Service connected to CompetitionTabActivity");

			// Set the webService
			webService = ((WebService.WebBinder) binder).getService();

			// Set the competition
			webService.setCompetition(competition);

			// Download the athletes and circuit
			webService.downloadAthletesAndCircuit();

			// Broadcast that the connection is established (Details Activity
			// will be able to show the competition details
			sendBroadcast(new Intent(WebService.CONNECTION_ESTABLISHED));

		}

		public void onServiceDisconnected(ComponentName className) {
			Log.i("LOG", "Service disconnected from CompetitionTabActivity");
			webService = null;
		}
	};

	/**
	 * Show the podium when the competition has finished
	 */
	protected Dialog onCreateDialog(int id) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.podium,
				(ViewGroup) findViewById(R.id.podium_layout));

		ImageView firstFace = (ImageView) layout.findViewById(R.id.first_face);
		ImageView secondFace = (ImageView) layout
				.findViewById(R.id.second_face);
		ImageView thirdFace = (ImageView) layout.findViewById(R.id.third_face);
		TextView firstName = (TextView) layout.findViewById(R.id.first_name);
		TextView secondName = (TextView) layout.findViewById(R.id.second_name);
		TextView thirdName = (TextView) layout.findViewById(R.id.third_name);

		// Get the names and faces from athlete list
		webService.getAthletesMutex().startToReadOrModify();

		// Faces
		Bitmap bitmap1 = webService.getFaces().get(
				webService.getAthletes().get(0).getId());
		Bitmap bitmap2 = webService.getFaces().get(
				webService.getAthletes().get(1).getId());
		Bitmap bitmap3 = webService.getFaces().get(
				webService.getAthletes().get(2).getId());

		// Names
		String name1 = "" + webService.getAthletes().get(0).getFirstName()
				+ " " + webService.getAthletes().get(0).getLastName();
		String name2 = "" + webService.getAthletes().get(1).getFirstName()
				+ " " + webService.getAthletes().get(1).getLastName();
		String name3 = "" + webService.getAthletes().get(2).getFirstName()
				+ " " + webService.getAthletes().get(2).getLastName();

		// End modifying
		webService.getAthletesMutex().endReadingOrModifying();

		// First athlete
		if (bitmap1 == null) {
			bitmap1 = webService.getFaces().get(0);
		}
		firstFace.setImageBitmap(bitmap1);
		firstName.setText(name1);

		// Second athlete
		if (bitmap2 == null) {
			bitmap2 = webService.getFaces().get(0);
		}
		secondFace.setImageBitmap(bitmap2);
		secondName.setText(name2);

		// Third athlete
		if (bitmap3 == null) {
			bitmap3 = webService.getFaces().get(0);
		}
		thirdFace.setImageBitmap(bitmap3);
		thirdName.setText(name3);

		builder.setView(layout);

		final Runnable dismissDialog = new Runnable() {
			public void run() {
				if (dialog != null) {
					dialog.dismiss();
				}
			}
		};

		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(DIALOG_TIME);
				} catch (InterruptedException e) {
					Log.e("TAB", "Interrupted exception");
				}
				handler.post(dismissDialog);
			}
		}).start();

		this.dialog = builder.create();
		return dialog;
	}

	/**
	 * Receiver to listen to updates
	 */
	private class CompetitionTabActivityBroadcastReceiver extends
			BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(WebService.CONNECTION_ESTABLISHED)) {
				isConnectedToService = true;
				if (menu != null) {
					setMenu();
				}
			} else if (intent.getAction().equals(WebService.DOWNLOAD_SYNC)) {
				if (menu != null) {
					setMenu();
				}
			} else if (intent.getAction().equals(WebService.HAS_ENDED)) {
				showDialog(0);
			}
		}
	}
}
