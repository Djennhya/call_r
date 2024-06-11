package tg.sanze_djenia.call_r

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
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
        const val TAG = "MainActivity"
    }

    private lateinit var startRecordingButton: Button
    private lateinit var stopRecordingButton: Button
    private lateinit var recordingsListView: ListView
    private lateinit var playButton: Button
    private lateinit var pauseButton: Button
    private lateinit var stopButton: Button
    private lateinit var deleteButton: Button
    private lateinit var recordingsAdapter: ArrayAdapter<String>
    private var mediaPlayer: MediaPlayer? = null
    private var isPaused = false
    private var selectedRecording: String? = null
    private var selectedView: View? = null

    override fun onResume() {
        super.onResume()
        loadRecordings()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startRecordingButton = findViewById(R.id.startRecordingButton)
        stopRecordingButton = findViewById(R.id.stopRecordingButton)
        recordingsListView = findViewById(R.id.recordingsListView)
        playButton = findViewById(R.id.playButton)
        pauseButton = findViewById(R.id.pauseButton)
        stopButton = findViewById(R.id.stopButton)
        deleteButton = findViewById(R.id.deleteButton)

        startRecordingButton.setOnClickListener {
            if (checkPermissions()) {
                startRecordingService()
            }
        }

        stopRecordingButton.setOnClickListener {
            stopRecordingService()
        }

        playButton.setOnClickListener {
            selectedRecording?.let { playRecording(it) }
        }

        pauseButton.setOnClickListener {
            mediaPlayer?.pause()
            playButton.visibility = Button.VISIBLE
            pauseButton.visibility = Button.GONE
            stopButton.visibility = Button.VISIBLE
            isPaused = true
        }

        stopButton.setOnClickListener {
            stopPlayback()
        }

        deleteButton.setOnClickListener {
            selectedRecording?.let { deleteRecording(it) }
        }

        recordingsListView.setOnItemClickListener { _, view, position, _ ->
            selectedView?.setBackgroundColor(Color.TRANSPARENT) // Reset previous selection
            selectedRecording = recordingsAdapter.getItem(position)
            selectedView = view
            view.setBackgroundColor(Color.LTGRAY) // Highlight selected item
            playButton.visibility = Button.VISIBLE
            pauseButton.visibility = Button.GONE
            stopButton.visibility = Button.GONE
            deleteButton.visibility = Button.VISIBLE
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
            name.startsWith("Recording...") && name.endsWith(".3gp")
        }?.map { it.name.removePrefix("Recording...") } ?: emptyList()

        recordingsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, recordings)
        recordingsListView.adapter = recordingsAdapter
    }

    private fun playRecording(recording: String) {
        stopPlayback()  // Stop any ongoing playback

        val filePath = "${Environment.getExternalStorageDirectory().path}/Recording...$recording"
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                start()
                setOnCompletionListener {
                    stopPlayback()
                }
            }
            playButton.visibility = Button.GONE
            pauseButton.visibility = Button.VISIBLE
            stopButton.visibility = Button.VISIBLE
        } catch (e: Exception) {
            Log.e(TAG, "Error playing recording", e)
        }
    }

    private fun stopPlayback() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            playButton.visibility = Button.VISIBLE
            pauseButton.visibility = Button.GONE
            stopButton.visibility = Button.GONE
            deleteButton.visibility = if (selectedRecording != null) Button.VISIBLE else Button.GONE // Show delete button if a recording is selected
            isPaused = false
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping playback", e)
        }
    }

    private fun deleteRecording(recording: String) {
        val filePath =  "${Environment.getExternalStorageDirectory().path}/$recording"
        val file = File(filePath)
        if (file.exists()) {
            if (file.delete()) {
                loadRecordings()
                playButton.visibility = Button.GONE
                pauseButton.visibility = Button.GONE
                stopButton.visibility = Button.GONE
                deleteButton.visibility = Button.GONE
                selectedRecording = null
                selectedView = null
            } else {
                Log.e(TAG, "Error deleting recording")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlayback()
    }
}
