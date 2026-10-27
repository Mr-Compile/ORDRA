package com.ordra.domain.repository

import com.ordra.core.common.AppResult

interface BackupRepository {
    /** Full JSON backup payload (all tables, relationships preserved). */
    suspend fun exportBackup(): ByteArray

    /** Validates + imports a backup. Returns number of orders restored. */
    suspend fun importBackup(bytes: ByteArray): AppResult<Int>

    /** User-readable CSV of order history. */
    suspend fun exportCsv(): String
}
