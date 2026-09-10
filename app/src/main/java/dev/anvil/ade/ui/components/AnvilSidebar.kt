package dev.anvil.ade.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.model.DiffLine
import dev.anvil.ade.model.ProjectFile
import dev.anvil.ade.model.StepKind
import dev.anvil.ade.viewmodel.AnvilViewModel
import dev.anvil.ade.ui.theme.GeistMono

/** One changed file derived from the live unified diff (path + +/- counts). */
internal data class GitChangedFile(
    val path: String,
    val status: String,
    val additions: Int,
    val deletions: Int
)

/** Extract per-file change summaries from the parsed unified diff. */
internal fun parseChangedFiles(diffLines: List<DiffLine>): List<GitChangedFile> {
    val out = mutableListOf<GitChangedFile>()
    var current: GitChangedFile? = null
    for (line in diffLines) {
        val text = line.text.trim()
        if (text.startsWith("+++ ")) {
            current?.let { out.add(it) }
            current = GitChangedFile(
                path = text.removePrefix("+++ ").removePrefix("b/").trim(),
                status = "M",
                additions = 0,
                deletions = 0
            )
        } else if (current != null) {
            current = when (line.type) {
                DiffLine.Type.ADD -> current.copy(additions = current.additions + 1)
                DiffLine.Type.DELETE -> current.copy(deletions = current.deletions + 1)
                else -> current
            }
        }
    }
    current?.let { out.add(it) }
    return out.map { f ->
        when {
            f.additions > 0 && f.deletions == 0 -> f.copy(status = "A")
            f.additions == 0 && f.deletions > 0 -> f.copy(status = "D")
            else -> f
        }
    }
}

/**
 * Reusable sidebar shell (anvil_ui spec Bagian 3.0): header with the Anvil
 * logo mark + active project name, scrollable body, optional extra header
 * row and footer. All list content uses [AnvilListRow].
 */
@Composable
fun AnvilSidebar(
    projectName: String,
    modifier: Modifier = Modifier,
    headerExtra: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = projectName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        headerExtra?.invoke()
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            content()
        }
        footer?.invoke()
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        fontFamily = GeistMono,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun EmptyHint(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 12.dp)
    )
}

// ---------- 3.1 Chat Sidebar ----------

@Composable
fun ChatSidebar(viewModel: AnvilViewModel, onClose: () -> Unit) {
    val projectName by viewModel.activeProjectName.collectAsState()
    val steps by viewModel.steps.collectAsState()

    AnvilSidebar(
        projectName = projectName,
        footer = {
            AnvilListRow(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                },
                title = "New Chat",
                onClick = {
                    viewModel.newChat()
                    onClose()
                },
                showDivider = false
            )
        }
    ) {
        val firstUser = steps.firstOrNull { it.kind == StepKind.USER }
        if (firstUser == null) {
            EmptyHint("Belum ada percakapan. Riwayat sesi akan muncul di sini.")
        } else {
            AnvilListRow(
                leadingIcon = { AnvilRowLetter(projectName.take(1).uppercase()) },
                title = firstUser.text,
                subtitle = "Sesi aktif · ${steps.size} langkah",
                onClick = onClose,
                showDivider = false
            )
        }
    }
}

// ---------- 3.2 Project Sidebar ----------

@Composable
fun ProjectSidebar(viewModel: AnvilViewModel, onClose: () -> Unit) {
    val projectName by viewModel.activeProjectName.collectAsState()
    val workspaceTree by viewModel.workspaceTree.collectAsState()
    val selectedFile by viewModel.selectedFile.collectAsState()
    val diffLines by viewModel.diffLines.collectAsState()
    val changed = remember(diffLines) { parseChangedFiles(diffLines) }

    var collapsedDirs by remember { mutableStateOf(setOf<String>()) }

    AnvilSidebar(
        projectName = projectName,
        headerExtra = if (changed.isNotEmpty()) {
            {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.setRoute("git")
                            onClose()
                        }
                        .padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${changed.size} files changed",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = GeistMono,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                }
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        } else null
    ) {
        workspaceTree.forEach { item ->
            FileTreeRow(
                item = item,
                selectedFile = selectedFile,
                collapsedDirs = collapsedDirs,
                onToggleDir = { path ->
                    collapsedDirs = if (path in collapsedDirs) collapsedDirs - path else collapsedDirs + path
                },
                onFileClick = { file ->
                    viewModel.openFileFromTree(file)
                    onClose()
                },
                depth = 0
            )
        }
    }
}

@Composable
private fun FileTreeRow(
    item: ProjectFile,
    selectedFile: ProjectFile?,
    collapsedDirs: Set<String>,
    onToggleDir: (String) -> Unit,
    onFileClick: (ProjectFile) -> Unit,
    depth: Int
) {
    if (item.isDirectory) {
        val isCollapsed = item.path in collapsedDirs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleDir(item.path) }
                .padding(
                    start = 14.dp + (depth * 12).dp,
                    end = 14.dp,
                    top = 8.dp,
                    bottom = 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = item.name,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (isCollapsed) Icons.Filled.KeyboardArrowRight else Icons.Filled.KeyboardArrowDown,
                contentDescription = if (isCollapsed) "Expand" else "Collapse",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
        }
        if (!isCollapsed) {
            item.children.forEach { child ->
                FileTreeRow(child, selectedFile, collapsedDirs, onToggleDir, onFileClick, depth + 1)
            }
        }
    } else {
        AnvilListRow(
            leadingIcon = { AnvilRowLetter(fileTypeInitial(item.name)) },
            title = item.name,
            subtitle = if (item.content.isNotEmpty()) "${item.content.lines().size} lines" else item.path,
            titleMono = true,
            onClick = { onFileClick(item) },
            modifier = Modifier.padding(start = (depth * 12).dp),
            showDivider = false
        )
    }
}

private fun fileTypeInitial(name: String): String {
    val ext = name.substringAfterLast('.', "").lowercase()
    return when (ext) {
        "java", "json", "js" -> "J"
        "kt", "kts" -> "K"
        "xml" -> "X"
        "gradle", "go" -> "G"
        "md" -> "M"
        "py", "properties" -> "P"
        "rs" -> "R"
        "ts", "toml" -> "T"
        "c", "h", "cpp" -> "C"
        "sh" -> "S"
        else -> if (ext.length == 1) ext.uppercase() else "•"
    }
}

// ---------- 3.3 Git Sidebar ----------

@Composable
fun GitSidebar(viewModel: AnvilViewModel, onClose: () -> Unit) {
    val projectName by viewModel.activeProjectName.collectAsState()
    val branch by viewModel.gitBranch.collectAsState()
    val diffLines by viewModel.diffLines.collectAsState()
    val changed = remember(diffLines) { parseChangedFiles(diffLines) }

    AnvilSidebar(projectName = projectName) {
        SectionLabel("BRANCHES")
        AnvilListRow(
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.CallSplit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
            },
            title = branch,
            titleMono = true,
            trailing = {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Active branch",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )
            },
            onClick = onClose,
            showDivider = false
        )
        EmptyHint("Daftar branch lain menyusul — branch aktif ditampilkan di atas.")

        SectionLabel("CHANGED FILES")
        if (changed.isEmpty()) {
            EmptyHint("Tidak ada perubahan belum di-commit.")
        } else {
            changed.forEachIndexed { idx, file ->
                AnvilListRow(
                    leadingIcon = { AnvilRowLetter(file.status) },
                    title = file.path.substringAfterLast('/'),
                    subtitle = "${file.path} · +${file.additions} -${file.deletions}",
                    titleMono = true,
                    onClick = {
                        viewModel.setRoute("git")
                        onClose()
                    },
                    showDivider = idx != changed.lastIndex
                )
            }
        }
    }
}

// ---------- 3.4 Terminal Sidebar ----------

@Composable
fun TerminalSidebar(viewModel: AnvilViewModel, onClose: () -> Unit) {
    val projectName by viewModel.activeProjectName.collectAsState()
    val history by viewModel.terminalHistory.collectAsState()

    AnvilSidebar(projectName = projectName) {
        SectionLabel("COMMAND HISTORY")
        if (history.isEmpty()) {
            EmptyHint("Belum ada command yang dijalankan di project ini.")
        } else {
            val recentFirst = history.asReversed()
            recentFirst.forEachIndexed { idx, entry ->
                AnvilListRow(
                    leadingIcon = { AnvilRowLetter("$") },
                    title = entry.command,
                    titleMono = true,
                    subtitle = entry.timestamp,
                    onClick = {
                        // Recall into the input bar only — user still presses
                        // send (confirmation principle).
                        viewModel.setTerminalInputDraft(entry.command)
                        viewModel.setRoute("terminal")
                        onClose()
                    },
                    showDivider = idx != recentFirst.lastIndex
                )
            }
        }
    }
}
