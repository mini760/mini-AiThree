package com.nightshadow.mini.agent

import kotlinx.serialization.Serializable

@Serializable
data class Action(
    val action: String,
    val x: Float? = null,
    val y: Float? = null,
    val direction: String? = null,
    val reason: String? = null
)
