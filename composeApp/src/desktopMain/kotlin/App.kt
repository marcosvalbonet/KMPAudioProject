import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogWindow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.io.*

import java.lang.Thread.sleep
import javax.sound.sampled.*

@Composable
@Preview
fun app() {
    MaterialTheme {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

            var isRecording by remember { mutableStateOf(false) }
            var showDialog by remember { mutableStateOf(false) }
            var stopCapture  by remember { mutableStateOf(false) }

            if (showDialog) {
                audioDialog(onDismiss = { showDialog = false })
            }

            Button(onClick = {
                isRecording = true
                showDialog = true
                startAudioRecording {
                    isRecording = false
                    showDialog = false
                    stopCapture = true
                }
            }) {
                Text("Click me & get audio!")
            }

        }
    }
}

@Composable
fun audioDialog(onDismiss: () -> Unit) {
    // Aquí puedes definir tu diálogo personalizado
    Text("Recording audio... Close this dialog to stop recording.")
}

fun startAudioRecording(onFinish: () -> Unit) {
    val stopCapture = false
    println("Start capturing audio...")

    val format = AudioFormat(44100.0f, 16, 2, true, true)
    val info = DataLine.Info(TargetDataLine::class.java, format)

    if (!AudioSystem.isLineSupported(info)) {
        println("Line not supported")
        return
    }

    val targetLine = AudioSystem.getLine(info) as TargetDataLine
    targetLine.open(format)
    targetLine.start()

    val buffer = ByteArray(4096)
    val out = ByteArrayOutputStream()

    val job = CoroutineScope(Dispatchers.IO).launch {
        while (!stopCapture) {
            val count = targetLine.read(buffer, 0, buffer.size)
            if (count > 0) {
                out.write(buffer, 0, count)
            }
        }

    }

    sleep(3000)
    println("Stopped recording")
    onFinish()
    saveAudioToFile(out.toByteArray(), format)
    job.cancel()
    targetLine.stop()
    targetLine.close()
}

fun saveAudioToFile(audioData: ByteArray, format: AudioFormat) {
    val outputFile = File("record.wav")
    val byteArrayInputStream = ByteArrayInputStream(audioData)
    val audioInputStream = AudioInputStream(byteArrayInputStream, format, audioData.size.toLong() / format.frameSize)

    try {
        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, outputFile)
    } catch (e: IOException) {
        println("Error saving audio to file: ${e.message}")
    } finally {
        try {
            audioInputStream.close()
        } catch (e: IOException) {
            println("Error closing audio input stream: ${e.message}")
        }
    }
}


