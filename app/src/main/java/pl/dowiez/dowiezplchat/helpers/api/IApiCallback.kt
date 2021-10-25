package pl.dowiez.dowiezplchat.helpers.api

import com.android.volley.VolleyError

interface IApiCallback {
    fun onError(error: VolleyError)
}