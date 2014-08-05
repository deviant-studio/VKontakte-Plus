package ds.vkplus.auth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import ds.vkplus.Constants;
import ds.vkplus.PrivateConstants;
import ds.vkplus.utils.L;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthActivity extends AccountAuthenticatorActivity {

	public static final String AUTH_URL = "https://oauth.vk.com/authorize?" +
			"client_id=" + PrivateConstants.VK_APP_ID + "&" +
			"scope=groups,friends,wall&" +
			"redirect_uri=https://oauth.vk.com/blank.html&" +
			"display=mobile&" +
			"v=5.23&" +
			"response_type=token";

	public static final Pattern PATTERN_TOKEN_URL = Pattern.compile("^https://oauth\\.vk\\.com/blank\\.html#access_token=(.+)&expires_in=(\\d+)&user_id=(\\d+)$");

	private WebView webView;


	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(final Bundle icicle) {
		super.onCreate(icicle);

		webView = new WebView(this);
		setContentView(webView);
		setTitle("Authentication");

		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				L.v("URL=" + url);
				Matcher m = PATTERN_TOKEN_URL.matcher(url);
				if (m.matches()) {
					finishLogin(m.group(1), m.group(2), m.group(3));
				}
				return !URLUtil.isNetworkUrl(url);
			}

		});

		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl(AUTH_URL);

	}


	private void finishLogin(final String token, final String expires, String userId) {
		String accountType = getIntent().getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
		String tokenType = Constants.AUTH_TOKEN_TYPE;
		String accountName = Constants.ACCOUNT_NAME;
		AccountManager am = AccountManager.get(this);
		Account account = AccountHelper.getInstance().getAccount();
		if (account == null) {
			account = new Account(accountName, accountType);
			am.addAccountExplicitly(account, null, null);
		}

		am.setAuthToken(account, tokenType, token);
		am.setUserData(account, Constants.KEY_USERID, userId);
		am.setUserData(account, Constants.KEY_EXPIRES, expires);

		final Intent intent = new Intent();
		intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, accountName);
		intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
		intent.putExtra(AccountManager.KEY_AUTHTOKEN, token);
		setAccountAuthenticatorResult(intent.getExtras());
		setResult(RESULT_OK, intent);
		finish();
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		L.v("doing notify()");
		synchronized (AccountHelper.getInstance()) {
			AccountHelper.getInstance().notify();
		}
	}
}
