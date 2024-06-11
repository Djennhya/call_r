package tg.sanze_djenia.call_r

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                    if (phoneNumber != null) {
                        // Démarrer l'enregistrement avec le numéro de téléphone
                        startRecordingService(context!!, phoneNumber)
                    }
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    // Arrêter l'enregistrement
                    stopRecordingService(context!!)
                }
            }
        }
    }

    private fun startRecordingService(context: Context, phoneNumber: String) {
        val intent = Intent(context, CallRecordingService::class.java)
        intent.putExtra("PHONE_NUMBER", phoneNumber)
        context.startService(intent)
    }

    private fun stopRecordingService(context: Context) {
        val intent = Intent(context, CallRecordingService::class.java)
        context.stopService(intent)
    }
}
