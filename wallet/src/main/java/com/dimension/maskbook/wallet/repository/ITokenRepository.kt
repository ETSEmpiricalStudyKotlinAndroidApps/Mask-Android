package com.dimension.maskbook.wallet.repository

import com.dimension.maskbook.wallet.db.AppDatabase
import com.dimension.maskbook.wallet.db.model.DbToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal


class TokenData(
    val address: String,
    val chainId: String,
    val name: String,
    val symbol: String,
    val decimals: Long,
    val logoURI: String?,
    val price: BigDecimal,
) {
    companion object {
        fun fromDb(data: DbToken) = with(data) {
            TokenData(
                address,
                chainId,
                name,
                symbol,
                decimals,
                logoURI,
                price,
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TokenData

        if (address != other.address) return false
        if (chainId != other.chainId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + chainId.hashCode()
        return result
    }
}

interface ITokenRepository {
    fun getTokenByAddress(id: String): Flow<TokenData>
}

class TokenRepository(
    private val database: AppDatabase,
) : ITokenRepository {
    override fun getTokenByAddress(id: String): Flow<TokenData> {
        return database.tokenDao().getByIdFlow(id).map { TokenData.fromDb(it) }
    }
}