package com.example.cryptownfile

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val editTextResultFull = findViewById<EditText>(R.id.editTextResultFull)
        val buttonDeleteFile = findViewById<Button>(R.id.buttonDeleteFile)
        val buttonClose = findViewById<Button>(R.id.buttonClose)
        val textViewResultMessage = findViewById<TextView>(R.id.textViewResultMessage)
        val buttonOverwriteSave = findViewById<Button>(R.id.buttonOverwriteSave)

        // スクロールを有効化　if this is TextView. EditText doesn't need this code.
        // textViewResultFull.movementMethod = ScrollingMovementMethod()

        // メイン画面から渡されたテキストを受け取って表示
        val resultText = intent.getStringExtra("RESULT_TEXT") ?: "データがありません"
        editTextResultFull.setText(resultText)

        // 【削除ボタン】の処理：ダイアログなしで即時削除
        buttonDeleteFile.setOnClickListener {
            val file = File(this.filesDir, DATA_FILE_NAME)
            if (file.exists()) {
                val deleted = file.delete()
                if (deleted) {
                    var deleteMessage = "◎ファイル削除完了。\n"
                    // 2. Android Keystoreから暗号鍵を削除する処理
                    try {
                        // Keystoreにアクセス
                        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

                        // 鍵が存在するか確認して削除
                        if (keyStore.containsAlias(KEY_ALIAS)) {
                            keyStore.deleteEntry(KEY_ALIAS) // ★ここでシステムから鍵を完全に削除します
                            deleteMessage += "◎ 暗号鍵(Keystore)を完全に消去しました"
                        } else {
                            deleteMessage += "☓ 消去する暗号鍵が存在しません"
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        deleteMessage += "☓ 鍵の消去中にエラーが発生しました: ${e.message}"
                    }

                    // 3. 結果を画面とトーストで通知
                    Toast.makeText(this, "削除処理が完了しました", Toast.LENGTH_SHORT).show()
                    textViewResultMessage.text = deleteMessage
                } else {
                    Toast.makeText(this, "ファイルの削除に失敗しました", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "削除するファイルが存在しません", Toast.LENGTH_SHORT).show()
            }
        }

        buttonOverwriteSave.setOnClickListener {
            val editedText = editTextResultFull.text.toString()
            if (editedText.isEmpty()) {
                Toast.makeText(this, "テキストを入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {

                // 1. 新しいテキストを暗号化
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.ENCRYPT_MODE, CryptoManager.getOrCreateSecretKey())

                val ivBytes = cipher.iv
                val encryptedBytes = cipher.doFinal(editedText.toByteArray(Charsets.UTF_8))

                // 2. 暗号化ファイルへの上書き処理
                val file = File(this.filesDir, DATA_FILE_NAME)

                // 安全のため、既にファイルが存在する場合は一度確実に削除する
                if (file.exists()) {
                    file.delete()
                }

                // 新しく暗号化データを書き込む
                file.outputStream().use { output ->
                    output.write(ivBytes.size) // IVサイズ
                    output.write(ivBytes)      // IV本体
                    output.write(encryptedBytes) // 暗号化データ
                }

                Toast.makeText(this, "編集内容を暗号化して上書き保存しました", Toast.LENGTH_SHORT).show()
                textViewResultMessage.text = "上書き保存完了"
            } catch (e: Exception) {
                e.printStackTrace()

                Toast.makeText(this, "上書き保存に失敗しました: ${e.message}", Toast.LENGTH_LONG).show()
                textViewResultMessage.text = e.message;
            }
        }
        // 【閉じるボタン】の処理
        buttonClose.setOnClickListener {
            finish() // 現在の画面を終了して前の画面に戻る
        }
    }
}
