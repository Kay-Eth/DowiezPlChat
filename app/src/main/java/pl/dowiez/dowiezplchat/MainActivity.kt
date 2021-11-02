package pl.dowiez.dowiezplchat

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import pl.dowiez.dowiezplchat.fragments.login.LoginFragment
import pl.dowiez.dowiezplchat.databinding.ActivityMainBinding
import pl.dowiez.dowiezplchat.fragments.chat.ChatFragment
import pl.dowiez.dowiezplchat.fragments.conversations.ConversationFragment
import pl.dowiez.dowiezplchat.helpers.user.UserHelper
import pl.dowiez.dowiezplchat.service.ChatService

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    companion object {

        const val REQUEST_CODE_INTERNET = 1

        private var chatService: ChatService? = null
        private var serviceBound: Boolean = false

        const val INTENT_CONVERSATION_ID_KEY = "ConversationId"

        fun getService() : ChatService? {
            return chatService
        }

        fun isServiceBound() : Boolean {
            return serviceBound
        }

        private var activityVisible = false
        fun isActivityVisible(): Boolean {
            return activityVisible
        }

        private var conversationId: String? = null
        fun getConversationId(): String? {
            return conversationId
        }

        fun setConversationId(convId: String?) {
            conversationId = convId
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ChatService.ChatBinder
            chatService = binder.getService()
            serviceBound = true
            Log.i("MainActivity", "Service Connected")

            if (UserHelper.token.isEmpty())
                loadFragment(LoginFragment())
            else
            {
                val con = intent.getStringExtra(INTENT_CONVERSATION_ID_KEY)
                if (con != null) {
                    loadFragment(ChatFragment.newInstance(con))
                } else {
                    loadFragment(ConversationFragment())
                }
            }

        }

        override fun onServiceDisconnected(name: ComponentName?) {
            chatService = null
            serviceBound = false
            Log.i("MainActivity", "Service Disconnected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("MainActivity", "onCreate")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissions()

        UserHelper.readToken(this)

        if (chatService == null) {
            Log.i("MainActivity", "Bound service")
            Intent(this, ChatService::class.java).also { intent ->
                bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }

            val intent = Intent(this, ChatService::class.java)
            startService(intent)
        }

        Intent(this, ChatService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("MainActivity", "onCreate")

        unbindService(connection)
        serviceBound = false
    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            add(R.id.container, fragment)
            addToBackStack("Traslalsrlas")
        }
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.container, fragment)
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, Array<String>(1) { android.Manifest.permission.INTERNET }, REQUEST_CODE_INTERNET)
        } else {
            Log.i("MainActivity", "Already granted - INTERNET")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_INTERNET) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("Permission", "INTERNET: Granted")
            }
            else
            {
                ActivityCompat.requestPermissions(this@MainActivity, Array<String>(1) { android.Manifest.permission.INTERNET }, REQUEST_CODE_INTERNET)
            }
        }
    }

    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount
        Log.i("onBackPressed", "$count")

        if (count == 1) {
            finish()
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    override fun onPause() {
        super.onPause()
        activityVisible = false
    }

    override fun onResume() {
        super.onResume()
        activityVisible = true
    }
}