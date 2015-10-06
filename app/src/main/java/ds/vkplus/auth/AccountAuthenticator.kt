package ds.vkplus.auth

import android.accounts.*
import android.content.Context
import android.os.Bundle
import ds.vkplus.utils.L

import android.accounts.AccountManager.KEY_INTENT

public class AccountAuthenticator(private val ctx: Context) : AbstractAccountAuthenticator(ctx) {


    /*
	 * The user has requested to add a new account to the system. We return an intent that will launch our login screen
	 * if the user has not logged in yet, otherwise our activity will just pass the user's credentials on to the account
	 * manager.
	 */
    @Throws(NetworkErrorException::class)
    override fun addAccount(response: AccountAuthenticatorResponse, accountType: String, authTokenType: String,
                            requiredFeatures: Array<String>, options: Bundle): Bundle {
        L.v("auth: add account")

        val bundle = Bundle()
        bundle.putParcelable(KEY_INTENT, AccountHelper.instance.prepareAuthActivity(accountType, response))
        return bundle

    }


    @Throws(NetworkErrorException::class)
    override fun getAuthToken(response: AccountAuthenticatorResponse, account: Account, authTokenType: String, options: Bundle): Bundle {
        L.v("auth: Attempting to get authToken")
        val am = AccountManager.get(ctx)

        val token = am.peekAuthToken(account, authTokenType)

        val result = Bundle()
        if (token == null) {
            L.w("auth: token not fiund")
            result.putParcelable(KEY_INTENT, AccountHelper.instance.prepareAuthActivity(account.type, response))
        } else {
            L.v("auth: found cached token")
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
            result.putString(AccountManager.KEY_AUTHTOKEN, token)
        }


        return result
    }


    override fun confirmCredentials(response: AccountAuthenticatorResponse, account: Account, options: Bundle): Bundle? = null

    override fun editProperties(response: AccountAuthenticatorResponse, accountType: String): Bundle? = null

    override fun getAuthTokenLabel(authTokenType: String): String? = null

    @Throws(NetworkErrorException::class)
    override fun hasFeatures(response: AccountAuthenticatorResponse, account: Account, features: Array<String>): Bundle? = null

    override fun updateCredentials(response: AccountAuthenticatorResponse, account: Account, authTokenType: String, options: Bundle): Bundle? = null
}
