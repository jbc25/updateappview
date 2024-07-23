package com.triskelapps.updateappviewsample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.triskelapps.simpleappupdate.SimpleAppUpdate
import com.triskelapps.simpleappupdate.SimpleAppUpdateView
import com.triskelapps.updateappviewsample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var simpleAppUpdate: SimpleAppUpdate
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        simpleAppUpdate = SimpleAppUpdate(this)

        configureManualCheck()

        checkUpdateBarStyle(false)

        binding.tvAppVersion.text = BuildConfig.VERSION_NAME
    }

    private fun configureManualCheck() {
        binding.btnCheckForUpdate.setOnClickListener {
            checkForUpdate()
        }
    }

    private fun checkForUpdate() {

        binding.btnCheckForUpdate.setText(R.string.checking)

        simpleAppUpdate.setUpdateAvailableListener {
            binding.tvManualCheck.setText(R.string.update_available)
            binding.btnCheckForUpdate.setText(R.string.launch_update)
            binding.btnCheckForUpdate.setOnClickListener {
                binding.tvManualCheck.setText(R.string.manual_ckeck)
                binding.btnCheckForUpdate.setText(R.string.check_for_update)
                simpleAppUpdate.launchUpdate()
            }
        }

        simpleAppUpdate.setErrorListener { error ->
            binding.tvManualCheck.text = "Error: $error"
            binding.btnCheckForUpdate.setText(R.string.check_for_update)
            binding.btnCheckForUpdate.setOnClickListener { checkForUpdate() }

        }

        simpleAppUpdate.setFinishListener {
        }

        simpleAppUpdate.checkUpdateAvailable()
    }

    private fun checkUpdateBarStyle(showBar: Boolean) {
        binding.simpleAppUpdateView.visibility = if (showBar) View.VISIBLE else View.GONE
    }
}