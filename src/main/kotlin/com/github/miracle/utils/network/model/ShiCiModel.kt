package com.github.miracle.utils.network.model

import kotlinx.serialization.Serializable

@Serializable
data class ShiCiModel (
    val content: String,
    val origin : String,
    val author : String,
    val category: String
)