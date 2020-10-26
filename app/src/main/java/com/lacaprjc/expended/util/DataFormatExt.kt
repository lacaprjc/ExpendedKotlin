package com.lacaprjc.expended.util

private val MIME_JSON = listOf(
    "application/json",
    "text/json"
)


private val MIME_CSV = listOf(
    "application/vnd.csv",
    "application/csv",
    "text/comma-separated-values",
    "text/csv"
)

private val MIME_SEMBAST = listOf("*/*")

val DataFormat.mimeTypes: List<String>
    get() = when (this) {
        DataFormat.JSON -> MIME_JSON
        DataFormat.CSV -> MIME_CSV
        DataFormat.SEMBAST -> MIME_SEMBAST
    }

val DataFormat.fileExtension: String
    get() = when (this) {
        DataFormat.JSON -> ".json"
        DataFormat.CSV -> ".csv"
        else -> ""
    }
