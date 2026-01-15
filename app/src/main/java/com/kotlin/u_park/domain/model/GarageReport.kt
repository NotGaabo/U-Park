package com.kotlin.u_park.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.time.LocalDateTime

// ---------------------------
// SERIALIZER PARA LocalDateTime
// ---------------------------
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.format.DateTimeFormatter

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_DATE_TIME
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.format(formatter))
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString(), formatter)
    }
}

// ---------------------------
// REPORT TYPES
// ---------------------------
@Serializable
enum class ReportType {
    OCCUPANCY,
    INCOME
}

@Serializable
enum class ReportPeriod(val months: Int, val displayName: String) {
    TWO_MONTHS(2, "Últimos 2 meses"),
    THREE_MONTHS(3, "Últimos 3 meses"),
    SIX_MONTHS(6, "Últimos 6 meses"),
    CUSTOM(0, "Rango personalizado")
}

// ---------------------------
// OCCUPANCY REPORT
// ---------------------------
@Serializable
data class OccupancyReport(
    val garageId: String,
    val garageName: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val startDate: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val endDate: LocalDateTime,
    val totalVehicles: Int,
    val dailyData: List<DailyOccupancy>,
    val employeeData: List<EmployeeOccupancy>,
    val averageStayMinutes: Double? = null
)

@Serializable
data class DailyOccupancy(
    val date: String,
    val vehicleCount: Int
)

@Serializable
data class EmployeeOccupancy(
    val employeeId: String,
    val employeeName: String,
    val vehicleCount: Int
)

// ---------------------------
// INCOME REPORT
// ---------------------------
@Serializable
data class IncomeReport(
    val garageId: String,
    val garageName: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val startDate: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val endDate: LocalDateTime,
    val totalIncome: Double,
    val dailyIncome: List<DailyIncome>,
    val parkingIncome: List<ParkingIncome>
)

@Serializable
data class DailyIncome(
    val date: String,
    val income: Double,
    val transactionCount: Int
)

@Serializable
data class ParkingIncome(
    val parkingId: String,
    val parkingName: String,
    val income: Double,
    val transactionCount: Int
)

// ---------------------------
// REPORT STATE
// ---------------------------
@Serializable
sealed class ReportState {
    @Serializable
    object Idle : ReportState()

    @Serializable
    object Loading : ReportState()

    @Serializable
    data class Success(val filePath: String) : ReportState()

    @Serializable
    data class Error(val message: String) : ReportState()
}
