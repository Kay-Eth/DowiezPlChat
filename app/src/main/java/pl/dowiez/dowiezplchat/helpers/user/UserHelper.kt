package pl.dowiez.dowiezplchat.helpers.user

import android.content.Context
import android.util.Log
import com.android.volley.VolleyError
import pl.dowiez.dowiezplchat.helpers.api.ApiHelper
import pl.dowiez.dowiezplchat.helpers.api.IMyAccountCallback
import java.io.File

object UserHelper {
    private const val TOKEN_FILE_PATH = "token.bin"

    var accountId: String = String()
    var email: String = String()
    var firstName: String = String()
    var lastName: String = String()
    var token: String = String()

    fun readToken(context: Context) {
        val file = File(context.filesDir, TOKEN_FILE_PATH)
        token = if (file.exists())
            file.readText().trim()
        else
            String()
    }

    fun saveToken(context: Context) {
        context.openFileOutput(TOKEN_FILE_PATH, Context.MODE_PRIVATE).use {
            it.write(token.toByteArray())
        }
    }

    fun loadAccountData(context: Context) {
        ApiHelper.getMyAccountInfo(context, object : IMyAccountCallback {
            override fun onSuccess() {
                Log.i("UserHelper", accountId)
                Log.i("UserHelper", email)
                Log.i("UserHelper", firstName)
                Log.i("UserHelper", lastName)
            }

            override fun onError(error: VolleyError) {
                Log.e("UserHelper", "Failed to get account data")
            }
        })
    }
}