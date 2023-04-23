package com.denizk0461.studip.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.denizk0461.studip.BuildConfig
import com.denizk0461.studip.activity.FetcherActivity
import com.denizk0461.studip.databinding.FragmentSettingsBinding

/**
 * User-facing fragment view that is used to change app settings.
 */
class SettingsFragment : Fragment() {

    // Nullable view binding reference
    private var _binding: FragmentSettingsBinding? = null

    /*
     * Non-null reference to the view binding. This property is only valid between onCreateView and
     * onDestroyView.
     */
    private val binding get() = _binding!!

    // Click counter on the app version button
    private var appVersionClick = 0

    // 222
    private val mysteryLink = "https://www.youtube.com/watch?v=nhIQMCXJzLI"

    // Instantiate the view binding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Launch the Stud.IP schedule fetcher activity
        binding.buttonRefreshSchedule.setOnClickListener {
            launchWebView()
        }

        // Set click listener for the app version button
        binding.buttonAppVersion.setOnClickListener {
            when (appVersionClick) {
                22 -> {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(mysteryLink)))
                    appVersionClick = 0
                }
                else -> appVersionClick += 1
            }
        }

        /*
         * Display the app's version as set in the build.gradle. Also display if the app is a
         * development version.
         */
        @SuppressLint("SetTextI18n")
        binding.appVersionText.text = "${BuildConfig.VERSION_NAME}-${if (BuildConfig.DEBUG) "dev" else "release"}"
    }

    // Invalidate the view binding
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Launch the Stud.IP schedule fetcher activity.
     */
    private fun launchWebView() {
        startActivity(Intent(context, FetcherActivity::class.java))
    }
}