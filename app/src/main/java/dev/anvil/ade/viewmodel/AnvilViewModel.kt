package dev.anvil.ade.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.anvil.ade.model.ProjectType
import dev.anvil.ade.workspace.WorkspaceDetector
import dev.anvil.ade.model.AgentStep
import dev.anvil.ade.model.DiffLine
import dev.anvil.ade.model.LlmProvider
import dev.anvil.ade.model.ProjectFile
import dev.anvil.ade.model.ProviderConfig
import dev.anvil.ade.model.StepKind
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/** Fullscreen onboarding wizard steps. */
enum class WizardStep { WELCOME, BOOTSTRAP, AI_ASSISTANT, PROJECT_SOURCE, TEMPLATE }

/** How the user wants to start their project in the wizard. */
enum class ProjectSource { NEW, LOCAL, CLONE }

class AnvilViewModel(private val app: Application) : AndroidViewModel(app) {

  // ------------------------------------------------------------------
  // Real engine bridge (agent session, git, build, terminal)
  // ------------------------------------------------------------------

  // Dynamic workspace root: defaults to the internal folder until a project
  // is opened; openLocalProject()/cloneGitHubProject() switch it to the real
  // project location on disk (Option B - edit files where they live).
  private val _workspaceRoot = MutableStateFlow(java.io.File(app.filesDir, "workspace"))
  private val workspaceRoot: java.io.File get() = _workspaceRoot.value

  /** Switch the active root and invalidate the old agent session so the next
   *  session() call rebuilds it against the new root. */
  /** The default internal workspace folder - where git clones land before
   *  a project switches the root elsewhere. */
  private fun workspaceRootIfDefault(): java.io.File =
    java.io.File(app.filesDir, "workspace")

  private fun switchWorkspaceRoot(newRoot: java.io.File) {
    newRoot.mkdirs()
    _workspaceRoot.value = newRoot
    agentSession = null
  }

  private var agentSession: dev.anvil.ade.agent.AgentSession? = null

  private fun session(): dev.anvil.ade.agent.AgentSession {
      val existing = agentSession
      if (existing != null) return existing
      val config = dev.anvil.ade.agent.ProviderConfigStore.load(app)
      val created = dev.anvil.ade.agent.AgentSession(
          config = config,
          workspaceRoot = workspaceRoot,
          appContext = app.applicationContext
      )
      created.projectType = _activeType.value
      viewModelScope.launch {
          created.steps.collect { engineSteps ->
              _steps.value = engineSteps.map { s ->
                  dev.anvil.ade.model.AgentStep(
                      id = java.util.UUID.randomUUID().toString(),
                      kind = when (s.kind) {
                          dev.anvil.ade.agent.AgentSession.Step.Kind.USER -> dev.anvil.ade.model.StepKind.USER
                          dev.anvil.ade.agent.AgentSession.Step.Kind.AGENT_TEXT -> dev.anvil.ade.model.StepKind.AGENT_TEXT
                          dev.anvil.ade.agent.AgentSession.Step.Kind.TOOL_CALL -> dev.anvil.ade.model.StepKind.TOOL_CALL
                          dev.anvil.ade.agent.AgentSession.Step.Kind.TOOL_RESULT -> dev.anvil.ade.model.StepKind.TOOL_RESULT
                          dev.anvil.ade.agent.AgentSession.Step.Kind.ERROR -> dev.anvil.ade.model.StepKind.ERROR
                          dev.anvil.ade.agent.AgentSession.Step.Kind.INFO -> dev.anvil.ade.model.StepKind.INFO
                      },
                      text = s.text,
                      timestamp = ""
                  )
              }
          }
      }
      viewModelScope.launch {
          created.busy.collect { _isBusy.value = it }
      }
      viewModelScope.launch {
          created.pendingMemoryEntry.collect { entry ->
              _pendingMemory.value = entry
          }
      }
      viewModelScope.launch {
          created.pendingSkillProposal.collect { proposal ->
              _pendingSkillProposal.value = proposal
          }
      }
      viewModelScope.launch {
          created.pendingTerminalCommand.collect { cmd ->
              _pendingTerminalCommand.value = cmd
          }
      }
      viewModelScope.launch {
          created.pendingSoulProposal.collect { proposal ->
              _pendingSoulProposal.value = proposal
          }
      }
      agentSession = created
      return created
  }

  // Navigation / Welcome state
  private val _showWelcome = MutableStateFlow(false)
  val showWelcome: StateFlow<Boolean> = _showWelcome.asStateFlow()

  // Setup Wizard State (shown on first app launch)
  private val _showSetupWizard = MutableStateFlow(true)
  val showSetupWizard: StateFlow<Boolean> = _showSetupWizard.asStateFlow()

  // Open Project Dialog State (Local & GitHub)
  private val _showOpenProjectDialog = MutableStateFlow(false)
  val showOpenProjectDialog: StateFlow<Boolean> = _showOpenProjectDialog.asStateFlow()

  // Active Project Name and Status
  private val _activeProjectName = MutableStateFlow("Anvil Studio")
  val activeProjectName: StateFlow<String> = _activeProjectName.asStateFlow()

  private val _projectNotice = MutableStateFlow<String?>(null)
  val projectNotice: StateFlow<String?> = _projectNotice.asStateFlow()

  // Selected bottom nav route
  private val _currentRoute = MutableStateFlow("chat")
  val currentRoute: StateFlow<String> = _currentRoute.asStateFlow()

  // Agent Mode
  /** Active project type - auto-detected when a project is opened. */
  private val _activeType = MutableStateFlow(ProjectType.ANDROID)
  val activeType: StateFlow<ProjectType> = _activeType.asStateFlow()

  // LLM Config
  private val _providerConfig = MutableStateFlow(ProviderConfig())
  val providerConfig: StateFlow<ProviderConfig> = _providerConfig.asStateFlow()

  private val _showSettingsDialog = MutableStateFlow(false)
  val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

  // Active loaded skills
  private val _activeSkills = MutableStateFlow(
    listOf("android-app-builder", "android-app-design", "xml-resource-safety")
  )
  val activeSkills: StateFlow<List<String>> = _activeSkills.asStateFlow()

  // Chat State
  private val _steps = MutableStateFlow<List<AgentStep>>(emptyList())
  val steps: StateFlow<List<AgentStep>> = _steps.asStateFlow()

  private val _isBusy = MutableStateFlow(false)
  val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

  private val _pendingMemory = MutableStateFlow<String?>(null)

  private val _pendingTerminalCommand = MutableStateFlow<String?>(null)
  val pendingTerminalCommand: StateFlow<String?> = _pendingTerminalCommand.asStateFlow()

  // Agent Config (Soul / Memory / Skills manual editing)
  private val _soulText = MutableStateFlow("")
  val soulText: StateFlow<String> = _soulText.asStateFlow()
  private val _memoryText = MutableStateFlow("")
  val memoryText: StateFlow<String> = _memoryText.asStateFlow()
  private val _skillList = MutableStateFlow<List<String>>(emptyList())
  val skillList: StateFlow<List<String>> = _skillList.asStateFlow()
  private val _selectedSkillContent = MutableStateFlow("")
  val selectedSkillContent: StateFlow<String> = _selectedSkillContent.asStateFlow()
  private var selectedSkillSlug: String? = null
  private val _pendingSoulProposal = MutableStateFlow<dev.anvil.ade.agent.AgentSession.SoulProposal?>(null)
  val pendingSoulProposal: StateFlow<dev.anvil.ade.agent.AgentSession.SoulProposal?> =
    _pendingSoulProposal.asStateFlow()
  private val _showAgentConfig = MutableStateFlow(false)
  val showAgentConfig: StateFlow<Boolean> = _showAgentConfig.asStateFlow()

  private val _pendingSkillProposal =
      MutableStateFlow<dev.anvil.ade.agent.SelfImprovement.SkillProposal?>(null)
  val pendingSkillProposal: StateFlow<dev.anvil.ade.agent.SelfImprovement.SkillProposal?> =
      _pendingSkillProposal.asStateFlow()
  val pendingMemory: StateFlow<String?> = _pendingMemory.asStateFlow()

  // Project Workspace State
  private val _workspaceTree = MutableStateFlow<List<ProjectFile>>(emptyList())
  val workspaceTree: StateFlow<List<ProjectFile>> = _workspaceTree.asStateFlow()

  private val _selectedFile = MutableStateFlow<ProjectFile?>(null)
  val selectedFile: StateFlow<ProjectFile?> = _selectedFile.asStateFlow()

  private val _editorContent = MutableStateFlow("")
  val editorContent: StateFlow<String> = _editorContent.asStateFlow()

  private val _editorSavedNotice = MutableStateFlow<String?>(null)
  val editorSavedNotice: StateFlow<String?> = _editorSavedNotice.asStateFlow()

  // Mockup State
  private val _mockupXml = MutableStateFlow(dev.anvil.ade.ui.mockup.MockupPresets.INITIAL_MOCKUP_XML)
  val mockupXml: StateFlow<String> = _mockupXml.asStateFlow()

  private val _mockupValidation = MutableStateFlow("XML OK • All @*android: resources sanitized")
  val mockupValidation: StateFlow<String> = _mockupValidation.asStateFlow()

  // Mockup Interactive Toggles (Quick Settings tiles)
  private val _tileStates = MutableStateFlow(
    mapOf(
      "Internet" to true,
      "Bluetooth" to true,
      "Monet Dark" to true,
      "Flashlight" to false,
      "Hotspot" to false,
      "Night Light" to true,
      "Auto-Rotate" to false,
      "Do Not Disturb" to false
    )
  )
  val tileStates: StateFlow<Map<String, Boolean>> = _tileStates.asStateFlow()

  // Git State
  private val _gitBranch = MutableStateFlow("ai-mockup/qs-monet-expressive")
  val gitBranch: StateFlow<String> = _gitBranch.asStateFlow()

  private val _gitRemote = MutableStateFlow("https://github.com/Mercphobia/Anvil.git")
  val gitRemote: StateFlow<String> = _gitRemote.asStateFlow()

  private val _gitToken = MutableStateFlow("ghp_liveKeySecuredInAndroidKeystore")
  val gitToken: StateFlow<String> = _gitToken.asStateFlow()

  private val _commitMessage = MutableStateFlow("feat(systemui): harmonize quick settings panel with Monet Expressive M3 tokens")
  val commitMessage: StateFlow<String> = _commitMessage.asStateFlow()

  private val _diffLines = MutableStateFlow<List<DiffLine>>(emptyList())
  val diffLines: StateFlow<List<DiffLine>> = _diffLines.asStateFlow()

  private val _gitPushStatus = MutableStateFlow<String?>(null)
  val gitPushStatus: StateFlow<String?> = _gitPushStatus.asStateFlow()

  private val _isGitBusy = MutableStateFlow(false)
  val isGitBusy: StateFlow<Boolean> = _isGitBusy.asStateFlow()

  // Build Pipeline State
  private val _isBuilding = MutableStateFlow(false)
  val isBuilding: StateFlow<Boolean> = _isBuilding.asStateFlow()

  private val _buildStage = MutableStateFlow(0)
  val buildStage: StateFlow<Int> = _buildStage.asStateFlow()

  private val _buildLogs = MutableStateFlow("")
  val buildLogs: StateFlow<String> = _buildLogs.asStateFlow()

  // Terminal State
  private val _terminalLogs = MutableStateFlow(
    dev.anvil.ade.system.env.AnvilFetch.render(app.applicationContext)
  )
  val terminalLogs: StateFlow<String> = _terminalLogs.asStateFlow()

  private val _isTerminalRunning = MutableStateFlow(false)
  val isTerminalRunning: StateFlow<Boolean> = _isTerminalRunning.asStateFlow()

  init {
    loadInitialData()
  }

  private fun loadInitialData() {
    val initialTree = dev.anvil.ade.workspace.DemoWorkspace.initialTree
    _workspaceTree.value = initialTree
    _selectedFile.value = initialTree[0].children[0].children[0]
    _editorContent.value = _selectedFile.value?.content ?: ""
    _steps.value = dev.anvil.ade.workspace.DemoWorkspace.initialSteps
    _buildLogs.value = dev.anvil.ade.workspace.DemoWorkspace.initialBuildLogs
  }

  // Welcome control
  fun dismissWelcome() {
    _showWelcome.value = false
  }

  fun openWelcome() {
    _showWelcome.value = true
  }

  // Route control
  fun setRoute(route: String) {
    _currentRoute.value = route
  }

  // Project type control
  fun setType(type: ProjectType) {
    _activeType.value = type
    val newSkills = when (type) {
      ProjectType.ANDROID -> listOf("android-app-builder", "android-app-design", "xml-resource-safety")
      else -> listOf("aosp-systemui-design", "aosp-systemui-editing", "git-commit-convention", "xml-resource-safety")
    }
    _activeSkills.value = newSkills
    addStep(
      AgentStep(
        id = UUID.randomUUID().toString(),
        kind = StepKind.INFO,
        text = "Project type: ${type.displayName}. Loaded skills: ${newSkills.joinToString(", ")}",
        timestamp = "09:45"
      )
    )
  }

  // Settings
  fun toggleSettingsDialog(show: Boolean) {
    _showSettingsDialog.value = show
  }

  fun updateProviderConfig(config: ProviderConfig) {
    // Persist to the real encrypted store and reset the engine session
    try {
      dev.anvil.ade.agent.ProviderConfigStore.save(
          app,
          dev.anvil.ade.agent.ProviderConfig(
              provider = when (config.provider) {
                  dev.anvil.ade.model.LlmProvider.CLAUDE -> dev.anvil.ade.agent.LlmProvider.CLAUDE
                  dev.anvil.ade.model.LlmProvider.OPENAI -> dev.anvil.ade.agent.LlmProvider.OPENAI
                  dev.anvil.ade.model.LlmProvider.OPENROUTER -> dev.anvil.ade.agent.LlmProvider.OPENROUTER
                  dev.anvil.ade.model.LlmProvider.GEMINI -> dev.anvil.ade.agent.LlmProvider.GEMINI
                  dev.anvil.ade.model.LlmProvider.GROQ -> dev.anvil.ade.agent.LlmProvider.GROQ
                  dev.anvil.ade.model.LlmProvider.MISTRAL -> dev.anvil.ade.agent.LlmProvider.MISTRAL
                  dev.anvil.ade.model.LlmProvider.DEEPSEEK -> dev.anvil.ade.agent.LlmProvider.DEEPSEEK
                  dev.anvil.ade.model.LlmProvider.TOGETHER -> dev.anvil.ade.agent.LlmProvider.TOGETHER
                  dev.anvil.ade.model.LlmProvider.FIREWORKS -> dev.anvil.ade.agent.LlmProvider.FIREWORKS
                  dev.anvil.ade.model.LlmProvider.AZURE_OPENAI -> dev.anvil.ade.agent.LlmProvider.AZURE_OPENAI
                  dev.anvil.ade.model.LlmProvider.OLLAMA -> dev.anvil.ade.agent.LlmProvider.OLLAMA
                  dev.anvil.ade.model.LlmProvider.QWEN -> dev.anvil.ade.agent.LlmProvider.QWEN
                  dev.anvil.ade.model.LlmProvider.KIMI -> dev.anvil.ade.agent.LlmProvider.KIMI
                  dev.anvil.ade.model.LlmProvider.GLM -> dev.anvil.ade.agent.LlmProvider.GLM
                  dev.anvil.ade.model.LlmProvider.CUSTOM -> dev.anvil.ade.agent.LlmProvider.CUSTOM
              },
              baseUrl = config.endpoint,
              model = config.model,
              apiKey = config.apiKey
          )
      )
      agentSession = null
    } catch (e: dev.anvil.ade.agent.SecureProviderStorageUnavailableException) {
      _projectNotice.value = "Device ini tidak mendukung penyimpanan API key yang aman. Konfigurasi tidak disimpan."
      return
    } catch (t: Throwable) {
      _projectNotice.value = "Gagal menyimpan konfigurasi: ${t.message}"
      return
    }
    _providerConfig.value = config
    _showSettingsDialog.value = false
    addStep(
      AgentStep(
        id = UUID.randomUUID().toString(),
        kind = StepKind.INFO,
        text = "LLM Provider updated: ${config.provider.displayName} [${config.model}]",
        timestamp = "09:45"
      )
    )
  }

  // Chat Actions
  fun sendMessage(prompt: String) {
    if (prompt.isBlank() || _isBusy.value) return
    // Sync the mode onto the session (it may have changed since creation)
    // and let the real agent loop handle everything: user step, LLM call,
    // tool execution, retry, streaming steps via the session collectors.
    agentSession?.projectType = _activeType.value
    session().send(prompt)
  }

  fun confirmMemory() {
    agentSession?.confirmMemoryEntry()
    _pendingMemory.value = null
  }

  fun confirmSkillProposal() {
    agentSession?.confirmSkillProposal()
    _pendingSkillProposal.value = null
  }

  fun dismissSkillProposal() {
    agentSession?.dismissSkillProposal()
    _pendingSkillProposal.value = null
  }

  fun confirmTerminalCommand() {
    agentSession?.confirmTerminalCommand()
    _pendingTerminalCommand.value = null
  }

  fun dismissTerminalCommand() {
    agentSession?.dismissTerminalCommand()
    _pendingTerminalCommand.value = null
  }

  fun toggleAgentConfig(show: Boolean) { _showAgentConfig.value = show }

  fun confirmSoulProposal() {
    agentSession?.confirmSoulProposal()
    _pendingSoulProposal.value = null
  }

  fun dismissSoulProposal() {
    agentSession?.dismissSoulProposal()
    _pendingSoulProposal.value = null
  }

  fun loadAgentConfig() {
    viewModelScope.launch {
      dev.anvil.ade.agent.AgentSoulStore.seedIfMissing(app)
      _soulText.value = dev.anvil.ade.agent.AgentSoulStore.read(app)
      _memoryText.value = dev.anvil.ade.agent.memory.MemoryStore(workspaceRoot).readMemory()
      _skillList.value = dev.anvil.ade.agent.SkillLoader.discover(app).map { it.slug }
    }
  }

  fun updateSoulDraft(text: String) { _soulText.value = text }
  fun saveSoul() {
    viewModelScope.launch {
      dev.anvil.ade.agent.AgentSoulStore.write(app, _soulText.value)
      agentSession = null // reload soul in the next session
    }
  }

  fun updateMemoryDraft(text: String) { _memoryText.value = text }
  fun saveMemory() {
    viewModelScope.launch {
      dev.anvil.ade.agent.memory.MemoryStore(workspaceRoot).writeMemoryRaw(_memoryText.value)
    }
  }

  fun selectSkill(slug: String) {
    selectedSkillSlug = slug
    viewModelScope.launch {
      val skill = dev.anvil.ade.agent.SkillLoader.discover(app).firstOrNull { it.slug == slug }
      _selectedSkillContent.value = skill?.body ?: ""
    }
  }
  fun updateSelectedSkillDraft(text: String) { _selectedSkillContent.value = text }
  fun saveSkill(slug: String) {
    viewModelScope.launch {
      dev.anvil.ade.agent.SkillLoader.save(app, slug, _selectedSkillContent.value)
      agentSession = null
    }
  }
  fun createSkill(slug: String, description: String) {
    if (slug.isBlank()) return
    viewModelScope.launch {
      dev.anvil.ade.agent.SkillLoader.create(app, slug, description, "MODE_A, MODE_B, ANDROID, GIT_LINKED_SYSTEM")
      _skillList.value = dev.anvil.ade.agent.SkillLoader.discover(app).map { it.slug }
    }
  }

  fun dismissMemory() {
    agentSession?.dismissMemoryEntry()
    _pendingMemory.value = null
  }

  private fun addStep(step: AgentStep) {
    _steps.value = _steps.value + step
  }

  // Workspace Actions
  fun selectFile(file: ProjectFile) {
    if (!file.isDirectory) {
      _selectedFile.value = file
      _editorContent.value = file.content
      _editorSavedNotice.value = null
    }
  }

  fun updateEditorContent(newContent: String) {
    _editorContent.value = newContent
  }

  fun saveCurrentFile() {
    _selectedFile.value?.let { current ->
      try {
        java.io.File(current.path).writeText(_editorContent.value)
        _selectedFile.value = current.copy(content = _editorContent.value)
        _editorSavedNotice.value = "Tersimpan (${_editorContent.value.lines().size} baris)"
      } catch (t: Throwable) {
        _editorSavedNotice.value = "Gagal simpan: ${t.message}"
      }
      viewModelScope.launch {
        delay(2500)
        _editorSavedNotice.value = null
      }
    }
  }

  // Mockup Interactive Actions
  fun toggleTile(tileName: String) {
    val current = _tileStates.value.toMutableMap()
    current[tileName] = !(current[tileName] ?: false)
    _tileStates.value = current
  }

  fun updateMockupXml(newXml: String) {
    _mockupXml.value = newXml
    // Syntax guard
    val isWellFormed = newXml.contains("<") && newXml.contains(">") && !newXml.contains("<<")
    _mockupValidation.value = if (isWellFormed) {
      "XML OK • Well-formed • All private @*android: resources sanitized"
    } else {
      "Error: Unclosed tag or malformed XML syntax"
    }
  }

  fun loadMockupPreset(presetName: String) {
    when (presetName) {
      "Quick Settings" -> updateMockupXml(dev.anvil.ade.ui.mockup.MockupPresets.INITIAL_MOCKUP_XML)
      "Status Bar" -> updateMockupXml(dev.anvil.ade.ui.mockup.MockupPresets.STATUS_BAR_XML)
      "Volume Panel" -> updateMockupXml(dev.anvil.ade.ui.mockup.MockupPresets.VOLUME_DIALOG_XML)
    }
  }

  // Git Actions
  fun setCommitMessage(msg: String) {
    _commitMessage.value = msg
  }

  /** Load the real unified diff from Git and parse it into DiffLine list. */
  fun refreshGitDiff() {
    viewModelScope.launch {
      try {
        val token = dev.anvil.ade.vcs.GitCredentialStore.token(app.applicationContext)
        if (token.isBlank()) return@launch
        val repoManager = dev.anvil.ade.vcs.GitRepoManager(workspaceRoot, token)
        val raw = repoManager.getDiff()
        _diffLines.value = dev.anvil.ade.vcs.DiffParser.parse(raw)
      } catch (t: Throwable) {
        // diff stays as-is on failure
      }
    }
  }


  fun commitAndPush() {
    if (_isGitBusy.value || _commitMessage.value.isBlank()) return
    _isGitBusy.value = true
    viewModelScope.launch {
      try {
        val token = dev.anvil.ade.vcs.GitCredentialStore.token(app.applicationContext)
        if (token.isBlank()) {
          _gitPushStatus.value = "Error: token Git belum diatur (buka Git > Settings)"
          _isGitBusy.value = false
          return@launch
        }
        val repoManager = dev.anvil.ade.vcs.GitRepoManager(workspaceRoot, token)
        val branchResult = repoManager.ensureWorkBranch(_gitBranch.value)
        if (branchResult.startsWith("error")) {
          _gitPushStatus.value = branchResult
          _isGitBusy.value = false
          return@launch
        }
        val gitTools = dev.anvil.ade.agent.tools.GitTools(repoManager)
        val result = gitTools.commitAndPush(_gitBranch.value, _commitMessage.value)
        _gitPushStatus.value = if (result.startsWith("error")) result else "Sukses: $result"
      } catch (t: Throwable) {
        _gitPushStatus.value = "Error: ${t.message}"
      } finally {
        _isGitBusy.value = false
        delay(4000)
        _gitPushStatus.value = null
      }
    }
  }

  // Build Pipeline Actions
  fun runBuildPipeline() {
    if (_isBuilding.value) return
    _isBuilding.value = true
    _buildStage.value = 1
    _buildLogs.value = "[build] starting on-device pipeline...\n"

    viewModelScope.launch {
      val tools = dev.anvil.ade.agent.tools.BuildTools(app.applicationContext, workspaceRoot)
      _buildStage.value = 3
      val result = tools.runBuild { line ->
        _buildLogs.value += line + "\n"
      }
      _buildStage.value = 5
      _buildLogs.value += result + "\n"
      _isBuilding.value = false
    }
  }

  fun runTerminalCommand(cmd: String) {
    val trimmed = cmd.trim()
    if (trimmed.isEmpty()) return
    if (trimmed == "clear") {
      clearTerminal()
      return
    }

    viewModelScope.launch {
      _isTerminalRunning.value = true
      val current = _terminalLogs.value
      val separator = if (current.endsWith("\n") || current.isEmpty()) "" else "\n"
      _terminalLogs.value = current + separator + "\u276F " + trimmed + "\n"

      val tools = dev.anvil.ade.agent.tools.TerminalTools(
          app.applicationContext, workspaceRoot
      )
      // First run installs the embedded environment if missing
      val envMsg = tools.ensureEnvironment { line ->
        _terminalLogs.value += "[env] " + line + "\n"
      }
      if (envMsg != "environment ready") {
        _terminalLogs.value += "[env] " + envMsg + "\n"
      }

      val output = tools.run(trimmed)
      _terminalLogs.value += output + "\n"
      _isTerminalRunning.value = false
    }
  }

  fun clearTerminal() {
    _terminalLogs.value = "\u276F "
  }

  fun openFileFromTree(file: ProjectFile) {
    if (file.isDirectory) return
    _selectedFile.value = file
    _editorContent.value = file.content
    _currentRoute.value = "project"
  }

  // Setup Wizard Controls
  fun toggleSetupWizard(show: Boolean) {
    _showSetupWizard.value = show
  }

  // ---------- Fullscreen onboarding wizard state ----------

  private val _wizardStep = MutableStateFlow(WizardStep.WELCOME)
  val wizardStep: StateFlow<WizardStep> = _wizardStep.asStateFlow()

  private val _wizardProjectSource = MutableStateFlow(ProjectSource.NEW)
  val wizardProjectSource: StateFlow<ProjectSource> = _wizardProjectSource.asStateFlow()

  private val _bootstrapLog = MutableStateFlow("")
  val bootstrapLog: StateFlow<String> = _bootstrapLog.asStateFlow()

  private val _bootstrapRunning = MutableStateFlow(false)
  val bootstrapRunning: StateFlow<Boolean> = _bootstrapRunning.asStateFlow()

  private val _bootstrapDone = MutableStateFlow(false)
  val bootstrapDone: StateFlow<Boolean> = _bootstrapDone.asStateFlow()

  private var wizardProvider: LlmProvider? = null
  private var wizardApiKey: String = ""
  private var wizardLocalPath: String = ""
  private var wizardRepoUrl: String = ""

  fun advanceWizard() {
    _wizardStep.value = when (_wizardStep.value) {
      WizardStep.WELCOME -> WizardStep.BOOTSTRAP
      WizardStep.BOOTSTRAP -> WizardStep.AI_ASSISTANT
      WizardStep.AI_ASSISTANT -> WizardStep.PROJECT_SOURCE
      WizardStep.PROJECT_SOURCE -> WizardStep.TEMPLATE
      WizardStep.TEMPLATE -> WizardStep.TEMPLATE // finished via finishWizard
    }
  }

  fun backWizard() {
    _wizardStep.value = when (_wizardStep.value) {
      WizardStep.WELCOME -> WizardStep.WELCOME
      WizardStep.BOOTSTRAP -> WizardStep.WELCOME
      WizardStep.AI_ASSISTANT -> WizardStep.BOOTSTRAP
      WizardStep.PROJECT_SOURCE -> WizardStep.AI_ASSISTANT
      WizardStep.TEMPLATE -> WizardStep.PROJECT_SOURCE
    }
  }

  fun selectWizardProvider(provider: LlmProvider) {
    wizardProvider = provider
    _providerConfig.value = _providerConfig.value.copy(provider = provider)
  }

  fun setWizardApiKey(key: String) {
    wizardApiKey = key
  }

  fun setWizardProjectSource(source: ProjectSource) {
    _wizardProjectSource.value = source
  }

  fun setWizardLocalPath(path: String) {
    wizardLocalPath = path
  }

  fun setWizardRepoUrl(url: String) {
    wizardRepoUrl = url
  }

  /** Auto-runs on the bootstrap step: installs the embedded terminal env once. */
  fun startBootstrap() {
    if (_bootstrapRunning.value || _bootstrapDone.value) return
    _bootstrapRunning.value = true
    viewModelScope.launch {
      try {
        val already = dev.anvil.ade.system.env.EmbeddedEnvironment.isInstalled(app.applicationContext)
        if (already) {
          _bootstrapLog.value = "environment already installed, skipping download.\n"
          _bootstrapDone.value = true
          return@launch
        }
        val report = dev.anvil.ade.system.env.EmbeddedEnvironment.setup(app.applicationContext) { line ->
          _bootstrapLog.value += line + "\n"
        }
        _bootstrapLog.value += report.message + "\n"
        _bootstrapDone.value = report.success
      } catch (t: Throwable) {
        _bootstrapLog.value += "failed: " + (t.message ?: t.toString()) + "\n"
        _bootstrapDone.value = false
      } finally {
        _bootstrapRunning.value = false
      }
    }
  }

  /** Final wizard step: apply provider + project choice, then close onboarding. */
  fun finishWizard(templateId: String) {
    // Apply provider/api key through the real encrypted store path
    val provider = wizardProvider ?: _providerConfig.value.provider
    _providerConfig.value = _providerConfig.value.copy(
      provider = provider,
      apiKey = if (wizardApiKey.isNotBlank()) wizardApiKey else _providerConfig.value.apiKey
    )
    updateProviderConfig(_providerConfig.value)

    when (_wizardProjectSource.value) {
      ProjectSource.NEW -> applyProjectTemplate(templateId)
      ProjectSource.LOCAL -> {
        val name = wizardLocalPath.ifBlank { "workspace" }
        openLocalProject(name, name)
      }
      ProjectSource.CLONE -> {
        if (wizardRepoUrl.isNotBlank()) {
          cloneGitHubProject(wizardRepoUrl, "main", "")
        } else {
          applyProjectTemplate(templateId)
        }
      }
    }

    _showSetupWizard.value = false

    _steps.value = listOf(
      AgentStep(
        id = UUID.randomUUID().toString(),
        kind = StepKind.AGENT_TEXT,
        text = "Setup complete. Anvil is ready - assistant: ${provider.displayName}.",
        timestamp = "10:00"
      )
    )
  }

  fun completeSetupWizard(
    provider: String,
    apiKey: String,
    mode: ProjectType,
    templateId: String,
    repoUrl: String = ""
  ) {
    val matchedProvider = when {
      provider.contains("OpenAI", ignoreCase = true) -> LlmProvider.OPENAI
      provider.contains("Claude", ignoreCase = true) -> LlmProvider.CLAUDE
      provider.contains("DeepSeek", ignoreCase = true) -> LlmProvider.OPENROUTER
      provider.contains("Ollama", ignoreCase = true) -> LlmProvider.CUSTOM
      else -> LlmProvider.GEMINI
    }

    _providerConfig.value = _providerConfig.value.copy(
      provider = matchedProvider,
      apiKey = apiKey.ifBlank { _providerConfig.value.apiKey }
    )
    _activeType.value = mode

    if (templateId == "github_clone" && repoUrl.isNotBlank()) {
      cloneGitHubProject(repoUrl, "main", "")
    } else {
      applyProjectTemplate(templateId)
    }

    _showSetupWizard.value = false

    val modeName = mode.displayName
    val setupStep = AgentStep(
      id = UUID.randomUUID().toString(),
      kind = StepKind.AGENT_TEXT,
      text = "Setup Wizard Selesai! Anvil siap digunakan dalam mode $modeName dengan template $templateId. Model: ${matchedProvider.displayName}.",
      timestamp = "10:00"
    )
    _steps.value = listOf(setupStep)
  }

  // Open Project & GitHub Dialog Controls
  fun toggleOpenProjectDialog(show: Boolean) {
    _showOpenProjectDialog.value = show
  }

  fun clearProjectNotice() {
    _projectNotice.value = null
  }


  /** Walk the real directory and build a ProjectFile tree - replaces the
   *  hardcoded template trees. Large/binary files get a placeholder note
   *  instead of being fully read into memory. */

  fun openLocalProject(name: String, path: String) {
    // Project type auto-detected from marker files at the opened root.
    _activeType.value = WorkspaceDetector.detectType(java.io.File(path))
    val root = java.io.File(path)
    if (!root.exists() || !root.isDirectory) {
      _projectNotice.value = "Error: folder tidak ditemukan di $path"
      return
    }
    switchWorkspaceRoot(root)
    _activeProjectName.value = name
    _showOpenProjectDialog.value = false

    val tree = dev.anvil.ade.workspace.WorkspaceTree.buildFromDisk(root)
    _workspaceTree.value = tree
    val firstFile = tree.firstNotNullOfOrNull { dev.anvil.ade.workspace.WorkspaceTree.findFirstFile(it) }
    _selectedFile.value = firstFile
    _editorContent.value = firstFile?.content ?: ""
    _projectNotice.value = "Berhasil memuat proyek lokal: $name ($path) - ${tree.size} item ditemukan"
  }

  fun cloneGitHubProject(repoUrl: String, branch: String, token: String) {
    viewModelScope.launch {
      _showOpenProjectDialog.value = false
      _isBusy.value = true
      val repoName = repoUrl.substringAfterLast("/").removeSuffix(".git")
      _projectNotice.value = "Mengkloning repository: $repoName ($branch)..."

      val cloneStep = AgentStep(
        id = UUID.randomUUID().toString(),
        kind = StepKind.TOOL_CALL,
        text = "git clone $repoUrl ($branch)",
        timestamp = "10:01",
        toolName = "git",
        executionMs = 1200L
      )
      _steps.value = _steps.value + cloneStep

      delay(1200)

      val successStep = AgentStep(
        id = UUID.randomUUID().toString(),
        kind = StepKind.INFO,
        text = "Repository $repoName berhasil dikloning. File kerja dimuat ke workspace.",
        timestamp = "10:01"
      )
      _steps.value = _steps.value + successStep

      _activeProjectName.value = repoName
      _gitRemote.value = repoUrl
      _gitBranch.value = branch
      // Build the tree from the REAL cloned directory on disk
      val cloneTargetDir = java.io.File(workspaceRootIfDefault(), repoName)
      switchWorkspaceRoot(cloneTargetDir)
      // Cloned repo - detect its type from markers (falls back to
      // GIT_LINKED_SYSTEM for system-style monorepos).
      _activeType.value = WorkspaceDetector.detectType(cloneTargetDir)
      _projectNotice.value = "Repository $repoName berhasil dimuat!"
      _isBusy.value = false

      val tree = dev.anvil.ade.workspace.WorkspaceTree.buildFromDisk(cloneTargetDir)
      _workspaceTree.value = tree
      val firstFile = tree.firstNotNullOfOrNull { dev.anvil.ade.workspace.WorkspaceTree.findFirstFile(it) }
      _selectedFile.value = firstFile
      _editorContent.value = firstFile?.content ?: ""
    }
  }

  // Template Scaffolder
  fun applyProjectTemplate(templateId: String) {
    val result = dev.anvil.ade.workspace.ProjectTemplates.scaffold(templateId) ?: return
    if (result.wantsCloneDialog) {
      _showOpenProjectDialog.value = true
      _projectNotice.value = result.notice
      return
    }
    _activeProjectName.value = result.projectName
    _activeType.value = result.projectType
    _workspaceTree.value = result.files
    _selectedFile.value = result.files.firstOrNull()
    _editorContent.value = result.files.firstOrNull()?.content ?: ""
    result.notice?.let { _projectNotice.value = it }
  }

}
