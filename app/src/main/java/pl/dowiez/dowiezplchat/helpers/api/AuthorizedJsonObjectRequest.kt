package pl.dowiez.dowiezplchat.helpers.api

import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject
import pl.dowiez.dowiezplchat.helpers.user.UserHelper
import java.util.HashMap

class AuthorizedJsonObjectRequest(
    method: Int,
    uri: String,
    jsonObject: JSONObject?,
    listener: Response.Listener<JSONObject>,
    errorListener: Response.ErrorListener
) : JsonObjectRequest(method, uri, jsonObject, listener, errorListener) {
    override fun getHeaders(): MutableMap<String, String> {
        val newHeaders = HashMap<String, String>()

        super.getHeaders().forEach {
            newHeaders[it.key] = it.value
        }

        newHeaders["Authorization"] = "Bearer ${UserHelper.token}"
        return newHeaders
    }
}