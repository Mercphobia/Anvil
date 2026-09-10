package dev.anvil.ade.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.model.ProjectType
import dev.anvil.ade.ui.components.*
import dev.anvil.ade.ui.screens.*
import dev.anvil.ade.ui.theme.*
import dev.anvil.ade.viewmodel.AnvilViewModel
import kotlinx.coroutines.launch

data class NavDest(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(viewModel: AnvilViewModel) {
  val currentRoute by viewModel.currentRoute.collectAsState()
  val showWelcome by viewModel.showWelcome.collectAsState()
  val showSetupWizard by viewModel.showSetupWizard.collectAsState()
  val activeType by viewModel.activeType.collectAsState()
  val providerConfig by viewModel.providerConfig.collectAsState()
  val showSettingsDialog by viewModel.showSettingsDialog.collectAsState()
  val showAgentConfig by viewModel.showAgentConfig.collectAsState()
  val isBusy by viewModel.isBusy.collectAsState()
  val isBuilding by viewModel.isBuilding.collectAsState()
  val showOpenProjectDialog by viewModel.showOpenProjectDialog.collectAsState()
  val showTemplateDialog by viewModel.showTemplateDialog.collectAsState()
  val projectName by viewModel.activeProjectName.collectAsState()
  val workspaceTree by viewModel.workspaceTree.collectAsState()
  val selectedFile by viewModel.selectedFile.collectAsState()
  val editorContent by viewModel.editorContent.collectAsState()
  val buildErrors by viewModel.buildErrors.collectAsState()
  val buildLogs = remember { mutableStateListOf<BuildLogEntry>() }

  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  val isProjectOpen = projectName.isNotEmpty() && currentRoute != "home"
  val isHomeRoute = currentRoute == "home" || (projectName.isEmpty() && currentRoute !in listOf("preferences", "ideconfig", "sdkinstall", "projectconfig", "templates", "clone"))

  // 4-tab nav
  val destinations = listOf(
    NavDest("chat", "Chat", Icons.Filled.ChatBubble),
    NavDest("project", "Files", Icons.Filled.Folder),
    NavDest("git", "Git", Icons.AutoMirrored.Filled.CallSplit),
    NavDest("terminal", "Terminal", Icons.Filled.Terminal)
  )

  // ---- OVERLAYS ----
  if (showSetupWizard) {
    dev.anvil.ade.ui.onboarding.OnboardingFlow(viewModel = viewModel)
    return
  }
  if (showSettingsDialog) {
    ProviderSettingsDialog(
      initial = providerConfig,
      onSave = { viewModel.updateProviderConfig(it) },
      onDismiss = { viewModel.toggleSettingsDialog(false) }
    )
  }
  if (showOpenProjectDialog) {
    OpenProjectDialog(
      onDismiss = { viewModel.toggleOpenProjectDialog(false) },
      onOpenLocal = { name, path -> viewModel.openLocalProject(name, path) },
      onCloneGitHub = { url, branch, token -> viewModel.cloneGitHubProject(url, branch, token) }
    )
  }
  if (showTemplateDialog) {
    ProjectTemplateDialog(
      onDismiss = { viewModel.toggleTemplateDialog(false) },
      onSelectTemplate = { viewModel.applyProjectTemplate(it) }
    )
  }
  if (showAgentConfig) {
    dev.anvil.ade.ui.screens.AgentConfigScreen(viewModel = viewModel, onClose = { viewModel.toggleAgentConfig(false) })
    return
  }

  // ---- NON-PROJECT SCREENS ----
  when (currentRoute) {
    "preferences" -> {
      PreferencesScreen(onBack = { viewModel.setRoute(if (projectName.isNotEmpty()) "project" else "home") })
      return
    }
    "ideconfig" -> {
      IdeConfigScreen(onBack = { viewModel.setRoute(if (projectName.isNotEmpty()) "project" else "home") })
      return
    }
    "sdkinstall" -> {
      SdkInstallationScreen(
        onBack = { viewModel.setRoute("home") },
        onDone = { sdk, jdk, ndk, git, ssh ->
          viewModel.installSdk(sdk, jdk, ndk, git, ssh)
          viewModel.setRoute("home")
        }
      )
      return
    }
    "projectconfig" -> {
      ProjectConfigScreen(
        onBack = { viewModel.setRoute("home") },
        onCreateProject = { name, pkg, loc, lang, sdk, kts ->
          viewModel.createProject(name, pkg, loc, lang, sdk, kts)
        }
      )
      return
    }
    "clone" -> {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
          .clickable { viewModel.setRoute("home") },
        contentAlignment = Alignment.BottomCenter
      ) {
        CloneGitSheet(
          onDismiss = { viewModel.setRoute("home") },
          onClone = { url, branch, shallow ->
            viewModel.cloneGitHubProject(url, branch, "")
            viewModel.setRoute("home")
          }
        )
      }
      return
    }
    "templates" -> {
      TemplateSelectionScreen(
        onBack = { viewModel.setRoute("projectconfig") },
        onSelectTemplate = { viewModel.applyProjectTemplate(it) }
      )
      return
    }
  }

  // ---- HOME SCREEN (no project) ----
  if (!isProjectOpen) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
      HomeScreen(
        onCreateProject = { viewModel.startNewProject() },
        onOpenProject = { viewModel.toggleOpenProjectDialog(true) },
        onCloneGit = { viewModel.setRoute("clone") },
        onOpenTerminal = { viewModel.setRoute("terminal") },
        onOpenPreferences = { viewModel.setRoute("preferences") },
        onOpenIdeConfig = { viewModel.setRoute("ideconfig") },
        onOpenDocs = { /* open docs intent */ }
      )
    }
    return
  }

  // ---- PROJECT OPEN: Sidebar + Editor + Bottom Nav ----
  val sidebarWidth: Dp = (maxOf(300, androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp) * 0.78f).dp

  ModalNavigationDrawer(
    drawerState = drawerState,
    gesturesEnabled = currentRoute in listOf("project", "chat", "git", "terminal"),
    drawerContent = {
      ModalDrawerSheet(
        modifier = Modifier.width(sidebarWidth),
        drawerContainerColor = MaterialTheme.colorScheme.surface
      ) {
        val close = { scope.launch { drawerState.close() } }
        when (currentRoute) {
          "chat" -> ChatSidebar(viewModel = viewModel, onClose = close)
          "git" -> GitSidebar(viewModel = viewModel, onClose = close)
          "terminal" -> TerminalSidebar(viewModel = viewModel, onClose = close)
          else -> ProjectSidebar(viewModel = viewModel, onClose = close)
        }
      }
    }
  ) {
    Scaffold(
      topBar = {
        TopAppBar(
          navigationIcon = {
            IconButton(onClick = { scope.launch { drawerState.open() } }) {
              Icon(Icons.Filled.Menu, "Sidebar", tint = MaterialTheme.colorScheme.onSurface)
            }
          },
          title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(projectName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
              Spacer(Modifier.width(8.dp))
              Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)) {
                  Box(modifier = Modifier.size(5.dp).clip(RoundedCornerShape(2.5.dp)).background(AcsGold))
                  Spacer(Modifier.width(5.dp))
                  Text(if (activeType == ProjectType.ANDROID) "App" else activeType.name.take(6), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
            }
          },
          actions = {
            IconButton(onClick = { viewModel.toggleSettingsDialog(true) }) {
              Icon(Icons.Filled.Tune, "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = { viewModel.toggleAgentConfig(true) }) {
              Icon(Icons.Filled.Psychology, "Agent", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )
      },
      bottomBar = {
        NavigationBar(
          containerColor = MaterialTheme.colorScheme.surfaceContainer,
          modifier = Modifier.testTag("bottom_nav")
        ) {
          destinations.forEach { dest ->
            val sel = currentRoute == dest.route
            NavigationBarItem(
              selected = sel,
              onClick = {
                viewModel.setRoute(dest.route)
                if (dest.route == "project" && selectedFile == null) scope.launch { drawerState.open() }
              },
              icon = { Icon(dest.icon, dest.label, modifier = Modifier.size(22.dp)) },
              label = { Text(dest.label, fontSize = 10.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium) },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AcsGold, selectedTextColor = AcsGold,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = AcsGold.copy(alpha = 0.1f)
              )
            )
          }
        }
      }
    ) { padding ->
      Box(modifier = Modifier.fillMaxSize().padding(padding)) {
        AnimatedContent(
          targetState = currentRoute,
          transitionSpec = { fadeIn() togetherWith fadeOut() },
          label = "route"
        ) { route ->
          when (route) {
            "chat" -> ChatScreen(viewModel = viewModel)
            "git" -> GitScreen(viewModel = viewModel)
            "terminal" -> TerminalScreen(viewModel = viewModel)
            "project" -> {
              if (selectedFile != null) {
                Column(modifier = Modifier.fillMaxSize()) {
                  Box(modifier = Modifier.weight(1f)) {
                    EditorScreen(viewModel = viewModel, onOpenSidebar = { scope.launch { drawerState.open() } })
                  }
                  if (buildErrors.isNotEmpty() || isBuilding) {
                    BuildOutputPanel(
                      logs = buildLogs.toList(),
                      isBuilding = isBuilding,
                      onRetry = { viewModel.runBuildPipeline() },
                      onClear = { buildLogs.clear() }
                    )
                  }
                }
              } else {
                // No file selected - show project splash
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Android Code Studio", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text("Open the left drawer for files.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceDim)
                    Text("Swipe up for build output.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceDim)
                  }
                }
              }
            }
            "mockup" -> Column(modifier = Modifier.fillMaxSize()) {
              Row(modifier = Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.setRoute("project") }) {
                  Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", modifier = Modifier.size(18.dp))
                }
                Text("Preview", fontSize = 13.sp, fontWeight = FontWeight.Medium)
              }
              Box(modifier = Modifier.weight(1f)) { MockupScreen(viewModel = viewModel) }
            }
            else -> ChatScreen(viewModel = viewModel)
          }
        }
      }
    }
  }
}