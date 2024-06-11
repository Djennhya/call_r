package tg.sanze_djenia.call_r

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log

class CallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    // Incoming call is ringing
                    if (phoneNumber != null) {
                        startRecordingService(context!!, phoneNumber)
                    }
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    // Call is answered or an outgoing call is initiated
                    if (phoneNumber != null) {
                        startRecordingService(context!!, phoneNumber)
                    }
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    // Call ended or idle
                    stopRecordingService(context!!)
                }
            }
        }
    }

    private fun startRecordingService(context: Context, phoneNumber: String) {
        val intent = Intent(context, CallRecordingService::class.java).apply {
            putExtra("PHONE_NUMBER", phoneNumber)
        }
        context.startService(intent)
    }

    private fun stopRecordingService(context: Context) {
        val intent = Intent(context, CallRecordingService::class.java)
        context.stopService(intent)
    }
}
