package ru.rtuitlab.copycheck.copyright

data class CopyrightResult(
    val resultStatus: Int,
    val data: List<RaoSearchResult>
)