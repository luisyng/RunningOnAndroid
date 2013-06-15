package running.android.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import running.android.domain.Circuit;
import running.android.util.AppUtils;
import running.android.util.MutexManager;
import running.android.web.WebInterface;

import com.google.android.maps.GeoPoint;

import running.android.app.R;
import running.domain.Athlete;
import running.domain.Comment;
import running.domain.Competition;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.Toast;

public class WebService extends Service {

	// Domain
	private Circuit circuit;
	private Competition competition;
	private List<Athlete> athletes;
	private List<Comment> comments;
	private Map<Integer, Bitmap> faces;
	private List<Integer> distancesFromStart;
	private Athlete loggedAthlete;

	// Application
	private final IBinder binder = new WebBinder();
	private Handler handler = new Handler();
	private LocationManager locationManager;
	private Timer timer;
	private Chronometer chrono;
	private long ellapsedTimeFirst;
	private MyItemizedOverlay commentsOverlay;

	// Concurrency
	private MutexManager athletesMutex;
	private MutexManager commentsMutex;

	// Flags
	private boolean isCheckingHasStarted;

	// Log
	private final static String LOG_TAG = "WEBSERVICE";

	// Final
	private static final int SYNC_DONWLOAD_PERIOD = 1000; // milliseconds
	private static final int SYNC_UPLOAD_PERIOD = 1000; // milliseconds

	public static final String CONNECTION_ESTABLISHED = "connection established";
	public static final String ATHLETES_DOWNLOADED = "athletes downloaded";
	public static final String CIRCUIT_DOWNLOADED = "circuit downloaded";
	public static final String COMMENTS_DOWNLOADED = "comments downloaded";
	public static final String DOWNLOAD_SYNC = "donwload sync";
	public static final String HAS_STARTED = "has started";
	public static final String HAS_ENDED = "has ended";

	@Override
	public void onCreate() {
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		athletes = new ArrayList<Athlete>();
		comments = new ArrayList<Comment>();
		isCheckingHasStarted = false;
		chrono = new Chronometer(this);
		chrono.setBackgroundColor(Color.BLACK);
		chrono.setTextColor(Color.WHITE);
		ellapsedTimeFirst = 0;

		// Mutex
		athletesMutex = new MutexManager("ATHLETES");
		commentsMutex = new MutexManager("COMMENTS");
	}

	/**
	 * Stops working (ends the timers)
	 */
	@Override
	public void onDestroy() {
		if (timer != null) {
			timer.cancel();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	/**
	 * Gets the faces as a hashmap of <idathlete,bitmap>
	 */
	public Map<Integer, Bitmap> getFaces() {
		return faces;
	}

	public void setCompetition(Competition competition) {
		this.competition = competition;

		if (competition.getState() == Competition.NOT_STARTED) {
			this.isCheckingHasStarted = true;
			this.startCheckingHasStarted();
		}
	}

	public MyItemizedOverlay getCommentsOverlay() {
		return commentsOverlay;
	}

	public void setCommentsOverlay(MyItemizedOverlay commentsOverlay) {
		this.commentsOverlay = commentsOverlay;
	}

	public Competition getCompetition() {
		return competition;
	}

	public Circuit getCircuit() {
		return circuit;
	}

	public Chronometer getChrono() {
		return this.chrono;
	}

	public long getMillisFromEnd() {
		if (ellapsedTimeFirst == 0) {
			return 0;
		}
		return SystemClock.elapsedRealtime() - ellapsedTimeFirst;
	}

	/**
	 * Gets the athletes
	 */
	public List<Athlete> getAthletes() {
		return athletes;
	}

	/**
	 * Gets the athletes
	 */
	public List<Comment> getComments() {
		return comments;
	}

	public Athlete getAthlete(int idAthlete) {
		for (Athlete ath : getAthletes()) {
			if (ath.getId() == idAthlete) {
				return ath;
			}
		}
		return null;
	}

	/**
	 * Starts downloading athletes and circuit
	 */
	public void downloadAthletesAndCircuit() {
		new Thread(downloadAthletesCommentsAndCircuit).start();
	}

	/**
	 * Asks the server to stop the simulation
	 */
	public void stopSimulation() {
		new Thread() {
			public void run() {
				WebInterface.stopSimulation(competition.getId());
			}
		}.start();
	}

	/**
	 * Ask the server to start the competition (and start the simulation)
	 */
	public void setHasStarted() {
		new Thread(setHasStarted).start();
	}

	/**
	 * Starts the synchronizing task of downloading
	 */
	private void startSyncDownload() {
		timer.schedule(syncDownloadTask, SYNC_DONWLOAD_PERIOD,
				SYNC_DONWLOAD_PERIOD);
	}

	/**
	 * Starts the synchronizing task of uploading
	 */
	private void startSyncUpload() {
		distancesFromStart = new ArrayList<Integer>();
		distancesFromStart.add(0);
		timer.schedule(syncUploadTask, SYNC_UPLOAD_PERIOD, SYNC_UPLOAD_PERIOD);
	}

	/**
	 * Starts the to check if the competition has started
	 */
	public void startCheckingHasStarted() {
		timer = new Timer();
		timer.schedule(checkHasStarted, SYNC_DONWLOAD_PERIOD,
				SYNC_DONWLOAD_PERIOD);
	}

	/**
	 * TIMER TASK for checking if has started
	 */
	private final TimerTask checkHasStarted = new TimerTask() {
		public void run() {
			Log.i(LOG_TAG, "Check if has started");

			if (WebInterface.checkHasStarted(competition)) {
				Log.i(LOG_TAG, "The competition has started");
				competition.setState(Competition.TAKING_PLACE);

				// Start the chrono
				chrono.setBase(SystemClock.elapsedRealtime());
				chrono.start();

				// Broadcast that has started
				sendBroadcast(new Intent(HAS_STARTED));

				// Create a new timer for synchronizing
				Timer oldTimer = timer;
				timer = new Timer();

				// Always activate the donwloading mode
				startSyncDownload();
				Log.d(LOG_TAG, "Sync donwloading scheduled");

				// Only if there an athlete logged we activate the uploading
				// mode
				loggedAthlete = AppUtils.getLoggedAthlete(WebService.this);
				if (loggedAthlete != null) {
					Log.i(LOG_TAG, "Logged athlete");
					startSyncUpload();
				} else {
					Log.i(LOG_TAG, "Not logged athlete");
				}

				// Notify the starting to the user
				handler.post(notifyHasStarted);

				// Cancel the old timer
				Log.d(LOG_TAG, "Timer cancelled");
				oldTimer.cancel();
			}
		}
	};

	/**
	 * RUNNABLE for setting has started
	 */
	private final Runnable setHasStarted = new Runnable() {
		public void run() {
			WebInterface.setHasStarted(competition.getId());
		}
	};

	/**
	 * RUNNABLE for notifying the user that the competition has started
	 */
	private final Runnable notifyHasStarted = new Runnable() {
		public void run() {
			Log.i(LOG_TAG, "Show toast");
			Toast.makeText(WebService.this, "La competición ha empezado",
					Toast.LENGTH_SHORT).show();
		}
	};

	/**
	 * TIMER TASK for download sync task
	 */
	private final TimerTask syncDownloadTask = new TimerTask() {
		public void run() {
			Log.i(LOG_TAG, "Download syncrhonization");

			// If the competition has ended, we stop the timer
			if (competition.getState() != Competition.TAKING_PLACE) {
				Log.i(LOG_TAG, "Competition just ended!");
				timer.cancel();
			} else {
				WebInterface.refreshAthletesDistancesFromStart(competition,
						athletes, WebService.this, athletesMutex);
				if (athletes.get(0).isHasArrived() && ellapsedTimeFirst == 0) {
					ellapsedTimeFirst = SystemClock.elapsedRealtime();
				}
				sendBroadcast(new Intent(DOWNLOAD_SYNC));
				Log.i(LOG_TAG, "Last athlete points downloaded");
				
				int lastIdComment = -1;
				if (!comments.isEmpty()) {
					lastIdComment = comments.get(0).getId();
				}
				WebInterface.addComments(comments, competition,
						lastIdComment, commentsMutex, commentsOverlay);
				sendBroadcast(new Intent(COMMENTS_DOWNLOADED));
			}

			if (competition.getState() == Competition.ENDED) {
				cancel();
				sendBroadcast(new Intent(HAS_ENDED));
			}
		}
	};

	/**
	 * TIMER TASK for upload sync task
	 */
	private final TimerTask syncUploadTask = new TimerTask() {
		public void run() {
			Log.i(LOG_TAG, "Upload syncrhonization");

			// Get the location from GPS
			Location location = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			Log.d(LOG_TAG, "New position" + location.getLatitude() + " "
					+ location.getLongitude());
			int longitud = (int) (location.getLongitude() * 1000000);
			int latitud = (int) (location.getLatitude() * 1000000);
			GeoPoint currentLocation = new GeoPoint(latitud, longitud);

			// Get the nearest circuitPoint
			int newDist = circuit.getNearestDistanceFromStart(
					distancesFromStart.get(distancesFromStart.size() - 1),
					currentLocation);
			distancesFromStart.add(newDist);

			// Send the point
			WebInterface.sendParticipationPoint(loggedAthlete.getId(),
					competition.getId(), newDist);
			Log.i(LOG_TAG, "Participation point sent");

			// If has arrived, cancels the upload task
			if (newDist == circuit.getLength()) {
				cancel();
			}
		}
	};

	/**
	 * Generates the bitmap of faces
	 */
	private void generateFaces() {
		this.faces = new HashMap<Integer, Bitmap>();
		this.faces.put(0, BitmapFactory.decodeResource(getResources(),
			 R.drawable.f0));
		athletesMutex.startToReadOrModify();
		for (Athlete ath : athletes) {
			int faceId = getResources().getIdentifier(
					"running.android.app:drawable/f" + ath.getId(),
					null, null);
			if (faceId != 0) {
				Log.d(LOG_TAG, "" + ath.getId() + " has face");
				Bitmap face = BitmapFactory.decodeResource(getResources(),
						faceId);
				faces.put(ath.getId(), face);
			}
		}
		athletesMutex.endReadingOrModifying();
	}

	/**
	 * RUNNABLE for downloading athletes and circuit
	 */
	private final Runnable downloadAthletesCommentsAndCircuit = new Runnable() {
		public void run() {
			Log.i(LOG_TAG, "Download athletes and circuit");

			// Athletes
			WebInterface.addAthletes(athletes, competition);
			Log.i(LOG_TAG, "Athletes downloaded");
			generateFaces();

			sendBroadcast(new Intent(ATHLETES_DOWNLOADED));

			// Circuit
			circuit = WebInterface.getCircuit(competition.getId());
			Log.i(LOG_TAG, "Circuit downloaded");
			sendBroadcast(new Intent(CIRCUIT_DOWNLOADED));

			// Comments
			WebInterface.addComments(comments, competition, -1, commentsMutex, commentsOverlay);
			Log.i(LOG_TAG, "Comments downloaded");
			sendBroadcast(new Intent(COMMENTS_DOWNLOADED));

			// If the competition had already started, only activates the
			// downloading mode
			if (competition.getState() == Competition.TAKING_PLACE) {
				timer = new Timer();
				startSyncDownload();
			}
		}
	};

	public void postComment(final Comment c) {
		new Thread() {
			public void run() {
				WebInterface.postComment(c);
				Log.i(LOG_TAG, "Comment posted");

				int lastIdComment = -1;
				if (!comments.isEmpty()) {
					lastIdComment = comments.get(0).getId();
				}
				// Only donwload if the competition is taking place
				// If taking place it will be done in downloaded task
				if (competition.getState() != Competition.TAKING_PLACE) {
					WebInterface.addComments(comments, competition,
							lastIdComment, commentsMutex, commentsOverlay);
					sendBroadcast(new Intent(COMMENTS_DOWNLOADED));
				}
			}
		}.start();
	}

	public void sendImage(final byte[] photo) {
		new Thread() {
			public void run() {
				Log.i(LOG_TAG, "Send image");

				WebInterface.sendImage(photo);
				Log.i(LOG_TAG, "Image sent");
			}
		}.start();
	}

	public MutexManager getAthletesMutex() {
		return athletesMutex;
	}

	public MutexManager getCommentsMutex() {
		return commentsMutex;
	}

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class WebBinder extends Binder {
		WebService getService() {
			return WebService.this;
		}
	}
}
