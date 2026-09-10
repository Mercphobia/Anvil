package dev.anvil.ade.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anvil.ade.model.ProjectType
import dev.anvil.ade.ui.components.ChatSidebar
import dev.anvil.ade.ui.components.GitSidebar
import dev.anvil.ade.ui.components.ProjectSidebar
import dev.anvil.ade.ui.components.ProviderSettingsDialog
import dev.anvil.ade.ui.components.TerminalSidebar
import dev.anvil.ade.ui.screens.*
import dev.anvil.ade.viewmodel.AnvilViewModel
import kotlinx.coroutines.launch

data class NavDestination(
  val route: String,
  val label: String,
  val icon: ImageVector,
  val hasBadge: Boolean = false
)

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

  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  // Keep the diff fresh so sidebar "N files changed" is honest.
  androidx.compose.runtime.LaunchedEffect(currentRoute) {
    viewModel.refreshGitDiff()
  }

  // 3-tab nav (user decision, FINAL): Chat / Git / Terminal.
  // Project files live in the Project Sidebar (drawer); build output is
  // merged into the Terminal panel; mockup preview is a contextual view
  // from the editor's Preview button.
  val destinations = listOf(
    NavDestination("chat", "Chat", Icons.Filled.ChatBubble, hasBadge = isBusy),
    NavDestination("git", "Git", Icons.AutoMirrored.Filled.CallSplit),
    NavDestination("terminal", "Terminal", Icons.Filled.Terminal, hasBadge = isBuilding)
  )

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
    dev.anvil.ade.ui.components.OpenProjectDialog(
      onDismiss = { viewModel.toggleOpenProjectDialog(false) },
      onOpenLocal = { name, path -> viewModel.openLocalProject(name, path) },
      onCloneGitHub = { url, branch, token -> viewModel.cloneGitHubProject(url, branch, token) }
    )
  }

  val showTemplateDialog by viewModel.showTemplateDialog.collectAsState()
  if (showTemplateDialog) {
    dev.anvil.ade.ui.components.ProjectTemplateDialog(
      onDismiss = { viewModel.toggleTemplateDialog(false) },
      onSelectTemplate = { templateId -> viewModel.applyProjectTemplate(templateId) }
    )
  }

  if (showAgentConfig) {
    dev.anvil.ade.ui.screens.AgentConfigScreen(
      viewModel = viewModel,
      onClose = { viewModel.toggleAgentConfig(false) }
    )
    return
  }

  // Responsive design: Check constraints for Compact vs Expanded layout
  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val isWideScreen = maxWidth >= 600.dp

    // Sidebar width: 78% of screen (anvil_ui component spec).
    val drawerWidth = (maxWidth.value * 0.78f).dp

    ModalNavigationDrawer(
      drawerState = drawerState,
      gesturesEnabled = !showWelcome,
      drawerContent = {
        ModalDrawerSheet(
          modifier = Modifier.width(drawerWidth),
          drawerContainerColor = MaterialTheme.colorScheme.surface
        ) {
          val closeDrawer: () -> Unit = { scope.launch { drawerState.close() } }
          when (currentRoute) {
            "chat" -> ChatSidebar(viewModel = viewModel, onClose = closeDrawer)
            "git" -> GitSidebar(viewModel = viewModel, onClose = closeDrawer)
            "terminal" -> TerminalSidebar(viewModel = viewModel, onClose = closeDrawer)
            else -> ProjectSidebar(viewModel = viewModel, onClose = closeDrawer)
          }
        }
      }
    ) {
      Scaffold(
        topBar = {
          if (!showWelcome) {
            TopAppBar(
              navigationIcon = {
                IconButton(
                  onClick = { scope.launch { drawerState.open() } },
                  modifier = Modifier.testTag("open_working_tree_button")
                ) {
                  Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Buka Working Tree Sidebar",
                    tint = MaterialTheme.colorScheme.onSurface
                  )
                }
              },
              title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "Anvil",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                  )
                }
              },
              actions = {
                // Mode indicator (read-only - auto-detected from how the
                // project was opened, not manually switchable)
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = MaterialTheme.colorScheme.surfaceContainerHigh,
                  modifier = Modifier.padding(end = 4.dp)
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Box(
                      modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                          if (activeType == ProjectType.ANDROID) MaterialTheme.colorScheme.primary
                          else MaterialTheme.colorScheme.secondary
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = if (activeType == ProjectType.ANDROID) "App Builder" else "SystemUI",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Medium,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }
                }

                // Setup Wizard trigger icon
                IconButton(onClick = { viewModel.toggleSetupWizard(true) }) {
                  Icon(
                    imageVector = Icons.Filled.RocketLaunch,
                    contentDescription = "Buka Setup Wizard",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                  )
                }

                // LLM Settings icon
                IconButton(onClick = { viewModel.toggleSettingsDialog(true) }) {
                  Icon(
                    imageVector = Icons.Filled.Tune,
                    contentDescription = "Konfigurasi LLM",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                  )
                }

                // Agent Config (Soul/Memory/Skills) icon
                IconButton(onClick = { viewModel.toggleAgentConfig(true) }) {
                  Icon(
                    imageVector = Icons.Filled.Psychology,
                    contentDescription = "Agent Config (Soul, Memory, Skills)",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                  )
                }

                // Welcome / Help Info icon
                IconButton(onClick = { viewModel.openWelcome() }) {
                  Icon(
                    imageVector = Icons.Filled.HelpOutline,
                    contentDescription = "Buka Welcome Page",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                  )
                }
              },
              colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
              )
            )
          }
        },
        bottomBar = {
          // Bottom Navigation Bar for Compact screens
          if (!isWideScreen && !showWelcome) {
            NavigationBar(
              containerColor = MaterialTheme.colorScheme.surfaceContainer,
              modifier = Modifier.testTag("bottom_nav_bar")
            ) {
              destinations.forEach { dest ->
                val isSelected = currentRoute == dest.route
                NavigationBarItem(
                  selected = isSelected,
                  onClick = { viewModel.setRoute(dest.route) },
                  icon = {
                    if (dest.hasBadge) {
                      BadgedBox(
                        badge = {
                          Badge(
                            containerColor = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(6.dp)
                          )
                        }
                      ) {
                        Icon(dest.icon, contentDescription = dest.label)
                      }
                    } else {
                      Icon(dest.icon, contentDescription = dest.label)
                    }
                  },
                  label = {
                    Text(
                      text = dest.label,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      fontSize = 10.sp
                    )
                  },
                  colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary
                  )
                )
              }
            }
          }
        }
      ) { paddingValues ->
        if (showWelcome) {
          // Fullscreen Welcome Page
          HomeScreen(
            viewModel = viewModel,
            modifier = Modifier
              .fillMaxSize()
              .padding(paddingValues)
          )
        } else {
          Row(
            modifier = Modifier
              .fillMaxSize()
              .padding(paddingValues)
          ) {
            // Adaptive Navigation Rail for Wide Screens
            if (isWideScreen) {
              NavigationRail(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.testTag("nav_rail")
              ) {
                Spacer(modifier = Modifier.height(8.dp))
                destinations.forEach { dest ->
                  val isSelected = currentRoute == dest.route
                  NavigationRailItem(
                    selected = isSelected,
                    onClick = { viewModel.setRoute(dest.route) },
                    icon = {
                      if (dest.hasBadge) {
                        BadgedBox(badge = { Badge(modifier = Modifier.size(6.dp)) }) {
                          Icon(dest.icon, contentDescription = dest.label)
                        }
                      } else {
                        Icon(dest.icon, contentDescription = dest.label)
                      }
                    },
                    label = { Text(dest.label, fontSize = 11.sp) },
                    colors = NavigationRailItemDefaults.colors(
                      indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                      selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                  )
                }
              }
            }

            // Main Screen Router with animated transitions
            Box(
              modifier = Modifier
                .fillMaxSize()
                .weight(1f)
            ) {
              AnimatedContent(
                targetState = currentRoute,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "RouteTransition"
              ) { route ->
                when (route) {
                  "chat" -> ChatScreen(viewModel = viewModel)
                  "git" -> GitScreen(viewModel = viewModel)
                  "terminal" -> TerminalScreen(viewModel = viewModel)
                  "editor" -> EditorScreen(
                    viewModel = viewModel,
                    onOpenSidebar = { scope.launch { drawerState.open() } }
                  )
                  "mockup" -> Column(modifier = Modifier.fillMaxSize()) {
                    // Mockup preview is a contextual view opened from the
                    // editor's Preview button — back returns to the editor.
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .padding(horizontal = 8.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      IconButton(onClick = { viewModel.setRoute("editor") }) {
                        Icon(
                          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                          contentDescription = "Kembali ke editor",
                          tint = MaterialTheme.colorScheme.onSurfaceVariant,
                          modifier = Modifier.size(18.dp)
                        )
                      }
                      Text(
                        text = "Preview",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                      )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                      MockupScreen(viewModel = viewModel)
                    }
                  }
                  "home" -> HomeScreen(
                    onCreateProject = { viewModel.startNewProject() },
                    onOpenProject = { viewModel.toggleOpenProjectDialog(true) },
                    onCloneGit = { viewModel.setRoute("clone") },
                    onOpenTerminal = { viewModel.setRoute("terminal") },
                    onOpenPreferences = { viewModel.setRoute("preferences") },
                    onOpenIdeConfig = { viewModel.setRoute("ideconfig") },
                    onOpenDocs = { }
                  )
                  "preferences" -> PreferencesScreen(onBack = { viewModel.setRoute("home") })
                  "projectconfig" -> ProjectConfigScreen(
                    onBack = { viewModel.setRoute("home") },
                    onCreateProject = { n, p, l, lang, sdk, kts -> viewModel.createProject(n, p, l, lang, sdk, kts) }
                  )
                  "templates" -> TemplateSelectionScreen(
                    onBack = { viewModel.setRoute("projectconfig") },
                    onSelectTemplate = { viewModel.applyProjectTemplate(it) }
                  )
                  "sdkinstall" -> SdkInstallationScreen(
                    onBack = { viewModel.setRoute("home") },
                    onDone = { s, j, n, g, ssh -> viewModel.installSdk(s, j, n, g, ssh) }
                  )
                  "ideconfig" -> IdeConfigScreen(onBack = { viewModel.setRoute("home") })
                  "settings" -> SettingsScreen(onBack = { viewModel.setRoute("home") })
                  "clone" -> CloneGitSheet(
                    onDismiss = { viewModel.setRoute("home") },
                    onClone = { url, branch, token -> viewModel.cloneGitHubProject(url, branch, token) }
                  )
                  else -> ChatScreen(viewModel = viewModel)
                }
              }
            }
          }
        }
      }
    }
  }
}
