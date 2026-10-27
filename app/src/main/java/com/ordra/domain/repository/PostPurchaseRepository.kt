package com.ordra.domain.repository

import com.ordra.domain.model.PurchaseReturn
import com.ordra.domain.model.Receipt
import com.ordra.domain.model.Refund
import com.ordra.domain.model.Warranty
import kotlinx.coroutines.flow.Flow

interface PostPurchaseRepository {
    fun observeReturns(orderId: String): Flow<List<PurchaseReturn>>
    fun observeRefunds(orderId: String): Flow<List<Refund>>
    fun observeWarranties(orderId: String): Flow<List<Warranty>>
    fun observeReceipts(orderId: String): Flow<List<Receipt>>
    suspend fun saveReturn(item: PurchaseReturn)
    suspend fun saveRefund(item: Refund)
    suspend fun saveWarranty(item: Warranty)
    suspend fun saveReceipt(item: Receipt)
    suspend fun deleteReceipt(id: String)
}
