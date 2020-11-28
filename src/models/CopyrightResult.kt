package ru.rtuitlab.copycheck.models

data class CopyrightResult(
    val resultStatus: Int,
    val data: List<RaoSearchResult>
)