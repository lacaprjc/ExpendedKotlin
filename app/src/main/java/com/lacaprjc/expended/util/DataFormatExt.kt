package com.lacaprjc.expended.util

private const val MIME_JSON = "text/json"
private const val MIME_CSV = "text/csv"
private const val MIME_SQL = "application/sql"
private const val MIME_SEMBAST = "*/*"

private const val EXTENSION_JSON = ".json"
private const val EXTENSION_CSV = ".csv"
private const val EXTENSION_SQL = ".db"
private const val EXTENSION_ELSE = ""

val DataFormat.mimeType: String
    get() = when (this) {
        DataFormat.JSON -> MIME_JSON
        DataFormat.CSV -> MIME_CSV
        DataFormat.SQL -> MIME_SQL
        DataFormat.SEMBAST -> MIME_SEMBAST
    }

val DataFormat.fileExtension: String
    get() = when (this) {
        DataFormat.JSON -> EXTENSION_JSON
        DataFormat.CSV -> EXTENSION_CSV
        DataFormat.SQL -> EXTENSION_SQL
        else -> EXTENSION_ELSE
    }
