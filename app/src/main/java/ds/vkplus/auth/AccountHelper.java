package ds.vkplus.auth;

import android.accounts.*;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import ds.vkplus.App;
import ds.vkplus.Constants;
import ds.vkplus.utils.L;
import ds.vkplus.utils.Utils;

import java.io.IOException;

import static android.accounts.AccountManager.*;

@SuppressWarnings("UnnecessaryLocalVariable")
public class AccountHelper {

	private static AccountHelper instance;

	Context ctx;
	private AccountManager am;


	private AccountHelper(final Context c) {
		ctx = c;
	}


	public static AccountHelper getInstance() {
		if (instance == null)
			instance = new AccountHelper(App.instance());
		return instance;
	}


	/**
	 * Blocking call
	 *
	 * @param a
	 * @return
	 * @throws AuthenticatorException
	 * @throws OperationCanceledException
	 * @throws IOException
	 */
	public String getToken(Activity a) throws AuthenticatorException, OperationCanceledException, IOException {
		AccountManagerFuture<Bundle> f = getAM().getAuthTokenByFeatures(Constants.ACCOUNT_TYPE, Constants.AUTH_TOKEN_TYPE,
				null, a, null, null, null, null);
		Bundle b = f.getResult();
		String token = b.getString(KEY_AUTHTOKEN);
		return token;
	}


	/**
	 * Can be safely call from UI thread. Use it only when you have no activity object to pass
	 *
	 * @return can be null :(
	 * @throws AuthenticatorException
	 * @throws OperationCanceledException
	 * @throws IOException
	 */
	public String getToken() throws AuthenticatorException, OperationCanceledException, IOException {
		Account account = getAccount();
		if (account != null) {
			String token = getAM().peekAuthToken(account, Constants.AUTH_TOKEN_TYPE);
			if (token == null) {
				startAuthActivity();
			}
			return token;
		} else {
			AccountManagerFuture<Bundle> f = getAM().addAccount(Constants.ACCOUNT_TYPE, Constants.AUTH_TOKEN_TYPE, null, null, null, null, null);
			Bundle b = f.getResult();
			Intent i = b.getParcelable(KEY_INTENT);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			App.instance().startActivity(i);
			return null;
		}
	}


	public void invalidateToken() {
		Account a = getAccount();
		if (a != null) {
			final String token = getAM().peekAuthToken(a, Constants.AUTH_TOKEN_TYPE);
			getAM().invalidateAuthToken(Constants.ACCOUNT_TYPE, token);
			L.i("token invalidated");
		} else {
			L.e("cant invalidate token. account is null");
		}
	}


	public Account getAccount() {
		Account[] accs = getAM().getAccountsByType(Constants.ACCOUNT_TYPE);
		if (accs.length == 0) {
			L.w("auth: no accounts found");
			return null;
		}
		return accs[0];
	}


	public AccountManager getAM() {
		if (am == null)
			am = AccountManager.get(ctx);

		return am;
	}


	public Intent prepareAuthActivity(String accountType, AccountAuthenticatorResponse response) {
		final Intent intent = new Intent(ctx, AuthActivity.class);
		//intent.putExtra(AccountManager.KEY_tACCOUNT_TYPEAuthActivity.KEY_AUTH_TOKEN_TYPE, authTokenType);
		intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
		intent.putExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
		return intent;
	}


	public String refreshToken() throws InterruptedException {
		synchronized (this) {
			Utils.post(this::startAuthActivity);
			L.v("doing wait()");
			this.wait(); // unlocks myRunable while waiting
			final String token = getAM().peekAuthToken(getAccount(), Constants.AUTH_TOKEN_TYPE);
			return token;
		}
	}


	private void startAuthActivity() {
		Intent i = prepareAuthActivity(Constants.ACCOUNT_TYPE, null);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		App.instance().startActivity(i);
	}
}
