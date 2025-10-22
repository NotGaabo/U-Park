package com.kotlin.u_park.ui.screens.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kotlin.u_park.domain.model.User


@SuppressLint("MutableCollectionMutableState")
@Composable
fun SettingsScreen(
    currentUser: User,
    userRoles: List<String>,
    allRoles: List<String>,
    onSaveRoles: (selectedRoles: List<String>) -> Unit
) {
    var selectedRoles by remember { mutableStateOf(userRoles.toMutableSet()) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Hola, ${currentUser.nombre}", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Tus roles:", style = MaterialTheme.typography.titleSmall)

        allRoles.forEach { role ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Checkbox(
                    checked = selectedRoles.contains(role),
                    onCheckedChange = { isChecked ->
                        if (isChecked) selectedRoles.add(role) else selectedRoles.remove(role)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = role)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onSaveRoles(selectedRoles.toList()) }) {
            Text(text = "Guardar Roles")
        }
    }
}
