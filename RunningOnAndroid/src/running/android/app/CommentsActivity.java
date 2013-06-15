package running.android.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import running.android.app.R;
import running.android.util.AppUtils;
import running.android.util.MutexManager;
import running.domain.Athlete;
import running.domain.Comment;
import running.domain.Competition;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CommentsActivity extends Activity {

	// Domain
	private List<Comment> comments;
	private List<Comment> commentsShown;
	private Competition competition;
	private String filter;

	// Application
	private WebService webService;
	private CompetitionTabActivity parent;
	private CommentsActivityBroadcastReceiver broadcastReceiver;
	private IntentFilter intentFilter;
	private ArrayAdapter<Comment> adapter;
	private ListView listView;

	// Concurrency
	private MutexManager commentsMutex;

	// Flags
	private int lastCompetitionState;
	private boolean areCommentsDownloaded;
	private boolean isTakingPhoto;

	// Photo
	private String photoName;
	private File path;
	private File file;
	private byte[] photo = null;

	// Log
	private final static String LOG_TAG = "COMMENTS";

	// Menu
	private final int NO_FILTER = 9;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comments);

		// Get the parent
		parent = (CompetitionTabActivity) getParent();

		// Get the list view
		listView = (ListView) this.findViewById(R.id.commentsListView);

		Button button = (Button) this.findViewById(R.id.postButton);
		final EditText commentEdit = (EditText) this
				.findViewById(R.id.commentText);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (commentEdit.getText().length() == 0) {
					Toast.makeText(CommentsActivity.this,
							"¡Escribe un Comentario!", Toast.LENGTH_SHORT).show();
				} else {								
					// Hide the keyboard
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(commentEdit.getWindowToken(), 0);

					Athlete ath = AppUtils.getLoggedAthlete(CommentsActivity.this);
					if(ath == null) {
						Toast.makeText(CommentsActivity.this,
								"Inicia sesión para poder enviar un mensaje",
								Toast.LENGTH_SHORT).show();
					} else {
						// Get the position
						LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
						Location location = locationManager
								.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						int lon = (int) (location.getLongitude() * 1000000);
						int lat = (int) (location.getLatitude() * 1000000);

						// Post the comment
						webService.postComment(new Comment(-1, commentEdit
								.getText().toString(), AppUtils.getLoggedAthlete(
								CommentsActivity.this).getUserName(),
								new ArrayList<String>(), competition.getId(), lat,
								lon, new Date()));

						/*if (photo != null) {
							Log.i(LOG_TAG, "photo !null");
							webService.sendImage(photo);
						} else {
							Log.i(LOG_TAG, "photo null");
						}*/

						// Empty the field
						commentEdit.setText("");
					}
					
				}
			}
		});

		/*Button cameraButton = (Button) this.findViewById(R.id.cameraButton);
		cameraButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(CommentsActivity.this,
						CameraActivity.class);
				isTakingPhoto = true;
				photoName = "" + new Date().getTime() + ".jpg";
				intent.putExtra("nombre", photoName);
				startActivity(intent);
			}

		});*/

		// Listener for the list items TODO
		/*
		 * listView.setOnItemClickListener(new OnItemClickListener() { public
		 * void onItemClick(AdapterView av, View v, int index, long arg3) {
		 * launchFollowingDialog(index); } });
		 */

		// Get the service and the domain from it if it's connected
		// Otherwise, it will get it when broadcasted
		this.commentsShown = new ArrayList<Comment>();
		if (parent.isConnectedToService()) {
			this.webService = parent.getWebService();
			this.competition = webService.getCompetition();
			this.comments = webService.getComments();
			this.commentsMutex = webService.getCommentsMutex();

			this.lastCompetitionState = competition.getState();
			setAdapter();

			// Don't filter any comments
			this.filter = null;
			filterComments();
		}

		// Set the broadcast receiver
		this.broadcastReceiver = new CommentsActivityBroadcastReceiver();
		this.intentFilter = new IntentFilter();
		this.intentFilter.addAction(WebService.CONNECTION_ESTABLISHED);
		this.intentFilter.addAction(WebService.COMMENTS_DOWNLOADED);
		this.intentFilter.addAction(WebService.DOWNLOAD_SYNC);
		this.intentFilter.addAction(WebService.HAS_ENDED);

		this.isTakingPhoto = false;
	}

	@Override
	public void onResume() {
		super.onResume();

		// if its connected, and the comments were not still downloaded
		if (parent.isConnectedToService() && !areCommentsDownloaded) {
			// If they are now downloaded, we notify it
			commentsMutex.startToReadOrModify();
			int commentsSize = comments.size();
			commentsMutex.endReadingOrModifying();
			if (commentsSize > 0) {
				filterComments();
				areCommentsDownloaded = true;
			}
		}

		registerReceiver(this.broadcastReceiver, this.intentFilter);
		if (competition != null
				&& competition.getState() != this.lastCompetitionState) {
			this.lastCompetitionState = competition.getState();
			filterComments();
		}

		if (isTakingPhoto) {
			path = Environment.getExternalStorageDirectory();
			Log.i("CAM", "lec path" + path.getAbsolutePath());
			Log.i("CAM", "ph: " + photoName);
			file = new File(path, photoName);
			photo = new byte[(int) file.length()];
			try {
				InputStream is = new FileInputStream(file);
				is.read(photo);
			} catch (IOException e) {
				photo = null;
			}
			isTakingPhoto = false;
		}
	}

	/**
	 * Sets the adapter for the list of competitions
	 */
	private void setAdapter() {
		// Inflater (it will be used inside the adapter)
		final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Adapter
		adapter = new ArrayAdapter<Comment>(this, R.layout.comment_item,
				this.commentsShown) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// Get the view
				View itemView;
				if (convertView == null) {
					itemView = inflater.inflate(R.layout.comment_item, null);
				} else {
					itemView = convertView;
				}

				// Get the views
				commentsMutex.startToReadOrModify();
				Comment comment = getItem(position);
				String commentText = "" + comment.getText();
				String commentWriter = "" + comment.getWriter();
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy, HH:mm");
				String commentDateStr = sdf.format(comment.getDate());
				commentsMutex.endReadingOrModifying();

				TextView textTextView = (TextView) itemView
						.findViewById(R.id.commentText);
				TextView writerTextView = (TextView) itemView
						.findViewById(R.id.commentWriter);
				TextView dateTextView = (TextView) itemView
						.findViewById(R.id.commentDate);

				// Set the text
				textTextView
						.setText(commentText, TextView.BufferType.SPANNABLE);
				writerTextView.setText("@" + commentWriter,
						TextView.BufferType.SPANNABLE);
				dateTextView.setText(commentDateStr);

				// Generate the spannable String
				Spannable writerS = (Spannable) writerTextView.getText();
				Spannable textS = (Spannable) textTextView.getText();

				// Create the clickable span
				final String writerText = commentWriter;
				ClickableSpan writerClickableSpan = new ClickableSpan() {
					@Override
					public void onClick(View view) {
						filter = writerText;
						filterComments();
					}
				};

				writerS.setSpan(writerClickableSpan, 0, writerS.length(),
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

				// Look for the references in text
				int lastIndexAt;
				// Variable for not to cut the string in future searches
				int lastEndOfUserName = 0;
				while ((lastIndexAt = commentText.indexOf('@',
						lastEndOfUserName)) != -1) {

					// Only ends with the end of comment or white space
					int lastIndexSpace = commentText.indexOf(' ', lastIndexAt);
					if (lastIndexSpace != -1) {
						lastEndOfUserName = lastIndexSpace;
					} else {
						lastEndOfUserName = commentText.length();
					}

					// Create the clickable span
					final String username = commentText.substring(
							lastIndexAt + 1, lastEndOfUserName);
					ClickableSpan textClickableSpan = new ClickableSpan() {
						@Override
						public void onClick(View view) {
							filter = username;
							filterComments();
						}
					};

					// Span the string
					textS
							.setSpan(textClickableSpan, lastIndexAt,
									lastEndOfUserName,
									Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}

				lastEndOfUserName = 0;
				while ((lastIndexAt = commentText.indexOf('#',
						lastEndOfUserName)) != -1) {

					// Only ends with the end of comment or white space
					int lastIndexSpace = commentText.indexOf(' ', lastIndexAt);
					if (lastIndexSpace != -1) {
						lastEndOfUserName = lastIndexSpace;
					} else {
						lastEndOfUserName = commentText.length();
					}

					int number = -1;
					try {
						number = Integer.parseInt(commentText.substring(
								lastIndexAt + 1, lastEndOfUserName));
					} catch (NumberFormatException e) {
						Log.i(LOG_TAG, "NumberFormatException #");
						
					}

					if(number != -1) {
						// Create the clickable span
						String userName = null;
						webService.getAthletesMutex().startToReadOrModify();
						for (Athlete a : webService.getAthletes()) {
							if (a.getNumber() == number) {
								userName = "" + a.getUserName();
							}
						}
						webService.getAthletesMutex().endReadingOrModifying();
						
						if(userName != null) {
							final String userN = userName; 
							ClickableSpan textClickableSpan = new ClickableSpan() {
								@Override
								public void onClick(View view) {
									filter = userN;
									filterComments();
								}
							};
	
							// Span the string
							textS
									.setSpan(textClickableSpan, lastIndexAt,
											lastEndOfUserName,
											Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
						}
					}
				}

				// Allow movement
				textTextView
						.setMovementMethod(LinkMovementMethod.getInstance());
				writerTextView.setMovementMethod(LinkMovementMethod
						.getInstance());

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

	private void filterComments() {
		// Clear the comments shown
		this.commentsShown.clear();

		commentsMutex.startToReadOrModify();
		// null -> Copy all the comments
		if (this.filter == null) {
			Log.i(LOG_TAG, "Filter comments: no filter");
			for (Comment c : this.comments) {
				this.commentsShown.add(c);
			}
			// Else, filter the comments
		} else {
			Log.i(LOG_TAG, "Filter comments: " + this.filter);
			for (Comment c : comments) {
				if (c.getWriter().equals(this.filter)) {
					this.commentsShown.add(c);
				} else {
					for (String ref : c.getUserNames()) {
						if (ref.equals(this.filter)) {
							this.commentsShown.add(c);
							break;
						}
					}
				}
			}
		}
		commentsMutex.endReadingOrModifying();
		adapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(Menu.NONE, NO_FILTER, Menu.NONE, R.string.no_filter);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case (NO_FILTER): {
			this.filter = null;
			filterComments();
			return true;
		}
		}
		return false;
	}

	/**
	 * Receiver to listen to updates
	 */
	private class CommentsActivityBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!parent.isConnectedToService()) {
				return;
			}

			if (webService == null) {
				webService = parent.getWebService();
				competition = webService.getCompetition();
				comments = webService.getComments();
				setAdapter();
				filterComments();
			} else {
				if (adapter != null) {
					filterComments();
				}
			}
		}
	}
}