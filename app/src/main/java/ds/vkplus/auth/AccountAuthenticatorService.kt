package ds.vkplus.auth

import android.app.Service
import android.content.Intent
import android.os.IBinder

import android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT


public class AccountAuthenticatorService : Service() {

    private val AUTHENTICATOR: AccountAuthenticator by lazy { AccountAuthenticator(this) }

    override fun onBind(intent: Intent): IBinder? {
        return if (intent.action == ACTION_AUTHENTICATOR_INTENT) AUTHENTICATOR.iBinder else null
    }

}

