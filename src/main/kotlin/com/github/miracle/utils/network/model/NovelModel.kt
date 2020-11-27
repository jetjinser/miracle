package com.github.miracle.utils.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NovelModel(
    @SerialName("status")
    val status: Int,
    @SerialName("chapter_id")
    val chapterId: Int,
    @SerialName("chapter_title")
    val chapterTitle: String,
    @SerialName("chapter_desc")
    val chapterDesc: String,
    @SerialName("title")
    val title: String,
)