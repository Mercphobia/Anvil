package dev.anvil.ade.ui.onboarding

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.viewmodel.ProjectSource
import dev.anvil.ade.viewmodel.AnvilViewModel

@Composable
fun ProjectSourceStep(viewModel: AnvilViewModel) {
    var source by remember { mutableStateOf(ProjectSource.NEW) }
    var localPath by remember { mutableStateOf("") }
    var repoUrl by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Text(
            text = "Project",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Start fresh, open something local, or clone a repository from GitHub.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SourceOption(
                icon = Icons.Filled.Add,
                title = "New Project",
                desc = "Create from a template",
                selected = source == ProjectSource.NEW,
                onClick = { source = ProjectSource.NEW; viewModel.setWizardProjectSource(it) }
            )
            SourceOption(
                icon = Icons.Filled.FolderOpen,
                title = "Open Local Project",
                desc = "Pick an existing folder in the workspace",
                selected = source == ProjectSource.LOCAL,
                onClick = { source = ProjectSource.LOCAL; viewModel.setWizardProjectSource(it) }
            )
            SourceOption(
                icon = Icons.Filled.Share,
                title = "Clone from GitHub",
                desc = "git clone into the sandbox",
                selected = source == ProjectSource.CLONE,
                onClick = { source = ProjectSource.CLONE; viewModel.setWizardProjectSource(it) }
            )

            when (source) {
                ProjectSource.LOCAL -> {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = localPath,
                        onValueChange = {
                            localPath = it
                            viewModel.setWizardLocalPath(it)
                        },
                        label = { Text("Project folder name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                ProjectSource.CLONE -> {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = repoUrl,
                        onValueChange = {
                            repoUrl = it
                            viewModel.setWizardRepoUrl(it)
                        },
                        label = { Text("Repository URL (https://github.com/...)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                else -> {}
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.backWizard() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Back")
            }
            Button(
                onClick = { viewModel.advanceWizard() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
private fun SourceOption(
    icon: ImageVector,
    title: String,
    desc: String,
    selected: Boolean,
    onClick: (ProjectSource) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick(
                when (title) {
                    "New Project" -> ProjectSource.NEW
                    "Open Local Project" -> ProjectSource.LOCAL
                    else -> ProjectSource.CLONE
                }
            ) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary
                   else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.size(14.dp))
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = desc,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
