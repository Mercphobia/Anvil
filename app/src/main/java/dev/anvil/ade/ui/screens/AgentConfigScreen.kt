package dev.anvil.ade.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import dev.anvil.ade.viewmodel.AnvilViewModel

/**
 * Agent Config screen: manual editing of SOUL.md, project memory, and the
 * agent's skills - all stored in app-private storage that is unreachable
 * from a normal file manager.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentConfigScreen(viewModel: AnvilViewModel, onClose: () -> Unit) {
    var tab by remember { mutableIntStateOf(0) }
    val soulText by viewModel.soulText.collectAsState()
    val memoryText by viewModel.memoryText.collectAsState()
    val skills by viewModel.skillList.collectAsState()
    val selectedSkillContent by viewModel.selectedSkillContent.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadAgentConfig() }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Agent Config", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onClose) { Text("Tutup") }
        }
        TabRow(selectedTabIndex = tab) {
            Tab(tab == 0, { tab = 0 }, text = { Text("Soul") })
            Tab(tab == 1, { tab = 1 }, text = { Text("Memory") })
            Tab(tab == 2, { tab = 2 }, text = { Text("Skills") })
        }
        when (tab) {
            0 -> EditableTextPanel(
                value = soulText,
                onValueChange = viewModel::updateSoulDraft,
                onSave = { viewModel.saveSoul() }
            )
            1 -> EditableTextPanel(
                value = memoryText,
                onValueChange = viewModel::updateMemoryDraft,
                onSave = { viewModel.saveMemory() }
            )
            2 -> SkillsTab(
                skills = skills,
                selectedContent = selectedSkillContent,
                onSelect = viewModel::selectSkill,
                onContentChange = viewModel::updateSelectedSkillDraft,
                onSave = { slug -> viewModel.saveSkill(slug) },
                onCreateNew = { slug, desc -> viewModel.createSkill(slug, desc) }
            )
        }
    }
}

@Composable
private fun EditableTextPanel(value: String, onValueChange: (String) -> Unit, onSave: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(top = 12.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace)
        )
        Button(onClick = onSave, modifier = Modifier.align(Alignment.End).padding(top = 8.dp)) {
            Text("Simpan")
        }
    }
}

@Composable
private fun SkillsTab(
    skills: List<String>,
    selectedContent: String,
    onSelect: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onSave: (String) -> Unit,
    onCreateNew: (String, String) -> Unit
) {
    var selected by remember { mutableStateOf<String?>(null) }
    var showCreate by remember { mutableStateOf(false) }

    Row(Modifier.fillMaxSize()) {
        Column(Modifier.width(140.dp)) {
            TextButton(onClick = { showCreate = true }) { Text("+ Skill baru") }
            LazyColumn {
                items(skills) { slug ->
                    TextButton(onClick = { selected = slug; onSelect(slug) }) {
                        Text(slug, maxLines = 1)
                    }
                }
            }
        }
        Column(Modifier.weight(1f)) {
            selected?.let { slug ->
                EditableTextPanel(selectedContent, onContentChange, { onSave(slug) })
            } ?: Text("Pilih skill di kiri, atau buat baru", Modifier.padding(16.dp))
        }
    }

    if (showCreate) {
        var newSlug by remember { mutableStateOf("") }
        var newDesc by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("Skill baru") },
            text = {
                Column {
                    OutlinedTextField(newSlug, { newSlug = it }, label = { Text("Slug (mis. my-custom-rule)") })
                    OutlinedTextField(newDesc, { newDesc = it }, label = { Text("Deskripsi singkat") })
                }
            },
            confirmButton = {
                TextButton(onClick = { onCreateNew(newSlug, newDesc); showCreate = false }) { Text("Buat") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Batal") } }
        )
    }
}
