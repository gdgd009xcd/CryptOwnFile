package com.example.cryptownfile

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class FileManager : AppCompatActivity() {

    private var fileNames: List<String> = emptyList()
    private var fileName = ""
    private var inputText = ""
    private var selectedFile: File? = null

    private var saveMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filer)

        // prevent screenshot.
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        val listView: ListView = findViewById(R.id.file_list_view)
        val btnOpen: Button = findViewById(R.id.btn_open)
        val btnDelete: Button = findViewById(R.id.btn_delete)
        val btnClose: Button = findViewById(R.id.btn_close)
        val fileNameInput: EditText = findViewById(R.id.file_name_input)

        fileName = intent.getStringExtra("FILE_NAME") ?: ""
        fileNameInput.setText(fileName)
        inputText = intent.getStringExtra("INPUT_TEXT") ?: ""

        if (!fileName.isEmpty() && !inputText.isEmpty()) {
            saveMode = true
            btnOpen.text = "OverWrite"
        }

        // 4. ListViewにAdapterをセットして表示
        fileNames = getFileList()
        listView.adapter = getFileListAdapter(fileNames)

        // 5. リストの項目がタップされた時のイベント処理（ラムダ式でスマートに記述）
        listView.setOnItemClickListener { parent, _, position, _ ->
            // タップされた位置のファイル名を取得
            // val selectedFileName = fileNames[position]
            val selectedFileName = parent.getItemAtPosition(position) as String
            // 実際のFileオブジェクトにアクセス
            selectedFile = File(Utils.getOwnFolder(this), selectedFileName)

            val selectedFileLocal = selectedFile
            if (selectedFileLocal != null && !selectedFileLocal.name.isEmpty()) {
                fileName = selectedFileLocal.name
                fileNameInput.setText(fileName)
            }

            // open file.
            if (!saveMode && selectedFileLocal != null) {
                val intent = android.content.Intent(this, ViewActivity::class.java).apply {
                    putExtra("FILE_NAME", selectedFileLocal.name)
                }
                startActivity(intent)
                fileNameInput.setText("")
                selectedFile = null
            }
            // テスト用にトースト通知を表示
            /*
            Toast.makeText(
                this,
                "選択されたファイル: $selectedFileName",
                Toast.LENGTH_SHORT
            ).show()
            */
        }

        listView.setOnItemLongClickListener { parent, view, position, id ->
            // 長押しされたアイテムのデータを取得
            val selectedFileName = parent.getItemAtPosition(position) as String

            selectedFile = File(Utils.getOwnFolder(this), selectedFileName)

            val selectedFileLocal = selectedFile
            if (selectedFileLocal != null && !selectedFileLocal.name.isEmpty()) {
                fileName = selectedFileLocal.name
                fileNameInput.setText(fileName)
            }

            // trueを返すと「イベントを消費」したことになり、通常のクリック（setOnItemClickListener）が発生しなくなります。
            // falseを返すと、長押しを離した後に通常のクリックイベントも続けて発生します。
            true
        }
        // Save/Open file
        btnOpen.setOnClickListener {
            val selectedFileLocal = selectedFile
            if (saveMode) {
                fileName = fileNameInput.text.toString()
                if (!fileName.isEmpty()) {
                    val file = File(Utils.getOwnFolder(this), fileName)
                    if (file.exists()) {
                        Toast.makeText(this, "同一ファイルが存在します", Toast.LENGTH_SHORT).show()
                    } else {
                        if (Utils.saveFile(file, inputText, null)) {
                            fileNames = getFileList()
                            listView.adapter = getFileListAdapter(fileNames)
                            intent.putExtra("message", "保存しました。:${file.name}")
                            setResult(RESULT_OK, intent)
                            finish()
                        } else {
                            Toast.makeText(this, "暗号化に失敗", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "ファイル名がありません", Toast.LENGTH_SHORT).show()
                }
            } else if (selectedFileLocal != null) {
                // open file.
                val intent = android.content.Intent(this, ViewActivity::class.java).apply {
                    putExtra("FILE_NAME", selectedFileLocal.name)
                }
                startActivity(intent)
                fileNameInput.setText("")
                selectedFile = null
            } else {
                Toast.makeText(this, "ファイルを選択してください", Toast.LENGTH_SHORT).show()
            }

        }

        // delete selected file
        btnDelete.setOnClickListener {
            val selectedFileLocal = selectedFile
            if (selectedFileLocal != null) {
                val deleted = selectedFileLocal.delete()
                if (deleted) {
                    fileNames = getFileList()
                    listView.adapter = getFileListAdapter(fileNames)
                    Toast.makeText(this, "ファイル削除完了　${selectedFileLocal.name}", Toast.LENGTH_SHORT).show()
                    selectedFile = null
                    fileNameInput.setText("")
                }
            } else {
                Toast.makeText(this, "ファイル長押し選択してください", Toast.LENGTH_SHORT).show()
            }
        }

        // 「閉じる」ボタン（画面を終了する）
        btnClose.setOnClickListener {
            setResult(RESULT_CANCELED, intent)
            intent.putExtra("message", " ")
            finish()
        }
    }

    fun getFileList() : List<String> {
        // 1. アプリ固有領域からファイル一覧を取得
        val internalFilesDir = Utils.getOwnFolder(this)
        val files: Array<File>? = internalFilesDir.listFiles()

        // 2. 表示用に「ファイル名（文字列）」のリストを作成（Kotlinのスコープ関数とmapで簡潔に）
        return files?.map { it.name } ?: emptyList()
    }

    fun getFileListAdapter(fileNameList: List<String>) : ArrayAdapter<String> {

        // 3. Adapterを作成（Android標準の簡易テキストレイアウトを使用）
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1, // 1行1文字列の標準レイアウト
            fileNameList
        )
        return adapter
    }
}