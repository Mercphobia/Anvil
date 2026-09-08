package com.vibe.forge.workspace

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Workspace file access with observable state. All I/O on Dispatchers.IO.
 * Tree and open-file content are exposed as StateFlows so the UI
 * auto-updates when the agent writes files.
 */
class FileRepository(private val root: File) {

    data class FileNode(
        val file: File,
        val name: String,
        val isDirectory: Boolean,
        val depth: Int,
        val children: List<FileNode> = emptyList()
    )

    private val _tree = MutableStateFlow<FileNode?>(null)
    val tree: StateFlow<FileNode?> = _tree.asStateFlow()

    private val _openFile = MutableStateFlow<File?>(null)
    val openFile: StateFlow<File?> = _openFile.asStateFlow()

    private val _openFileContent = MutableStateFlow("")
    val openFileContent: StateFlow<String> = _openFileContent.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        root.mkdirs()
    }

    suspend fun refresh() = withContext(Dispatchers.IO) {
        try {
            _tree.value = buildNode(root, 0)
            _error.value = null
        } catch (t: Throwable) {
            _error.value = t.message
        }
    }

    private fun buildNode(file: File, depth: Int): FileNode {
        val children = if (file.isDirectory && depth < 8) {
            file.listFiles()
                ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                ?.map { buildNode(it, depth + 1) }
                ?: emptyList()
        } else emptyList()
        return FileNode(file, file.name, file.isDirectory, depth, children)
    }

    suspend fun open(file: File) = withContext(Dispatchers.IO) {
        try {
            if (file.isFile) {
                _openFile.value = file
                _openFileContent.value = file.readText()
                _error.value = null
            }
        } catch (t: Throwable) {
            _error.value = t.message
        }
    }

    suspend fun save(content: String) = withContext(Dispatchers.IO) {
        val file = _openFile.value ?: return@withContext
        try {
            file.writeText(content)
            _openFileContent.value = content
            _error.value = null
        } catch (t: Throwable) {
            _error.value = t.message
        }
    }

    suspend fun write(path: String, content: String) = withContext(Dispatchers.IO) {
        try {
            val target = File(root, path)
            target.parentFile?.mkdirs()
            target.writeText(content)
            refresh()
        } catch (t: Throwable) {
            _error.value = t.message
        }
    }
}
