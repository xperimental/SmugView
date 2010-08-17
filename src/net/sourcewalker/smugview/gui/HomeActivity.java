package net.sourcewalker.smugview.gui;

import net.sourcewalker.smugview.ApiConstants;
import net.sourcewalker.smugview.R;
import net.sourcewalker.smugview.data.Prefs;
import net.sourcewalker.smugview.parcel.Extras;
import net.sourcewalker.smugview.parcel.LoginResult;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kallasoft.smugmug.api.json.v1_2_0.APIVersionConstants;
import com.kallasoft.smugmug.api.json.v1_2_0.login.WithPassword;
import com.kallasoft.smugmug.api.json.v1_2_0.login.WithPassword.WithPasswordResponse;

public class HomeActivity extends Activity implements OnClickListener {

    public static final int LOGIN_DIALOG = 100;
    private EditText username;
    private EditText password;
    private Button cancelButton;
    private Button loginButton;
    private Prefs prefs;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        prefs = new Prefs(this);

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        cancelButton = (Button) findViewById(R.id.cancelbutton);
        cancelButton.setOnClickListener(this);
        loginButton = (Button) findViewById(R.id.loginbutton);
        loginButton.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String user = prefs.getUsername();
        if (user != null) {
            username.setText(user);
            password.setText(prefs.getPassword());

            startLogin();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.cancelbutton:
            finish();
            break;
        case R.id.loginbutton:
            startLogin();
            break;
        }
    }

    private void startLogin() {
        new LoginTask().execute(username.getText().toString(), password
                .getText().toString());
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case LOGIN_DIALOG:
            return ProgressDialog.show(this, getString(R.string.app_name),
                    getString(R.string.loginprogress), true, false);
        default:
            return null;
        }
    }

    private class LoginTask extends AsyncTask<String, Void, LoginResult> {

        @Override
        protected LoginResult doInBackground(String... params) {
            LoginResult result = new LoginResult();
            try {
                String username = params[0];
                String password = params[1];
                WithPasswordResponse response = new WithPassword().execute(
                        APIVersionConstants.SECURE_SERVER_URL,
                        ApiConstants.APIKEY, username, password);
                if (!response.isError()) {
                    result.setSuccessful(true);
                    result.setId(response.getUserID());
                    result.setUsername(username);
                    result.setAccountType(response.getAccountType());
                    result.setSession(response.getSessionID());
                    prefs.setUsername(username);
                    prefs.setPassword(password);
                } else {
                    prefs.setUsername(null);
                    prefs.setPassword(null);
                }
            } catch (RuntimeException e) {
                Log.e("LoginTask", "Error logging in: " + e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(LOGIN_DIALOG);
        }

        @Override
        protected void onPostExecute(LoginResult result) {
            dismissDialog(LOGIN_DIALOG);
            if (result.isSuccessful()) {
                Intent albums = new Intent(getBaseContext(),
                        AlbumListActivity.class);
                albums.putExtra(Extras.EXTRA_LOGIN, result);
                startActivity(albums);
                finish();
            } else {
                Toast.makeText(getBaseContext(), R.string.loginfailed,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

}
