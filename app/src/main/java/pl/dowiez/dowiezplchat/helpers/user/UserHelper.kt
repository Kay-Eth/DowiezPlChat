package pl.dowiez.dowiezplchat.helpers.user

import android.content.Context
import android.util.Log
import com.android.volley.VolleyError
import pl.dowiez.dowiezplchat.helpers.api.ApiHelper
import pl.dowiez.dowiezplchat.helpers.api.IMyAccountCallback
import java.io.File

object UserHelper {
    private const val TOKEN_FILE_PATH = "token.bin"
    private const val ID_FILE_PATH = "id.bin"

    var accountId: String = String()
    var email: String = String()
    var firstName: String = String()
    var lastName: String = String()
    var token: String = String()

    fun readToken(context: Context) {
        readId(context)

        val file = File(context.filesDir, TOKEN_FILE_PATH)
        token = if (file.exists())
            file.readText().trim()
        else
            String()
    }

    fun saveToken(context: Context) {
        saveId(context)

        context.openFileOutput(TOKEN_FILE_PATH, Context.MODE_PRIVATE).use {
            it.write(token.toByteArray())
        }
    }

    fun readId(context: Context) {
        val file = File(context.filesDir, ID_FILE_PATH)
        accountId = if (file.exists())
            file.readText().trim()
        else
            String()
    }

    fun saveId(context: Context) {
        context.openFileOutput(ID_FILE_PATH, Context.MODE_PRIVATE).use {
            it.write(accountId.toByteArray())
        }
    }

    fun loadAccountData(context: Context) {
        readId(context)

        ApiHelper.getMyAccountInfo(context, object : IMyAccountCallback {
            override fun onSuccess() {
                Log.i("UserHelper", accountId)
                Log.i("UserHelper", email)
                Log.i("UserHelper", firstName)
                Log.i("UserHelper", lastName)

                saveId(context)
            }

            override fun onError(error: VolleyError) {
                Log.e("UserHelper", "Failed to get account data")
            }
        })
    }
}