package com.example.sendingfileusingtcp

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.lang.Exception
import java.net.Socket
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    val WRITE_EXTERNAL_STORAGE_CODE = 1
    lateinit var mText: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //get the user input to write to file and send
        mText = enteredText.text.toString()

        btnSend.setOnClickListener {

            //checking necessary permissions for writing files to the device storage

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    val permissions = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestPermissions(permissions, WRITE_EXTERNAL_STORAGE_CODE)
                } else {
                    saveToFile(mText)


                }
            } else {
                saveToFile(mText)


            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            WRITE_EXTERNAL_STORAGE_CODE -> {
                //checking if user grant permission or not
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveToFile(mText)


                } else {
                    Toast.makeText(this, "permission is needed", Toast.LENGTH_LONG)
                        .show()
                }

            }
        }
    }

    private fun saveToFile(myText: String) {
        //specifying custom format of date to use for files name
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.ENGLISH
        ).format(System.currentTimeMillis())
        val myTime = SimpleDateFormat(
            "yyyy:MM:dd:HH:mm:ss",
            Locale.ENGLISH
        ).format(System.currentTimeMillis())

        try {
            val path: File = Environment.getExternalStorageDirectory()
            // following line will create a folder named Tcp Test
            val dir = File("$path/Tcp Test/")
            dir.mkdirs()
            // declaring file's name
            val fileName = "MyFile_$timeStamp.txt"
            val file = File(dir, fileName)
            // writing to file
            val fileWriter: FileWriter = FileWriter(file.absoluteFile)
            val bufferedWriter = BufferedWriter(fileWriter)
            bufferedWriter.write("$$myText")
            bufferedWriter.close()
            Toast.makeText(this, "$fileName is saved to \n $dir", Toast.LENGTH_LONG).show()
            MyClientTask(file).execute()
        } catch (e: Exception) {
            Toast.makeText(this, "${e.message}", Toast.LENGTH_LONG).show()

        }

    }

    @SuppressLint("StaticFieldLeak")
    inner class MyClientTask(private var file: File) :
        AsyncTask<Void?, Void?, Void?>() {
        private var response = ""
        override fun doInBackground(vararg params: Void?): Void? {
            var socket: Socket? = null
            try {
                // you can customize this part and enter your host's ip and port
                socket = Socket("192.168.1.15", 333)
                val myByteArray = ByteArray(file.length().toInt())
                val fis = FileInputStream(file)
                val bis = BufferedInputStream(fis)
                bis.read(myByteArray, 0, myByteArray.size)
                val os = socket.getOutputStream()
                os?.write(myByteArray, 0, myByteArray.size)
                os?.flush()
                socket.close()


            } catch (e: UnknownHostException) {

                e.printStackTrace()
                response = "UnknownHostException: $e"
            } catch (e: IOException) {

                e.printStackTrace()
                response = "IOException: $e"
            } finally {
                if (socket != null) {
                    try {
                        socket.close()
                    } catch (e: IOException) {

                        e.printStackTrace()
                    }
                }
            }
            return null

        }

        override fun onPostExecute(result: Void?) {
            Log.d("status", response)
            super.onPostExecute(result)
        }
    }
}