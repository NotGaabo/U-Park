package com.kotlin.u_park

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.kotlin.u_park.ui.navigation.NavGraph
import com.kotlin.u_park.ui.theme.UParkTheme
import com.kotlin.u_park.data.remote.supabase
import io.github.jan.supabase.createSupabaseClient

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            UParkTheme {
                App()
            }
        }
    }
}

@Composable
fun App() {
    val supabase = createSupabaseClient()

    val navController = rememberNavController()
    NavGraph(navController = navController)
}
