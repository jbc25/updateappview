package com.triskelapps.updateappviewsample

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.common.util.concurrent.ListenableFuture
import com.triskelapps.simpleappupdate.SimpleAppUpdate
import com.triskelapps.updateappviewsample.databinding.ActivityMainBinding
import java.util.concurrent.ExecutionException


class MainActivity : AppCompatActivity() {

    private val TAG: String = MainActivity::class.java.simpleName

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

        binding.btnWorkerStatus.setOnClickListener {
            binding.btnWorkerStatus.text = simpleAppUpdate.getWorkerStatus()?.toString()
        }

        binding.btnCancelWorker.setOnClickListener {
            simpleAppUpdate.cancelWorker()
        }

        binding.btnUpdateLogs.setOnClickListener {
            binding.tvLogs.text = simpleAppUpdate.getLogs()
        }
    }


    private fun configureManualCheck() {
        binding.btnCheckForUpdate.setOnClickListener {
            binding.tvManualCheck.setText(R.string.checking)

            simpleAppUpdate.setUpdateAvailableListener {
                binding.tvManualCheck.setText(R.string.update_available)
                binding.btnUpdate.visibility = View.VISIBLE
                binding.btnUpdate.setOnClickListener {
                    simpleAppUpdate.launchUpdate()
                }
            }

            simpleAppUpdate.setErrorListener { error ->
                binding.tvManualCheck.text = "Error: $error"
            }

            simpleAppUpdate.checkUpdateAvailable()
        }
    }


    private fun checkUpdateBarStyle(showBar: Boolean) {
        binding.simpleAppUpdateView.visibility = if (showBar) View.VISIBLE else View.GONE
    }
}