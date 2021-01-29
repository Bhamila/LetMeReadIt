package com.example.readmessage

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.telephony.SmsManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import org.w3c.dom.Text
import java.util.*
import java.util.jar.Manifest
import android.telephony.SmsMessage as SmsMessage1


class MainActivity : AppCompatActivity() {
    lateinit var mTTS: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.RECEIVE_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.RECEIVE_SMS,
                    android.Manifest.permission.SEND_SMS,android.Manifest.permission.READ_SMS
                )
                ,
                111
            )
        } else
            receiveMsg()

        buttonSend.setOnClickListener {
            var sms = SmsManager.getDefault()

            sms.sendTextMessage(editText1.text.toString(), null, editText2.text.toString(), null, null)
        }

            mTTS= TextToSpeech(applicationContext,TextToSpeech.OnInitListener { status ->
                if (status!=TextToSpeech.ERROR){
                    mTTS.language= Locale.US
                    buttonSpeak.isEnabled=true
                }
            })
        buttonSpeak.setOnClickListener {
            var toSpeak=editText2.text.toString()
            var codePattern ="(\\d{6})".toRegex()
            if (toSpeak==""){
                Toast.makeText(this,"NO MSG TO READ",Toast.LENGTH_SHORT).show()
        }
           else{

                Toast.makeText(this,toSpeak,Toast.LENGTH_SHORT).show()
                var pitch=(pitchSeekBar2.progress/50).toFloat()
                if (pitch<0.1) {
                    pitch= 0.1F
                }
                var speed=(speedSeekBar.progress/50).toFloat()
                if (speed<0.1) {
                    speed= 0.1F
                }
                mTTS.setPitch(pitch)
                mTTS.setSpeechRate(speed)
                //match pattern for OTP
                val finalMessage: MatchResult? = codePattern.find(toSpeak)
                if(finalMessage?.value!=null){
                    mTTS.speak(finalMessage.value.toString(),TextToSpeech.QUEUE_FLUSH,null)
                    closeKeyboard()
                }
                else
                    Toast.makeText(this,"NO MSG TO READ",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onDestroy() {
        if(mTTS!=null){
            mTTS.stop()
            mTTS.shutdown()
        }
        super.onDestroy()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode ==111 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            receiveMsg()
    }

    private fun receiveMsg() {
        var br=object: BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.KITKAT){
                    for(sms in Telephony.Sms.Intents.getMessagesFromIntent(p1)){
                        editText1.setText(sms.originatingAddress)
                        editText2.setText(sms.displayMessageBody)

                    }
                }
            }

        }
        registerReceiver(br, IntentFilter("android.provider.Telephony.SMS_RECEIVED"))

}
}




