package com.atakolstudio.universalremote.data.local

import androidx.room.TypeConverter
import com.atakolstudio.universalremote.data.local.entity.ConnectionType
import com.atakolstudio.universalremote.data.local.entity.DeviceCategory
import com.atakolstudio.universalremote.data.local.entity.IrProtocol
import com.atakolstudio.universalremote.data.local.entity.RemoteFunction
import com.atakolstudio.universalremote.data.local.entity.WifiProtocol

class Converters {
    @TypeConverter
    fun fromDeviceCategory(value: DeviceCategory): String = value.name
    @TypeConverter
    fun toDeviceCategory(value: String): DeviceCategory = DeviceCategory.valueOf(value)

    @TypeConverter
    fun fromConnectionType(value: ConnectionType): String = value.name
    @TypeConverter
    fun toConnectionType(value: String): ConnectionType = ConnectionType.valueOf(value)

    @TypeConverter
    fun fromWifiProtocol(value: WifiProtocol): String = value.name
    @TypeConverter
    fun toWifiProtocol(value: String): WifiProtocol = WifiProtocol.valueOf(value)

    @TypeConverter
    fun fromIrProtocol(value: IrProtocol): String = value.name
    @TypeConverter
    fun toIrProtocol(value: String): IrProtocol = IrProtocol.valueOf(value)

    @TypeConverter
    fun fromRemoteFunction(value: RemoteFunction): String = value.name
    @TypeConverter
    fun toRemoteFunction(value: String): RemoteFunction = RemoteFunction.valueOf(value)
}
