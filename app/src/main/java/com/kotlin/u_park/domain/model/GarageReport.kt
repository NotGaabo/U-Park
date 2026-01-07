package com.kotlin.u_park.domain.model

import java.time.LocalDateTime

/**
 * Tipos de reportes disponibles
 */
enum class ReportType {
    OCCUPANCY,  // Reporte de ocupación
    INCOME      // Reporte de ingresos
}

/**
 * Períodos predefinidos para reportes
 */
enum class ReportPeriod(val months: Int, val displayName: String) {
    TWO_MONTHS(2, "Últimos 2 meses"),
    THREE_MONTHS(3, "Últimos 3 meses"),
    SIX_MONTHS(6, "Últimos 6 meses"),
    CUSTOM(0, "Rango personalizado")
}

/**
 * Reporte de ocupación del garage
 */
data class OccupancyReport(
    val garageId: String,
    val garageName: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val totalVehicles: Int,
    val dailyData: List<DailyOccupancy>,
    val employeeData: List<EmployeeOccupancy>,
    val averageStayMinutes: Double?
)

data class DailyOccupancy(
    val date: String,
    val vehicleCount: Int
)

data class EmployeeOccupancy(
    val employeeId: String,
    val employeeName: String,
    val vehicleCount: Int
)

/**
 * Reporte de ingresos del garage
 */
data class IncomeReport(
    val garageId: String,
    val garageName: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val totalIncome: Double,
    val dailyIncome: List<DailyIncome>,
    val parkingIncome: List<ParkingIncome>
)

data class DailyIncome(
    val date: String,
    val income: Double,
    val transactionCount: Int
)

data class ParkingIncome(
    val parkingId: String,
    val parkingName: String,
    val income: Double,
    val transactionCount: Int
)

/**
 * Estado de generación de reporte
 */
sealed class ReportState {
    object Idle : ReportState()
    object Loading : ReportState()
    data class Success(val filePath: String) : ReportState()
    data class Error(val message: String) : ReportState()
}