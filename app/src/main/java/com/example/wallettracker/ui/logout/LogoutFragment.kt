package com.example.wallettracker.ui.logout

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.wallettracker.data.session.SessionDAO
import com.example.wallettracker.databinding.FragmentLogoutBinding
import com.example.wallettracker.ui.login.LoginActivity


class LogoutFragment() : Fragment() {
    private var _binding: FragmentLogoutBinding? = null
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentLogoutBinding.inflate(inflater, container, false)

        //log out process
        SessionDAO(this.context).use { sSess ->
            sSess.deleteAll()//clears all sessions
        }
        val intent: Intent = Intent(
            this.context,
            LoginActivity::class.java
        )
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)




        val root: View = binding.root
        return root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}