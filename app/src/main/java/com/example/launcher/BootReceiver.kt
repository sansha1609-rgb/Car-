package com.example.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.MainActivity

/**
 * Boot Completed Broadcast Receiver for Automotive Radios / Tape Recorders / Head Units:
 * Ensures the Car Launcher automatically launches when the car ignition is turned on
 * and the head unit finishes booting up.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("BootReceiver", "Received boot event: $action")
        if (Intent.ACTION_BOOT_COMPLETED == action || Intent.ACTION_MY_PACKAGE_REPLACED == action) {
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            }
            try {
                context.startActivity(launchIntent)
            } catch (e: Exception) {
                Log.e("BootReceiver", "Failed to auto-start Car Launcher on boot", e)
            }
        }
    }
}
