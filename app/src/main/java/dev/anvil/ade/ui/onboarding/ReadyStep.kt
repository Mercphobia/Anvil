package dev.anvil.ade.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.ui.theme.LocalThemeTokens
import dev.anvil.ade.viewmodel.AnvilViewModel

/**
 * Ready step — Blueprint 4.5.1 langkah 4.
 * Wordmark kecil + green "Setup complete" (PERTAMA KALI warna hijau muncul —
 * momen positif yang disengaja) + 3 action buttons.
 */
@Composable
fun ReadyStep(viewModel: AnvilViewModel) {
    val tokens = LocalThemeTokens.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Small wordmark
        Text(
            text = "ANVIL",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = 6.sp
        )
        Spacer(Modifier.height(20.dp))

        // Green check + "Setup complete" — first intentional green signal
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = "Setup complete",
            tint = tokens.ok,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Setup complete",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = tokens.ok
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "You're ready to start coding.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(40.dp))

        // 3 action buttons
        Button(
            onClick = { viewModel.toggleSetupWizard(false) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Create project", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = { viewModel.toggleSetupWizard(false) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.FolderOpen, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Open project")
        }

        Spacer(Modifier.height(10.dp))

        TextButton(onClick = { viewModel.toggleSetupWizard(false) }) {
            Text("Clone repository", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}