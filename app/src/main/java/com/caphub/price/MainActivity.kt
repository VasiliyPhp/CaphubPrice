package com.caphub.price
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.InputStream
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pick = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let { (applicationContext as android.content.Context).contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            findViewById<android.view.View>(android.R.id.content).post { viewModelStore } // noop
        }
        setContent {
            val vm: PriceViewModel = viewModel()
            var uriStr by remember { mutableStateOf<String?>(null) }
            Scaffold(topBar = { TopAppBar(title = { Text("CaphubPrice") }) }) {
                Column(Modifier.padding(16.dp)) {
                    Button(onClick = { pick.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) }) {
                        Text("Выбрать .xlsx")
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = vm.lowMarkupText, onValueChange = { vm.lowMarkupText = it }, label = { Text("Наценка для <3000 (%)") })
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = vm.highMarkupText, onValueChange = { vm.highMarkupText = it }, label = { Text("Наценка для >=3000 (%)") })
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { vm.startProcessing(this@MainActivity) }) {
                        Text("Обработать")
                    }
                    Spacer(Modifier.height(12.dp))
                    if (vm.isProcessing) CircularProgressIndicator()
                    vm.resultPath?.let { path ->
                        Spacer(Modifier.height(8.dp))
                        Text("Готово: $path")
                        Button(onClick = { // share
                            val file = java.io.File(path)
                            val uri = androidx.core.content.FileProvider.getUriForFile(this@MainActivity, packageName + ".provider", file)
                            val intent = Intent(Intent.ACTION_SEND).apply { type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                            startActivity(Intent.createChooser(intent, "Share"))
                        }) { Text("Поделиться итоговым .xlsx") }
                    }
                }
            }
        }
    }
}
