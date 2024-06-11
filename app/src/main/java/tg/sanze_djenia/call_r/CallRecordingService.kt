package tg.sanze_djenia.call_r

import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.Environment
import android.os.IBinder
import android.util.Log
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CallRecordingService : Service() {

    private var recorder: MediaRecorder? = null
    private var isRecording = false
    private var phoneNumber: String? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        phoneNumber = intent?.getStringExtra("PHONE_NUMBER")
        startRecording()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
    }

    private fun startRecording() {
        if (!isRecording && phoneNumber != null) {
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val date = dateFormat.format(Date())
            val time = timeFormat.format(Date())

            val fileName = "Recording...${phoneNumber}_${date}_${time}.3gp"
            val filePath = "${Environment.getExternalStorageDirectory().absolutePath}/$fileName"

            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(filePath)

                try {
                    prepare()
                    start()
                    isRecording = true
                    Log.d("CallRecordingService", "Recording started")
                } catch (e: IOException) {
                    Log.e("CallRecordingService", "Error starting recording", e)
                }
            }
        }
    }

    private fun stopRecording() {
        if (isRecording) {
            recorder?.apply {
                stop()
                release()
                isRecording = false
                Log.d("CallRecordingService", "Recording stopped")
            }
        }
    }
}
