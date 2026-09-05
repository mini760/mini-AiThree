package com.nightshadow.mini.agent

enum class AgentState {
    IDLE,
    PLANNING,
    WAITING_FOR_SCREEN,
    VALIDATING,
    EXECUTING,
    VERIFYING,
    RECOVERING,
    COMPLETED,
    FAILED,
    CANCELLED
}
