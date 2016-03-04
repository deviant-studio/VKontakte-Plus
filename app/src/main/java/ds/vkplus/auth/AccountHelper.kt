package ds.vkplus.auth

import android.accounts.*
import android.accounts.AccountManager.*
import android.app.Activity
import android.content.Context
import android.content.Intent
import ds.vkplus.App
import ds.vkplus.Constants
import ds.vkplus.utils.L
import ds.vkplus.utils.post
import java.io.IOException

public class AccountHelper private constructor(var ctx: Context) {
    val am: AccountManager by lazy { AccountManager.get(ctx.applicationContext) }


    /**
     * Blocking call

     * @param a
     * *
     * @return
     * *
     * @throws AuthenticatorException
     * *
     * @throws OperationCanceledException
     * *
     * @throws IOException
     */
    @Throws(AuthenticatorException::class, OperationCanceledException::class, IOException::class)
    public fun getToken(a: Activity): String {
        val f = am.getAuthTokenByFeatures(Constants.ACCOUNT_TYPE, Constants.AUTH_TOKEN_TYPE,
                null, a, null, null, null, null)
        val b = f.result
        val token = b.getString(KEY_AUTHTOKEN)
        return token
    }


    /**
     * Can be safely call from UI thread. Use it only when you have no activity object to pass

     * @return can be null :(
     * *
     * @throws AuthenticatorException
     * *
     * @throws OperationCanceledException
     * *
     * @throws IOException
     */
    @Throws(AuthenticatorException::class, OperationCanceledException::class, IOException::class)
    public fun getToken(): String? {
        val account = getAccount()
        if (account != null) {
            val token = am.peekAuthToken(account, Constants.AUTH_TOKEN_TYPE)
            if (token == null) {
                startAuthActivity()
            }
            return token
        } else {
            val f = am.addAccount(Constants.ACCOUNT_TYPE, Constants.AUTH_TOKEN_TYPE, null, null, null, null, null)
            val b = f.result
            val i = b.getParcelable<Intent>(KEY_INTENT)
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            App.instance.startActivity(i)
            return null
        }
    }


    public fun invalidateToken() {
        val a = getAccount()
        if (a != null) {
            val token = am.peekAuthToken(a, Constants.AUTH_TOKEN_TYPE)
            am.invalidateAuthToken(Constants.ACCOUNT_TYPE, token)
            L.i("token invalidated")
        } else {
            L.e("cant invalidate token. account is null")
        }
    }


    public fun getAccount(): Account? {
        val accs = am.getAccountsByType(Constants.ACCOUNT_TYPE)
        if (accs.size == 0) {
            L.w("auth: no accounts found")
            return null
        }
        return accs[0]
    }


    public fun prepareAuthActivity(accountType: String, response: AccountAuthenticatorResponse?): Intent {
        val intent = Intent(ctx, AuthActivity::class.java)
        //intent.putExtra(AccountManager.KEY_tACCOUNT_TYPEAuthActivity.KEY_AUTH_TOKEN_TYPE, authTokenType);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType)
        intent.putExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        return intent
    }


    public fun refreshToken(): String {
        synchronized (this) {
            post { startAuthActivity() }
            L.v("doing wait()")
            (this as Object).wait()
            return peekToken()
        }
    }


    private fun startAuthActivity() {
        val i = prepareAuthActivity(Constants.ACCOUNT_TYPE, null)
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        App.instance.startActivity(i)
    }


    public fun peekToken(): String {
        try {
            return am.peekAuthToken(getAccount(), Constants.AUTH_TOKEN_TYPE)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            return ""
        }

    }

    companion object {
        val instance: AccountHelper by lazy { AccountHelper(App.instance) }
    }
}
