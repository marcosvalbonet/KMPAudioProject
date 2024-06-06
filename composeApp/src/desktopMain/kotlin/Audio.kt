import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import javax.sound.sampled.*


fun getAudio(){
    val format = AudioFormat(44100.0f, 16, 2, true, true)
    val info = DataLine.Info(TargetDataLine::class.java, format)

    if (!AudioSystem.isLineSupported(info)) {
        println("Line not supported")
        return
    }

    val line = AudioSystem.getLine(info) as TargetDataLine
    line.open(format)

    //TODO: ADD DialogWindow
    line.start()

    println("Start capturing audio...")

    val audioRecorderThread = Thread (){
        run {
            val recordingStream = AudioInputStream(line)
            val outputFile = File("record.wav")
            val fileOutputStream = FileOutputStream(outputFile)
            try {
                AudioSystem.write(recordingStream, AudioFileFormat.Type.WAVE, fileOutputStream)
            } catch( e:IOException){
                println(e.message)
            }
            println("Stopped recording")
        }
    }

    audioRecorderThread.start()


}