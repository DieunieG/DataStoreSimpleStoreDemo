package edu.farmingdale.datastoresimplestoredemo

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import edu.farmingdale.datastoresimplestoredemo.data.AppPreferences
import edu.farmingdale.datastoresimplestoredemo.ui.theme.DataStoreSimpleStoreDemoTheme
import kotlinx.coroutines.launch
import java.io.PrintWriter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DataStoreSimpleStoreDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DataStoreDemo(modifier = Modifier.padding(innerPadding))
                }
            }
        }

        writeToInternalFile()
        val fileContents = readFromInternalFile()
        Log.d("MainActivity", fileContents)
    }

    private fun writeToInternalFile() {
        openFileOutput("fav_haiku", Context.MODE_PRIVATE).use { out ->
            PrintWriter(out).use { writer ->
                writer.println("This world of dew")
                writer.println("is a world of dew,")
                writer.println("and yet, and yet.")
            }
        }
    }

    private fun readFromInternalFile(): String {
        return openFileInput("fav_haiku").bufferedReader().use { reader ->
            buildString {
                reader.forEachLine { line ->
                    append(line).append("\n BCS 371 \n").appendLine()
                }
            }
        }
    }
}

@Composable
fun DataStoreDemo(modifier: Modifier) {
    val context = LocalContext.current
    val store = remember(context) { AppStorage(context) }

    val appPrefs by store.appPreferenceFlow.collectAsState(AppPreferences())
    val coroutineScope = rememberCoroutineScope()

    var username by rememberSaveable { mutableStateOf("") }
    var scoreInput by rememberSaveable { mutableStateOf("") }
    var darkMode by rememberSaveable { mutableStateOf(false) }

    // initialize UI fields from saved values once data arrives
    LaunchedEffect(appPrefs) {
        if (username.isEmpty()) username = appPrefs.userName
        if (scoreInput.isEmpty()) scoreInput = appPrefs.highScore.toString()
        darkMode = appPrefs.darkMode
    }

    Column(
        modifier = modifier.padding(50.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Values = ${appPrefs.userName}, ${appPrefs.highScore}, ${appPrefs.darkMode}")

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") }
        )

        OutlinedTextField(
            value = scoreInput,
            onValueChange = { scoreInput = it },
            label = { Text("High score (number)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Dark Mode")
            Switch(checked = darkMode, onCheckedChange = { darkMode = it })
        }

        Button(onClick = {
            coroutineScope.launch {
                store.saveUsername(username)
                store.saveHighScore(scoreInput.toIntOrNull() ?: 0)
                store.saveDarkMode(darkMode)
            }
        }) {
            Text("Save Values")
        }
    }
}
