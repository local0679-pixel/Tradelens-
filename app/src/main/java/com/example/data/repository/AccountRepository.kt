package com.example.data.repository

import com.example.data.local.AccountDao
import com.example.data.model.AccountEntity
import kotlinx.coroutines.flow.Flow

class AccountRepository(private val accountDao: AccountDao) {

    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()

    suspend fun insertAccount(account: AccountEntity) {
        accountDao.insertAccount(account)
    }

    suspend fun insertAccounts(accounts: List<AccountEntity>) {
        accountDao.insertAccounts(accounts)
    }

    suspend fun deleteAccount(account: AccountEntity) {
        accountDao.deleteAccount(account)
    }

    suspend fun deleteAccountById(accountId: String) {
        accountDao.deleteAccountById(accountId)
    }

    suspend fun clearAllAccounts() {
        accountDao.deleteAllAccounts()
    }
}
