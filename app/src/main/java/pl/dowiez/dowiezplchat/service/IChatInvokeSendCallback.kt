package pl.dowiez.dowiezplchat.service

interface IChatInvokeSendCallback {
    fun onSuccess()
    fun onError(e: Throwable)
}