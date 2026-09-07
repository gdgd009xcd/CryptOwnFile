package com.example.cryptownfile

import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
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
class MainActivity : AppCompatActivity() {

    // 保存するファイル名
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val editTextInput = findViewById<EditText>(R.id.editTextInput)
        val buttonEncrypt = findViewById<Button>(R.id.buttonEncrypt)
        val buttonDecrypt = findViewById<Button>(R.id.buttonDecrypt)
        val textViewResult = findViewById<TextView>(R.id.textViewResult)

// アプリ起動時に鍵を（なければ）自動生成
        CryptoManager.getOrCreateSecretKey()

// 【暗号化して保存ボタン】が押された時の処理
        buttonEncrypt.setOnClickListener {
            val inputText = editTextInput.text.toString()
            if (inputText.isEmpty()) {
                Toast.makeText(this, "テキストを入力してください",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
// 1. 暗号化の実行
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.ENCRYPT_MODE, CryptoManager.getOrCreateSecretKey())

                val ivBytes = cipher.iv // 自動生成されたIV（初期化ベクトル）
                val encryptedBytes = cipher.doFinal(inputText.toByteArray(Charsets.UTF_8))

// 2. 「IVの長さ(1バイト) + IV本体 + 暗号化データ」を1つのバイナリに結合してファイル保存
                val file = File(this.filesDir, DATA_FILE_NAME)
                file.outputStream().use { output ->
                    output.write(ivBytes.size) // 先頭にIVのサイズを書き込む（通常GCMは12バイト）
// 次にIV本体を書き込む
                    output.write(ivBytes)
                    output.write(encryptedBytes) // 最後に暗号データを書き込む
                }

// 画面表示用にBase64形式の文字列にして結果表示
                val encryptedTextBase64 = Base64.encodeToString(encryptedBytes,
                    Base64.DEFAULT)
                textViewResult.text = "【暗号化してローカルに保存しました】\nファイル名:$DATA_FILE_NAME"
                editTextInput.text.clear() // 入力欄をクリア

            } catch (e: Exception) {
                e.printStackTrace()
                textViewResult.text = "暗号化・保存に失敗しました: ${e.message}"
            }
        }

// 【ファイルから読み込んで復号ボタン】が押された時の処理
        // ... 省略（ボタンのクリック処理の内部まで移動） ...
        buttonDecrypt.setOnClickListener {
            val file = File(this.filesDir, DATA_FILE_NAME)
            if (!file.exists()) {
                Toast.makeText(this, "保存された暗号化ファイルがありません", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                file.inputStream().use { input ->
                    val ivSize = input.read()
                    if (ivSize <= 0) throw Exception("不適切なファイル形式です")

                    val ivBytes = ByteArray(ivSize)
                    input.read(ivBytes)

                    val encryptedBytes = input.readBytes()

                    val cipher = Cipher.getInstance(TRANSFORMATION)
                    val spec = GCMParameterSpec(128, ivBytes)
                    cipher.init(Cipher.DECRYPT_MODE, CryptoManager.getOrCreateSecretKey(), spec)

                    val decryptedBytes = cipher.doFinal(encryptedBytes)
                    val decryptedText = String(decryptedBytes, Charsets.UTF_8)

                    // ★★★ ここから差し替え ★★★
                    // 新しい画面（ResultActivity）を開き、復号データを引き渡す
                    val intent = android.content.Intent(this, ResultActivity::class.java).apply {
                        putExtra("RESULT_TEXT", decryptedText)
                    }
                    startActivity(intent)
                    // ★★★ ここまで差し替え ★★★
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "読み込み・復号に失敗しました", Toast.LENGTH_SHORT).show()
            }
        }
    }
}