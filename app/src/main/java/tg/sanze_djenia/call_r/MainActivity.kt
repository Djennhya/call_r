package tg.sanze_djenia.call_r

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_PERMISSIONS = 1
    }

    private lateinit var startRecordingButton: Button
    private lateinit var stopRecordingButton: Button
    private lateinit var recordingsListView: ListView
    private lateinit var playButton: Button
    private lateinit var pauseButton: Button
    private lateinit var stopButton: Button
    private lateinit var recordingsAdapter: ArrayAdapter<String>
    private var mediaPlayer: MediaPlayer? = null
    private var isPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startRecordingButton = findViewById(R.id.startRecordingButton)
        stopRecordingButton = findViewById(R.id.stopRecordingButton)
        recordingsListView = findViewById(R.id.recordingsListView)
        playButton = findViewById(R.id.playButton)
        pauseButton = findViewById(R.id.pauseButton)
        stopButton = findViewById(R.id.stopButton)

        startRecordingButton.setOnClickListener {
            if (checkPermissions()) {
                startRecordingService()
            }
        }

        stopRecordingButton.setOnClickListener {
            stopRecordingService()
        }

        playButton.setOnClickListener {
            if (isPaused) {
                mediaPlayer?.start()
                pauseButton.visibility = Button.VISIBLE
                stopButton.visibility = Button.VISIBLE
                playButton.visibility = Button.GONE
                isPaused = false
            } else {
                val selectedItem = recordingsListView.selectedItem as String?
                selectedItem?.let { playRecording(it) }
            }
        }

        pauseButton.setOnClickListener {
            mediaPlayer?.pause()
            playButton.visibility = Button.VISIBLE
            pauseButton.visibility = Button.GONE
            stopButton.visibility = Button.VISIBLE
            isPaused = true
        }

        stopButton.setOnClickListener {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            playButton.visibility = Button.VISIBLE
            pauseButton.visibility = Button.GONE
            stopButton.visibility = Button.GONE
            isPaused = false
        }

        requestPermissions()
        loadRecordings()
    }

    private fun checkPermissions(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.RECEIVE_BOOT_COMPLETED
        )

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.RECEIVE_BOOT_COMPLETED
        )

        if (!checkPermissions()) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS)
        }
    }

    private fun startRecordingService() {
        val intent = Intent(this, CallRecordingService::class.java)
        ContextCompat.startForegroundService(this, intent)
        startRecordingButton.visibility = Button.GONE
        stopRecordingButton.visibility = Button.VISIBLE
    }

    private fun stopRecordingService() {
        val intent = Intent(this, CallRecordingService::class.java)
        stopService(intent)
        startRecordingButton.visibility = Button.VISIBLE
        stopRecordingButton.visibility = Button.GONE
    }

    private fun loadRecordings() {
        val recordingsDir = Environment.getExternalStorageDirectory().path
        val recordings = File(recordingsDir).listFiles { _, name ->
            name.startsWith("recording_") && name.endsWith(".3gp")
        }?.map { it.name } ?: emptyList()

        recordingsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, recordings)
        recordingsListView.adapter = recordingsAdapter
    }

    private fun playRecording(recording: String) {
        val filePath = "${Environment.getExternalStorageDirectory().path}/$recording"
        mediaPlayer = MediaPlayer().apply {
            setDataSource(filePath)
            prepare()
            start()
        }
        playButton.visibility = Button.GONE
        pauseButton.visibility = Button.VISIBLE
        stopButton.visibility = Button.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}
