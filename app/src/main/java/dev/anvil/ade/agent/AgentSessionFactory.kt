package dev.anvil.ade.agent

import android.content.Context
import dev.anvil.ade.model.AgentStep
import dev.anvil.ade.model.StepKind
import dev.anvil.ade.model.ProjectType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

/**
 * Creates and wires AgentSession instances. Extracted from AnvilViewModel so
 * session construction (config load, project type sync, collector wiring)
 * lives with the agent layer. The owner supplies callbacks to receive state.
 */
object AgentSessionFactory {

    data class Hooks(
        val onSteps: (List<AgentStep>) -> Unit,
        val onBusy: (Boolean) -> Unit,
        val onPendingMemoryEntry: (String?) -> Unit,
        val onPendingSkillProposal: (SelfImprovement.SkillProposal?) -> Unit,
        val onPendingTerminalCommand: (String?) -> Unit,
        val onPendingSoulProposal: (AgentSession.SoulProposal?) -> Unit
    )

    fun create(
        context: Context,
        scope: CoroutineScope,
        workspaceRoot: File,
        projectType: ProjectType,
        hooks: Hooks
    ): AgentSession {
        val config = ProviderConfigStore.load(context)
        val created = AgentSession(
            config = config,
            workspaceRoot = workspaceRoot,
            appContext = context.applicationContext
        )
        created.projectType = projectType
        scope.launch {
            created.steps.collect { engineSteps ->
                hooks.onSteps(engineSteps.map { s ->
                    AgentStep(
                        id = UUID.randomUUID().toString(),
                        kind = when (s.kind) {
                            AgentSession.Step.Kind.USER -> StepKind.USER
                            AgentSession.Step.Kind.AGENT_TEXT -> StepKind.AGENT_TEXT
                            AgentSession.Step.Kind.TOOL_CALL -> StepKind.TOOL_CALL
                            AgentSession.Step.Kind.TOOL_RESULT -> StepKind.TOOL_RESULT
                            AgentSession.Step.Kind.ERROR -> StepKind.ERROR
                            AgentSession.Step.Kind.INFO -> StepKind.INFO
                        },
                        text = s.text,
                        timestamp = ""
                    )
                })
            }
        }
        scope.launch { created.busy.collect { hooks.onBusy(it) } }
        scope.launch { created.pendingMemoryEntry.collect { hooks.onPendingMemoryEntry(it) } }
        scope.launch { created.pendingSkillProposal.collect { hooks.onPendingSkillProposal(it) } }
        scope.launch { created.pendingTerminalCommand.collect { hooks.onPendingTerminalCommand(it) } }
        scope.launch { created.pendingSoulProposal.collect { hooks.onPendingSoulProposal(it) } }
        return created
    }
}
