package com.samuel.shakepercussion

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private lateinit var soundPool: SoundPool
    private var soundId = 0

    private var tocando = false

    private lateinit var txtStatus: TextView
    private lateinit var progress: ProgressBar
    private lateinit var seekSensibilidade: SeekBar
    private lateinit var seekVolume: SeekBar
    private lateinit var btnIniciar: Button

    private var limite = 12f
    private var volume = 1f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtStatus = findViewById(R.id.txtStatus)
        progress = findViewById(R.id.progressIntensidade)
        seekSensibilidade = findViewById(R.id.seekSensibilidade)
        seekVolume = findViewById(R.id.seekVolume)
        btnIniciar = findViewById(R.id.btnIniciar)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val atributos = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(atributos)
            .build()

        // IMPORTANTE:
        // Depois colocaremos o arquivo "chocalho.wav"
        soundId = soundPool.load(this, R.raw.chocalho, 1)

        seekSensibilidade.progress = 60
        seekVolume.progress = 80

        seekSensibilidade.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                limite = 4f + (100 - progress) / 5f
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekVolume.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                volume = progress / 100f
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnIniciar.setOnClickListener {

            tocando = !tocando

            if (tocando) {
                btnIniciar.text = "PARAR"
                txtStatus.text = "Status: Ativo"

                sensorManager.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_GAME
                )

            } else {

                btnIniciar.text = "INICIAR"
                txtStatus.text = "Status: Parado"

                sensorManager.unregisterListener(this)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {

        if (event == null) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val intensidade = sqrt(x * x + y * y + z * z)

        progress.progress = intensidade.toInt().coerceAtMost(100)

        if (intensidade > limite) {

            val velocidade = (intensidade / 20f).coerceIn(0.8f, 1.4f)

            soundPool.play(
                soundId,
                volume,
                volume,
                1,
                0,
                velocidade
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}