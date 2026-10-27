package com.ordra.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ordra.data.local.entity.ReceiptEntity
import com.ordra.data.local.entity.RefundEntity
import com.ordra.data.local.entity.ReturnEntity
import com.ordra.data.local.entity.WarrantyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostPurchaseDao {
    @Query("SELECT * FROM returns WHERE orderId = :orderId")
    fun observeReturns(orderId: String): Flow<List<ReturnEntity>>

    @Query("SELECT * FROM refunds WHERE orderId = :orderId")
    fun observeRefunds(orderId: String): Flow<List<RefundEntity>>

    @Query("SELECT * FROM warranties WHERE orderId = :orderId")
    fun observeWarranties(orderId: String): Flow<List<WarrantyEntity>>

    @Query("SELECT * FROM receipts WHERE orderId = :orderId")
    fun observeReceipts(orderId: String): Flow<List<ReceiptEntity>>

    @Upsert suspend fun upsertReturn(e: ReturnEntity)
    @Upsert suspend fun upsertRefund(e: RefundEntity)
    @Upsert suspend fun upsertWarranty(e: WarrantyEntity)
    @Upsert suspend fun upsertReceipt(e: ReceiptEntity)

    @Query("DELETE FROM receipts WHERE id = :id")
    suspend fun deleteReceipt(id: String)
}
