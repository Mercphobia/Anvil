package com.vibe.forge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vibe.forge.ui.screens.BuildScreen
import com.vibe.forge.ui.screens.ChatScreen
import com.vibe.forge.ui.screens.GitScreen
import com.vibe.forge.ui.screens.MockupScreen
import com.vibe.forge.ui.screens.ProjectScreen
import com.vibe.forge.ui.theme.VibeForgeTheme

class MainActivity : ComponentActivity() {

    private data class NavItem(val route: String, val label: String, val icon: ImageVector)

    private val navItems = listOf(
        NavItem("chat", "Chat", Icons.Filled.Chat),
        NavItem("project", "Project", Icons.Filled.Folder),
        NavItem("mockup", "Mockup", Icons.Filled.Preview),
        NavItem("git", "Git", Icons.Filled.Share),
        NavItem("build", "Build", Icons.Filled.Build)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VibeForgeTheme {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route ?: "chat"

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            navItems.forEach { item ->
                                NavigationBarItem(
                                    selected = currentRoute == item.route,
                                    onClick = {
                                        navController.navigate(item.route) {
                                            popUpTo("chat") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    label = { Text(item.label) }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(modifier = Modifier.padding(innerPadding)) {
                        NavHost(
                            navController = navController,
                            startDestination = "chat"
                        ) {
                            composable("chat") { ChatScreen() }
                            composable("project") { ProjectScreen() }
                            composable("mockup") { MockupScreen() }
                            composable("git") { GitScreen() }
                            composable("build") { BuildScreen() }
                        }
                    }
                }
            }
        }
    }
}
