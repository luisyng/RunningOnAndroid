package running.android.app;

import org.json.JSONException;

import running.android.web.WebInterface;

import running.android.app.R;
import running.domain.Athlete;
import running.json.JSONAdapter;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {

	private Button loginButton;
	private TextView usernameTextView;
	private TextView passwordTextView;
	private TextView wrongLoginTextView;
	private Athlete loggedAthlete;
	private final Handler handler = new Handler();
	private Context context;
	private static final String LOGGED_ATHLETE = "logged_athlete";
	private static final String PREFERENCES_FILE = "main";
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		context = getApplicationContext();

		usernameTextView = (TextView) findViewById(R.id.username);
		passwordTextView = (TextView) findViewById(R.id.password);
		wrongLoginTextView = (TextView) findViewById(R.id.wrong_login);
		loginButton = (Button) findViewById(R.id.login_button);
		loginButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login_button:
			loginButton.setEnabled(false);
			wrongLoginTextView.setVisibility(View.INVISIBLE);
			new Thread(login).start();
		}
	}
	
	private final Runnable login = new Runnable() {
		public void run() {
			loggedAthlete = WebInterface.logIn(usernameTextView.getText().toString(), 
					passwordTextView.getText().toString());
			handler.post(loggedIn);
		}
	};
	
	private final Runnable loggedIn = new Runnable() {
		public void run() {
			if(loggedAthlete == null) {
				loginButton.setEnabled(true);
				wrongLoginTextView.setVisibility(View.VISIBLE);
			}
			else {		
				try {
					getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE).edit().
					putString(LOGGED_ATHLETE, 
							JSONAdapter.athleteToJSON(loggedAthlete).toString()).commit();
				} catch (JSONException e) {
					Log.e("LOGIN", "Error parsing logged athlete");
				}
				finish();
				Toast toast = Toast.makeText(context, "Sesión iniciada correctamente", Toast.LENGTH_LONG);
				toast.show();			
			}
		}
	};
}
