package com.example.cryptownfile

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher

class ViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)

        // prevent screenshot.
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        val textViewResultFull = findViewById<TextView>(R.id.viewTextResultFull)
        val buttonClose = findViewById<Button>(R.id.buttonClose)
        val textViewResultMessage = findViewById<TextView>(R.id.textViewResultMessage)
        val buttonOverwriteSave = findViewById<Button>(R.id.buttonOverwriteSave)

        // スクロールを有効化　if this is TextView. EditText doesn't need this code.
        textViewResultFull.movementMethod = ScrollingMovementMethod()

        // retrieve fileName from Previous Activity
        val fileName = intent.getStringExtra("FILE_NAME")
        if (fileName != null) {
            val file = File(Utils.getOwnFolder(this), fileName)
            val resultText = Utils.openFile(file, this)
            textViewResultFull.setText(resultText)
        }

        buttonOverwriteSave.setOnClickListener {
            // edit
            if (fileName != null) {
                val intent = android.content.Intent(this, ResultActivity::class.java).apply {
                    putExtra("FILE_NAME", fileName)
                }
                startActivity(intent)
            }
            finish()
        }

        // 【閉じるボタン】の処理
        buttonClose.setOnClickListener {
            finish() // 現在の画面を終了して前の画面に戻る
        }
    }
}
