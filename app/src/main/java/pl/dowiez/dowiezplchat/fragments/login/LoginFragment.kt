package pl.dowiez.dowiezplchat.fragments.login

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.ColorInt
import com.android.volley.VolleyError
import kotlinx.android.synthetic.main.fragment_login.*
import pl.dowiez.dowiezplchat.MainActivity
import pl.dowiez.dowiezplchat.R
import pl.dowiez.dowiezplchat.databinding.FragmentLoginBinding
import pl.dowiez.dowiezplchat.fragments.conversations.ConversationFragment
import pl.dowiez.dowiezplchat.helpers.api.ApiHelper
import pl.dowiez.dowiezplchat.helpers.api.ILoginCallback

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
//
//    class LoginViewModel : ViewModel() {
//
//    }

    companion object {
        //        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            LoginFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
        fun newInstance() = LoginFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
        @ColorInt val color = typedValue.data
        requireActivity().window.statusBarColor = color
        binding = FragmentLoginBinding.bind(view)

        loginBT.setOnClickListener { this@LoginFragment.login() }
    }

    private fun login() {
        loginEmailET.isFocusable = false
        loginPasswordET.isFocusable = false
        loginBT.isEnabled = false

        ApiHelper.login(
            requireActivity(),
            loginEmailET.text.toString(),
            loginPasswordET.text.toString(),
            object : ILoginCallback {
                override fun onSuccess() {
                    Log.i("LoginActivity", "Login successfull")

                    Toast.makeText(activity, "Login successfull", Toast.LENGTH_SHORT).show()

                    (requireActivity() as MainActivity).replaceFragment(ConversationFragment())
                }
                override fun onError(error: VolleyError) {
                    Log.e("LoginActivity", "Failed to login")
                    loginEmailET.isFocusable = true
                    loginEmailET.isFocusableInTouchMode = true
                    loginPasswordET.isFocusable = true
                    loginPasswordET.isFocusableInTouchMode = true
                    loginBT.isEnabled = true

                    Toast.makeText(activity, "Failed to login", Toast.LENGTH_SHORT).show()
                }
            })
    }
}