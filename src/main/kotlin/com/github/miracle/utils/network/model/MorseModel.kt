package com.github.miracle.utils.network.model

import kotlinx.serialization.Serializable

@Serializable
data class MorseModel(
    val status: Boolean,
    val message: String,
    val text: String
)
