package com.example.cryptownfile

import android.os.Bundle
import android.view.WindowManager
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

        // prevent screenshot.
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        val editTextResultFull = findViewById<EditText>(R.id.editTextResultFull)
        val buttonClose = findViewById<Button>(R.id.buttonClose)
        val textViewResultMessage = findViewById<TextView>(R.id.textViewResultMessage)
        val buttonOverwriteSave = findViewById<Button>(R.id.buttonOverwriteSave)

        // スクロールを有効化　if this is TextView. EditText doesn't need this code.
        // textViewResultFull.movementMethod = ScrollingMovementMethod()

        // retrieve fileName from Previous Activity
        val fileName = intent.getStringExtra("FILE_NAME")
        if (fileName != null) {
            val file = File(Utils.getOwnFolder(this), fileName)
            val resultText = Utils.openFile(file, this)
            editTextResultFull.setText(resultText)
        }


        // 【削除ボタン】の処理：ダイアログなしで即時削除
        /*
        buttonDeleteFile.setOnClickListener {
            val file = File(Utils.getOwnFolder(this), DATA_FILE_NAME)
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
        }*/

        buttonOverwriteSave.setOnClickListener {
            val editedText = editTextResultFull.text.toString()
            if (editedText.isEmpty()) {
                Toast.makeText(this, "テキストを入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {

                // 2. 暗号化ファイルへの上書き処理
                val file = File(Utils.getOwnFolder(this), fileName)

                // 安全のため、既にファイルが存在する場合は一度確実に削除する
                if (file.exists()) {
                    file.delete()
                }

                if (Utils.saveFile(file, editedText, textViewResultMessage)) {

                    Toast.makeText(
                        this,
                        "編集内容を暗号化して上書き保存しました",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
