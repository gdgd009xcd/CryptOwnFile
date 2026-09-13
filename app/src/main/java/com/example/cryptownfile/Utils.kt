package com.example.cryptownfile

import android.content.Context
import android.util.Base64
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import javax.crypto.Cipher
import java.io.File
import javax.crypto.spec.GCMParameterSpec

const val OWN_FOLDER_NAME = "MyCryptOwnFolder"
object Utils {
    fun getOwnFolder(context: Context): File {
        val dir = File(context.filesDir, OWN_FOLDER_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    fun saveFile(file: File, inputText: String, textViewResult: TextView?):Boolean {
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, CryptoManager.getOrCreateSecretKey())

            val ivBytes = cipher.iv // 自動生成されたIV（初期化ベクトル）
            val encryptedBytes = cipher.doFinal(inputText.toByteArray(Charsets.UTF_8))

// 2. 「IVの長さ(1バイト) + IV本体 + 暗号化データ」を1つのバイナリに結合してファイル保存

            file.outputStream().use { output ->
                output.write(ivBytes.size) // 先頭にIVのサイズを書き込む（通常GCMは12バイト）
// 次にIV本体を書き込む
                output.write(ivBytes)
                output.write(encryptedBytes) // 最後に暗号データを書き込む
            }

// 画面表示用にBase64形式の文字列にして結果表示
            val encryptedTextBase64 = Base64.encodeToString(
                encryptedBytes,
                Base64.DEFAULT
            )
            textViewResult?.text =
                "【暗号化してローカルに保存しました】\nファイル名:${file.name}"
            return true;
        } catch (e: Exception) {
            // print stackTrace to Logcat in Android Studio.
            Log.getStackTraceString(e)
            textViewResult?.text = "エラー：　${e.message}"
        }
        return false
    }

    fun openFile(file: File, context: Context) : String {
        try {
            file.inputStream().use { input ->
                val ivSize = input.read()
                if (ivSize <= 0) {
                    Toast.makeText(context, "読み込み・復号に失敗しました", Toast.LENGTH_SHORT)
                        .show()
                    return ""
                }

                val ivBytes = ByteArray(ivSize)
                input.read(ivBytes)

                val encryptedBytes = input.readBytes()

                val cipher = Cipher.getInstance(TRANSFORMATION)
                val spec = GCMParameterSpec(128, ivBytes)
                cipher.init(Cipher.DECRYPT_MODE, CryptoManager.getOrCreateSecretKey(), spec)

                val decryptedBytes = cipher.doFinal(encryptedBytes)
                val decryptedText = String(decryptedBytes, Charsets.UTF_8)
                return decryptedText
            }
        } catch (e:Exception) {
            Log.getStackTraceString(e)
            Toast.makeText(context, "読み込み・復号失敗例外発生", Toast.LENGTH_SHORT)
        }
        return ""
    }
}