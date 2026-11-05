package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "Device booted / updated: action=$action. Rescheduling all reminders...")
        NotificationService.initialize(context)
        NotificationService.rescheduleAll(context)
    }
}
