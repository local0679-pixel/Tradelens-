package com.example.data.repository

import com.example.data.local.TradeDao
import com.example.data.model.TradeEntity
import kotlinx.coroutines.flow.Flow

class TradeRepository(private val tradeDao: TradeDao) {

    val allTrades: Flow<List<TradeEntity>> = tradeDao.getAllTrades()

    fun getTradesForAccount(accountId: String): Flow<List<TradeEntity>> {
        return if (accountId == "ALL") {
            tradeDao.getAllTrades()
        } else {
            tradeDao.getTradesForAccount(accountId)
        }
    }

    suspend fun insertTrades(trades: List<TradeEntity>) {
        tradeDao.insertTrades(trades)
    }

    suspend fun insertTrade(trade: TradeEntity): Long {
        return tradeDao.insertTrade(trade)
    }

    suspend fun deleteTrade(trade: TradeEntity) {
        tradeDao.deleteTrade(trade)
    }

    suspend fun deleteTradesForAccount(accountId: String) {
        if (accountId == "ALL") {
            tradeDao.deleteAllTrades()
        } else {
            tradeDao.deleteTradesForAccount(accountId)
        }
    }

    suspend fun clearAllTrades() {
        tradeDao.deleteAllTrades()
    }
}
