package com.nightshadow.mini.agent

import android.content.Context
import com.nightshadow.mini.ai.GeminiProvider
import com.nightshadow.mini.diagnostics.MiniLogger
import com.nightshadow.mini.vision.ScreenCaptureManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json

class AgentEngine(private val context: Context) {
    private val _stateFlow = MutableStateFlow(AgentState.IDLE)
    val stateFlow: StateFlow<AgentState> = _stateFlow

    private var currentJob: Job? = null
    private val aiProvider = GeminiProvider()
    private val validator = ActionValidator(context)
    private val executor = ActionExecutor(context)
    
    private val json = Json { ignoreUnknownKeys = true }

    private val MAX_STEPS = 15
    private val MAX_RETRIES = 3

    fun startTask(prompt: String) {
        if (_stateFlow.value != AgentState.IDLE && _stateFlow.value != AgentState.COMPLETED && _stateFlow.value != AgentState.FAILED && _stateFlow.value != AgentState.CANCELLED) {
            MiniLogger.w("AgentEngine", "Agent is already running.")
            return
        }

        currentJob = CoroutineScope(Dispatchers.Default).launch {
            runAgentLoop(prompt)
        }
    }

    fun cancelTask() {
        currentJob?.cancel()
        _stateFlow.value = AgentState.CANCELLED
        MiniLogger.i("AgentEngine", "Task cancelled by user")
    }

    private suspend fun runAgentLoop(prompt: String) {
        var stepCount = 0
        var retryCount = 0
        _stateFlow.value = AgentState.PLANNING

        try {
            while (isActive && stepCount < MAX_STEPS) {
                _stateFlow.value = AgentState.WAITING_FOR_SCREEN
                val screenshot = ScreenCaptureManager.captureScreen()
                
                if (screenshot == null) {
                    MiniLogger.e("AgentEngine", "Failed to capture screen")
                    _stateFlow.value = AgentState.FAILED
                    return
                }

                _stateFlow.value = AgentState.PLANNING
                val jsonResponse = try {
                    aiProvider.getNextAction(prompt, screenshot)
                } catch (e: Exception) {
                    if (++retryCount > MAX_RETRIES) throw e
                    delay(2000)
                    continue
                }
                
                screenshot.recycle() // Prevent memory leaks

                _stateFlow.value = AgentState.VALIDATING
                val action = try {
                    json.decodeFromString<Action>(jsonResponse)
                } catch (e: Exception) {
                    MiniLogger.e("AgentEngine", "Failed to parse AI response: $jsonResponse")
                    if (++retryCount > MAX_RETRIES) throw e
                    continue
                }

                if (action.action.lowercase() == "done") {
                    _stateFlow.value = AgentState.COMPLETED
                    return
                }
                
                if (action.action.lowercase() == "stop") {
                    MiniLogger.w("AgentEngine", "AI stopped task: ${action.reason}")
                    _stateFlow.value = AgentState.FAILED
                    return
                }

                if (!validator.isValid(action)) {
                    MiniLogger.e("AgentEngine", "Invalid action generated: $action")
                    if (++retryCount > MAX_RETRIES) {
                        _stateFlow.value = AgentState.FAILED
                        return
                    }
                    continue
                }

                _stateFlow.value = AgentState.EXECUTING
                val success = executor.execute(action)
                if (!success) {
                    MiniLogger.w("AgentEngine", "Action execution failed")
                }

                stepCount++
                retryCount = 0 // Reset retries on successful step
                
                _stateFlow.value = AgentState.VERIFYING
                delay(1500) // Wait for UI to settle before next screenshot
            }
            
            if (stepCount >= MAX_STEPS) {
                MiniLogger.w("AgentEngine", "Max steps reached")
                _stateFlow.value = AgentState.FAILED
            }

        } catch (e: CancellationException) {
            _stateFlow.value = AgentState.CANCELLED
            throw e
        } catch (e: Exception) {
            MiniLogger.e("AgentEngine", "Fatal error in agent loop", e)
            _stateFlow.value = AgentState.FAILED
        }
    }
}
