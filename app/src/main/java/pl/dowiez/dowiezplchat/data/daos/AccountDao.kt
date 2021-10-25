package pl.dowiez.dowiezplchat.data.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import pl.dowiez.dowiezplchat.data.entities.Account
import pl.dowiez.dowiezplchat.data.entities.ConversationWithAccounts

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(account: Account)

    @Query("SELECT * FROM Account")
    fun getAll(): Flow<List<Account>>

    @Query("SELECT * FROM Account WHERE accountId = :accountId")
    fun get(accountId: String): Account
}