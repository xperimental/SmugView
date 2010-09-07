package net.sourcewalker.smugview.auth;

import net.sourcewalker.smugview.ApiConstants;
import net.sourcewalker.smugview.R;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kallasoft.smugmug.api.json.v1_2_1.APIVersionConstants;
import com.kallasoft.smugmug.api.json.v1_2_1.login.WithPassword;
import com.kallasoft.smugmug.api.json.v1_2_1.login.WithPassword.WithPasswordResponse;

/**
 * Activity which is displayed to create a new account for SmugMug. It verifies
 * the credentials with the server and creates the account on the phone if
 * successful.
 * 
 * @author Xperimental
 */
public class AuthenticationActivity extends AccountAuthenticatorActivity
        implements OnClickListener {

    private static final int DIALOG_LOGIN = 100;

    private EditText usernameField;
    private EditText passwordField;
    private Button cancelButton;
    private Button loginButton;
    private LoginTask loginTask;

    /*
     * (non-Javadoc)
     * @see
     * android.accounts.AccountAuthenticatorActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.authenticator);

        usernameField = (EditText) findViewById(R.id.auth_username);
        passwordField = (EditText) findViewById(R.id.auth_password);
        cancelButton = (Button) findViewById(R.id.auth_cancelbutton);
        cancelButton.setOnClickListener(this);
        loginButton = (Button) findViewById(R.id.auth_loginbutton);
        loginButton.setOnClickListener(this);
    }

    /*
     * (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.auth_cancelbutton:
            finish();
            break;
        case R.id.auth_loginbutton:
            startLogin();
            break;
        }
    }

    private void startLogin() {
        if (loginTask == null) {
            loginTask = new LoginTask();
            String username = usernameField.getText().toString();
            String password = passwordField.getText().toString();
            loginTask.execute(username, password);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_LOGIN:
            return ProgressDialog.show(this, getString(R.string.app_name),
                    getString(R.string.loginprogress), true, false);
        default:
            return null;
        }
    }

    public void createAccount() {
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();
        Account account = new Account(username, Authenticator.TYPE);
        AccountManager accountManager = AccountManager.get(this);
        boolean accountCreated = accountManager.addAccountExplicitly(account,
                password, null);
        if (accountCreated) {
            Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, username);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE,
                    Authenticator.TYPE);
            setAccountAuthenticatorResult(result);
            finish();
        } else {
            Toast.makeText(this, R.string.auth_createfailed, Toast.LENGTH_LONG)
                    .show();
        }
    }

    private class LoginTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            Boolean result = Boolean.FALSE;
            try {
                String username = params[0];
                String password = params[1];
                WithPasswordResponse response = new WithPassword().execute(
                        APIVersionConstants.SECURE_SERVER_URL,
                        ApiConstants.APIKEY, username, password);
                result = !response.isError();
            } catch (RuntimeException e) {
                Log.e("LoginTask", "Error logging in: " + e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(DIALOG_LOGIN);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            dismissDialog(DIALOG_LOGIN);
            loginTask = null;
            if (result) {
                createAccount();
            } else {
                Toast.makeText(getBaseContext(), R.string.loginfailed,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

}
