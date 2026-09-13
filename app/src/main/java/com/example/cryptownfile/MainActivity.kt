package com.example.cryptownfile

import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

// alias name.
const val KEY_ALIAS = "0123Awful"
// android key store. MUST be the same as this value.
const val ANDROID_KEYSTORE = "AndroidKeyStore"
const val TRANSFORMATION = "AES/GCM/NoPadding"
const val DATA_FILE_NAME = "dumb.dat"
const val DATA_FILE_EXT = ".dat"
const val DATA_FILE_NAME_DEFAULT = "ZDEFAULT"
class MainActivity : AppCompatActivity() {

    // registerForActivityResult を定義する
    val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // 呼び出し先のActivityを閉じた時に呼び出されるコールバックを登録
            // (呼び出し先で埋め込んだデータを取り出して処理する)
            val editTextInput = findViewById<EditText>(R.id.editTextInput)
            val textViewResult = findViewById<TextView>(R.id.textViewResult)
            val resultIntent = result.data
            val message = resultIntent?.getStringExtra("message")
            Log.i("Main", message?:"")
            if (result.resultCode == RESULT_OK) {
                // RESULT_OK時の処理
                editTextInput.text.clear()
                textViewResult.setText(message)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            } else {

                textViewResult.setText(message)
            }
        }

    // 保存するファイル名
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // prevent screenshot.
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        setContentView(R.layout.activity_main)

        val editTextInput = findViewById<EditText>(R.id.editTextInput)
        val buttonEncrypt = findViewById<Button>(R.id.buttonEncrypt)
        val buttonFiler = findViewById<Button>(R.id.buttonFiler)
        val textViewResult = findViewById<TextView>(R.id.textViewResult)
        var fileName = DATA_FILE_NAME

// アプリ起動時に鍵を（なければ）自動生成
        CryptoManager.getOrCreateSecretKey()

// 【暗号化して保存ボタン】が押された時の処理
        buttonEncrypt.setOnClickListener {
            val inputText = editTextInput.text.toString()
            if (inputText.isEmpty()) {
                Toast.makeText(this, "テキストを入力してください",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                val fileNameTextTrimmed = inputText.split("\n","\r", limit = 2)[0].trim()
                val fileNameText = fileNameTextTrimmed.substring(0, if (fileNameTextTrimmed.length > 50) 50 else fileNameTextTrimmed.length)
                if (!fileNameText.isEmpty()) {
                    fileName = fileNameText + DATA_FILE_EXT
                } else {
                    fileName = DATA_FILE_NAME_DEFAULT + DATA_FILE_EXT
                }
            }

            try {
// 1. 暗号化の実行
                val file = File(Utils.getOwnFolder(this), fileName)
                if (file.exists()) {
                    val intent = android.content.Intent(this,FileManager::class.java).apply {
                        putExtra("FILE_NAME", fileName)
                        putExtra("INPUT_TEXT", inputText)
                    }
                    getContent.launch(intent)
                } else {
                    if (Utils.saveFile(file, inputText, textViewResult)) {
                        editTextInput.text.clear() // 入力欄をクリア
                    } else {
                        Toast.makeText(this, "暗号化に失敗", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.getStackTraceString(e)
                textViewResult.text = "暗号化・保存に失敗しました: ${e.message}"
            }
        }

        buttonFiler.setOnClickListener {
            val intent = android.content.Intent(this,FileManager::class.java)
            getContent.launch(intent)
        }

    }
}