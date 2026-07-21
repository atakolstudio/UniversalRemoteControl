package com.atakolstudio.universalremote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.atakolstudio.universalremote.data.local.entity.DeviceCategory
import com.atakolstudio.universalremote.data.local.entity.IrCodeEntity
import com.atakolstudio.universalremote.data.local.entity.RemoteFunction
import kotlinx.coroutines.flow.Flow

@Dao
interface IrCodeDao {

    @Query(
        """SELECT * FROM ir_codes WHERE brand = :brand AND category = :category
           AND deviceId IS NULL ORDER BY function"""
    )
    suspend fun getPresetCodes(brand: String, category: DeviceCategory): List<IrCodeEntity>

    @Query(
        """SELECT * FROM ir_codes WHERE brand = :brand AND category = :category
           AND function = :function AND deviceId IS NULL LIMIT 1"""
    )
    suspend fun getPresetCode(
        brand: String,
        category: DeviceCategory,
        function: RemoteFunction
    ): IrCodeEntity?

    @Query("SELECT * FROM ir_codes WHERE deviceId = :deviceId")
    fun observeLearnedCodesForDevice(deviceId: Long): Flow<List<IrCodeEntity>>

    @Query(
        "SELECT * FROM ir_codes WHERE deviceId = :deviceId AND function = :function LIMIT 1"
    )
    suspend fun getLearnedCode(deviceId: Long, function: RemoteFunction): IrCodeEntity?

    @Query("SELECT DISTINCT brand FROM ir_codes WHERE category = :category ORDER BY brand")
    suspend fun getBrandsForCategory(category: DeviceCategory): List<String>

    @Query(
        """DELETE FROM ir_codes WHERE brand = :brand AND category = :category
           AND function = :function AND deviceId IS NULL"""
    )
    suspend fun deletePreset(brand: String, category: DeviceCategory, function: RemoteFunction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(codes: List<IrCodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(code: IrCodeEntity): Long

    @Query("SELECT COUNT(*) FROM ir_codes")
    suspend fun count(): Int
}
