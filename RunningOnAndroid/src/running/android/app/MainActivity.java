package running.android.app;

import java.util.ArrayList;
import java.util.List;

import running.android.web.WebInterface;

import running.android.app.R;
import running.domain.Competition;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity {
	private ListView listView;
	private ArrayAdapter<Competition> adapter;
	private List<Competition> competitions;
	private static final int REFRESH_ID = 1;
	private static final int TEST_COMPETITION_ID = 2;
	private LayoutInflater inflater;
	private Activity activity;
	private ProgressDialog progressDialog;
	private final Handler handler = new Handler(); // Necessary for talking
													// between threads
	private AlertDialog circuitNamesDialog;
	private String[] circuitNames;
	private int idCircuitSelected;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.main);
		listView = (ListView) this.findViewById(R.id.listView);

		// Save the activity in a variable to put into the intent
		activity = this;
		// Listener for the list items
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView av, View v, int index, long arg3) {
				Intent intent = new Intent(activity,
						CompetitionTabActivity.class);
				intent.putExtra("competition", competitions.get(index));
				startActivity(intent);
			}
		});

		// Create the arrayList for the competitions
		competitions = new ArrayList<Competition>();

		// Inflater
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// Adapter
		adapter = new ArrayAdapter<Competition>(this,
				R.layout.competition_item, competitions) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View itemView;

				// Get the view
				if (convertView == null) {
					itemView = inflater
							.inflate(R.layout.competition_item, null);
				} else {
					itemView = convertView;
				}

				// Inflate the view
				Competition comp = getItem(position);
				if (comp.getState() == Competition.ENDED) {
					itemView.setBackgroundResource(R.color.ended);
					Log.w("a", "ended" + itemView);
				} else if (comp.getState() == Competition.TAKING_PLACE) {
					itemView.setBackgroundResource(R.color.taking_place);
					Log.w("a", "taking place" + itemView);
				} else {
					itemView.setBackgroundResource(R.color.not_started);
					Log.w("a", "not started" + itemView);
				}

				TextView compTextView = (TextView) itemView
						.findViewById(R.id.competition);
				compTextView.setText(comp.getName());
				TextView locationTextView = (TextView) itemView
						.findViewById(R.id.locationAndDate);
				locationTextView.setText(comp.gtScheduledDateStrToShow()
						+ " >> " + comp.getLocation());
				// Return the view
				return itemView;
			}
		};
		listView.setAdapter(adapter);
	}

	@Override
	public void onResume() {
		super.onResume();
		showProgressDialog();
		new Thread(downloadCompetitions).start();
	}

	public void onPause() {
		super.onPause();
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(Menu.NONE, REFRESH_ID, Menu.NONE, R.string.refresh);
		menu.add(Menu.NONE, TEST_COMPETITION_ID, Menu.NONE,
				R.string.test_competition);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case (REFRESH_ID): {
			showProgressDialog();
			new Thread(downloadCompetitions).start();
			return true;
		}
		case (TEST_COMPETITION_ID): {
			new Thread(downloadCircuits).start();
			return true;
		}
		}
		return false;
	}

	private void showProgressDialog() {
		progressDialog = ProgressDialog.show(this, "Espera...",
				"Buscando competiciones en internet...", true);

	}

	/**
	 * Crea el dialog para selccionar tags
	 */
	@Override
	protected Dialog onCreateDialog(int id) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.test_circuits_dialog);
		builder.setSingleChoiceItems(circuitNames, -1,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						idCircuitSelected = which;
						new Thread(createTestCompetition).start();					
						circuitNamesDialog.dismiss();
					}
				});

		circuitNamesDialog = builder.create();
		return circuitNamesDialog;
	}

	// Runnable for notifying the download of the competitions
	private final Runnable notifyCompetitionsDownloaded = new Runnable() {
		public void run() {
			adapter.notifyDataSetChanged();
			progressDialog.dismiss();
			progressDialog = null;
		}
	};

	// Runnable for downloading the competitions
	private final Runnable downloadCompetitions = new Runnable() {
		public void run() {
			// Get the competitions from the server
			WebInterface.addCompetitions(activity, competitions);
			handler.post(notifyCompetitionsDownloaded);
		}
	};

	// Runnable for creating the test competition
	private final Runnable downloadCircuits = new Runnable() {
		public void run() {
			// Get the circuitNames from the server
			Log.w("MAIN", "circuit: download circuits");
			circuitNames = WebInterface.getCircuitNames();
			handler.post(notifyCircuitsDownloaded);
		}
	};
	
	// Runnable for creating the test competition
	private final Runnable createTestCompetition = new Runnable() {
		public void run() {
			// Get the competitions from the server
			WebInterface.createTestCompetition(idCircuitSelected);
			handler.post(notifyTestCompetitionCreated);
			new Thread(downloadCompetitions).start();
		}
	};
	
	// Runnable for showing the progess dialog
	private final Runnable notifyTestCompetitionCreated = new Runnable() {
		public void run() {
			showProgressDialog();
		}
	};
	
	// Runnable for creating the dialog
	private final Runnable notifyCircuitsDownloaded = new Runnable() {
		public void run() {
			showDialog(0);
		}
	};
}