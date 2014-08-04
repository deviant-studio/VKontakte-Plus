package ds.vkplus.auth;

import android.accounts.*;
import android.content.Context;
import android.os.Bundle;
import ds.vkplus.utils.L;

import static android.accounts.AccountManager.KEY_INTENT;

public class AccountAuthenticator extends AbstractAccountAuthenticator {

	private final Context ctx;


	public AccountAuthenticator(Context context) {
		super(context);
		this.ctx = context;
	}


	/*
	 * The user has requested to add a new account to the system. We return an intent that will launch our login screen
	 * if the user has not logged in yet, otherwise our activity will just pass the user's credentials on to the account
	 * manager.
	 */
	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType,
			String[] requiredFeatures, Bundle options) throws NetworkErrorException {
		L.v("auth: add account");

		final Bundle bundle = new Bundle();
		bundle.putParcelable(KEY_INTENT, AccountHelper.getInstance().prepareAuthActivity(accountType, response));
		return bundle;

	}


	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
		L.v("auth: Attempting to get authToken");
		final AccountManager am = AccountManager.get(ctx);

		String token = am.peekAuthToken(account, authTokenType);

		final Bundle result = new Bundle();
		if (token == null) {
			L.w("auth: token not fiund");
			result.putParcelable(KEY_INTENT, AccountHelper.getInstance().prepareAuthActivity(account.type, response));
		} else {
			L.v("auth: found cached token");
			result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
			result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
			result.putString(AccountManager.KEY_AUTHTOKEN, token);
		}


		return result;
	}


	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) {
		L.v("auth: confirmCredentials");
		return null;
	}


	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
		L.v("auth: editProperties");
		return null;
	}


	@Override
	public String getAuthTokenLabel(String authTokenType) {
		L.v("auth: getAuthTokenLabel");
		return null;
	}


	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
		L.v("auth: hasFeatures");
		return null;
	}


	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) {
		L.v("auth: updateCredentials");
		return null;
	}
}
