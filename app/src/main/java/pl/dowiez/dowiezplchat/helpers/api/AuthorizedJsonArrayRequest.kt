package pl.dowiez.dowiezplchat.helpers.api

import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import org.json.JSONArray
import pl.dowiez.dowiezplchat.helpers.user.UserHelper

class AuthorizedJsonArrayRequest(
    method: Int,
    uri: String,
    jsonObject: JSONArray?,
    listener: Response.Listener<JSONArray>,
    errorListener: Response.ErrorListener
) : JsonArrayRequest(method, uri, jsonObject, listener, errorListener) {
    override fun getHeaders(): MutableMap<String, String> {
        val newHeaders = HashMap<String, String>()

        super.getHeaders().forEach {
            newHeaders[it.key] = it.value
        }

        newHeaders["Authorization"] = "Bearer ${UserHelper.token}"
        return newHeaders
    }
}