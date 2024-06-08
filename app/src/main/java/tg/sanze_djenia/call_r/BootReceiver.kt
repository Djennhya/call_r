package tg.sanze_djenia.call_r

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Register for call state changes
            val callReceiverIntent = Intent(context, CallReceiver::class.java)
            context.sendBroadcast(callReceiverIntent)
        }
    }
}
