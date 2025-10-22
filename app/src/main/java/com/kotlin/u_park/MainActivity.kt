package com.kotlin.u_park

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.kotlin.u_park.ui.navigation.NavGraph
import com.kotlin.u_park.ui.theme.UParkTheme
import com.kotlin.u_park.data.remote.supabase
import com.kotlin.u_park.data.repository.AuthRepository

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

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun App() {
    val navController = rememberNavController()
    NavGraph(navController = navController, authRepository = AuthRepository(supabase))
}
