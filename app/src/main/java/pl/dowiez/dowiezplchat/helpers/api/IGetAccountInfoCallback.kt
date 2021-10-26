package pl.dowiez.dowiezplchat.helpers.api

import pl.dowiez.dowiezplchat.data.entities.Account

interface IGetAccountInfoCallback : IApiCallback {
    fun onSuccess(account: Account)
}