package net.sourcewalker.smugview.auth;

import net.sourcewalker.smugview.ApiConstants;
import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.kallasoft.smugmug.api.json.AbstractResponse.Error;
import com.kallasoft.smugmug.api.json.v1_2_1.APIVersionConstants;
import com.kallasoft.smugmug.api.json.v1_2_1.login.WithPassword;
import com.kallasoft.smugmug.api.json.v1_2_1.login.WithPassword.WithPasswordResponse;

/**
 * This authenticator handles accounts which connect to SmugMug.
 * 
 * @author Xperimental
 */
public class Authenticator extends AbstractAccountAuthenticator {

    /**
     * Type of account handled by this authenticator.
     */
    public static final String TYPE = "net.sourcewalker.smugview.auth";

    /**
     * Type of authentication token this authenticator can produce.
     */
    public static final String TOKEN_TYPE = TYPE + ".token";

    private final Context context;
    private AccountManager accountManager;

    public Authenticator(Context context) {
        super(context);

        this.context = context;
        this.accountManager = AccountManager.get(context);
    }

    /*
     * (non-Javadoc)
     * @see
     * android.accounts.AbstractAccountAuthenticator#addAccount(android.accounts
     * .AccountAuthenticatorResponse, java.lang.String, java.lang.String,
     * java.lang.String[], android.os.Bundle)
     */
    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response,
            String accountType, String authTokenType,
            String[] requiredFeatures, Bundle options)
            throws NetworkErrorException {
        if (accountType.equals(TYPE) == false) {
            throw new IllegalArgumentException("Invalid account type: "
                    + accountType);
        }
        final Intent intent = new Intent(context, AuthenticationActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
                response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    /*
     * (non-Javadoc)
     * @see
     * android.accounts.AbstractAccountAuthenticator#confirmCredentials(android
     * .accounts.AccountAuthenticatorResponse, android.accounts.Account,
     * android.os.Bundle)
     */
    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response,
            Account account, Bundle options) throws NetworkErrorException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see
     * android.accounts.AbstractAccountAuthenticator#editProperties(android.
     * accounts.AccountAuthenticatorResponse, java.lang.String)
     */
    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response,
            String accountType) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see
     * android.accounts.AbstractAccountAuthenticator#getAuthToken(android.accounts
     * .AccountAuthenticatorResponse, android.accounts.Account,
     * java.lang.String, android.os.Bundle)
     */
    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response,
            Account account, String authTokenType, Bundle options)
            throws NetworkErrorException {
        String username = account.name;
        String password = accountManager.getPassword(account);
        WithPasswordResponse loginResponse = new WithPassword().execute(
                APIVersionConstants.SECURE_SERVER_URL, ApiConstants.APIKEY,
                username, password);
        Bundle result = new Bundle();
        if (loginResponse.isError()) {
            Error error = loginResponse.getError();
            result.putInt(AccountManager.KEY_ERROR_CODE, error.getCode());
            result.putString(AccountManager.KEY_ERROR_MESSAGE,
                    error.getMessage());
        } else {
            result.putString(AccountManager.KEY_ACCOUNT_NAME, username);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN,
                    loginResponse.getSessionID());
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * @see
     * android.accounts.AbstractAccountAuthenticator#getAuthTokenLabel(java.
     * lang.String)
     */
    @Override
    public String getAuthTokenLabel(String authTokenType) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see
     * android.accounts.AbstractAccountAuthenticator#hasFeatures(android.accounts
     * .AccountAuthenticatorResponse, android.accounts.Account,
     * java.lang.String[])
     */
    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response,
            Account account, String[] features) throws NetworkErrorException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see
     * android.accounts.AbstractAccountAuthenticator#updateCredentials(android
     * .accounts.AccountAuthenticatorResponse, android.accounts.Account,
     * java.lang.String, android.os.Bundle)
     */
    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response,
            Account account, String authTokenType, Bundle options)
            throws NetworkErrorException {
        // TODO Auto-generated method stub
        return null;
    }

}
