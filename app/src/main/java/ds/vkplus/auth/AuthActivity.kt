package ds.vkplus.auth

import android.accounts.Account
import android.accounts.AccountAuthenticatorActivity
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.URLUtil
import android.webkit.WebView
import android.webkit.WebViewClient
import ds.vkplus.Constants
import ds.vkplus.PrivateConstants
import ds.vkplus.network.RestService
import ds.vkplus.utils.L

import java.util.regex.Matcher
import java.util.regex.Pattern

public class AuthActivity : AccountAuthenticatorActivity() {

    lateinit private var webView: WebView


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        webView = WebView(this)
        setContentView(webView)
        title = "Authentication"

        webView.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                L.v("URL=" + url)
                val m = PATTERN_TOKEN_URL.matcher(url)
                if (m.matches()) {
                    finishLogin(m.group(1), m.group(2), m.group(3))
                }
                return !URLUtil.isNetworkUrl(url)
            }

        })

        webView.settings.javaScriptEnabled = true
        webView.loadUrl(AUTH_URL)

    }


    private fun finishLogin(token: String, expires: String, userId: String) {
        val accountType = intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)
        val tokenType = Constants.AUTH_TOKEN_TYPE
        val accountName = Constants.ACCOUNT_NAME
        val am = AccountManager.get(this)
        var account = AccountHelper.instance.getAccount()
        if (account == null) {
            account = Account(accountName, accountType)
            am.addAccountExplicitly(account, null, null)
        }

        am.setAuthToken(account, tokenType, token)
        am.setUserData(account, Constants.KEY_USERID, userId)
        am.setUserData(account, Constants.KEY_EXPIRES, expires)

        val intent = Intent()
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, accountName)
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType)
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, token)
        setAccountAuthenticatorResult(intent.extras)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }


    override fun onDestroy() {
        super.onDestroy()
        L.v("doing notify()")
        synchronized (AccountHelper.instance) {
			(AccountHelper.instance as Object).notify()
        }
    }

    companion object {

        val AUTH_URL: String = "https://oauth.vk.com/authorize?client_id=${PrivateConstants.VK_APP_ID}" +
                "&scope=groups,friends,wall,video,audio,pages,messages&redirect_uri=" +
                "https://oauth.vk.com/blank.html&display=mobile&v=${RestService.API_VERSION}&response_type=token"

        val PATTERN_TOKEN_URL: Pattern = Pattern.compile("""^https://oauth\.vk\.com/blank\.html#access_token=(.+)&expires_in=(\d+)&user_id=(\d+)$""")
    }
}
