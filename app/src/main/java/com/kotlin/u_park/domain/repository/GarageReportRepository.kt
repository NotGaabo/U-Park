package com.kotlin.u_park.domain.repository

import com.kotlin.u_park.domain.model.IncomeReport
import com.kotlin.u_park.domain.model.OccupancyReport
import java.time.LocalDateTime

interface GarageReportRepository {

    suspend fun getGarageOccupancyReport(
        garageId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Result<OccupancyReport>

    suspend fun getGarageIncomeReport(
        garageId: String,
        parkingIds: List<String>,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Result<IncomeReport>

    suspend fun getParkingIdsByGarage(
        garageId: String
    ): Result<List<String>>
}
