package com.kotlin.u_park.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.kotlin.u_park.domain.model.*
import com.kotlin.u_park.domain.repository.GarageReportRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GarageReportRepositoryImpl(
    private val supabase: SupabaseClient
) : GarageReportRepository {

    @RequiresApi(Build.VERSION_CODES.O)
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    // -----------------------------
    // OCCUPANCY REPORT
    // -----------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getGarageOccupancyReport(
        garageId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Result<OccupancyReport> {
        return try {
            val response = supabase.postgrest.rpc(
                "rpc_get_garage_occupancy_report",
                mapOf(
                    "params" to mapOf(
                        "garage_id" to garageId,
                        "start_date" to startDate.format(formatter),
                        "end_date" to endDate.format(formatter)
                    )
                )
            )

            val json = Json.parseToJsonElement(response.data).jsonObject

            Result.success(
                OccupancyReport(
                    garageId = garageId,
                    garageName = json["garage_name"]!!.jsonPrimitive.content,
                    startDate = startDate,
                    endDate = endDate,
                    totalVehicles = json["total_vehicles"]!!.jsonPrimitive.int,
                    dailyData = parseDailyOccupancy(json["daily_data"]),
                    employeeData = parseEmployeeOccupancy(json["employee_data"]),
                    averageStayMinutes = json["average_stay_minutes"]?.jsonPrimitive?.doubleOrNull
                )
            )

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }


    // -----------------------------
    // INCOME REPORT
    // -----------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getGarageIncomeReport(
        garageId: String,
        parkingIds: List<String>,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Result<IncomeReport> {
        return try {
            val response = supabase.postgrest.rpc(
                "rpc_get_garage_income_report",
                mapOf(
                    "params" to mapOf(
                        "garage_id" to garageId,
                        "parking_ids" to parkingIds,
                        "start_date" to startDate.format(formatter),
                        "end_date" to endDate.format(formatter)
                    )
                )
            )

            val json = Json.parseToJsonElement(response.data).jsonObject

            Result.success(
                IncomeReport(
                    garageId = garageId,
                    garageName = json["garage_name"]!!.jsonPrimitive.content,
                    startDate = startDate,
                    endDate = endDate,
                    totalIncome = json["total_income"]!!.jsonPrimitive.double,
                    dailyIncome = parseDailyIncome(json["daily_income"]),
                    parkingIncome = parseParkingIncome(json["parking_income"])
                )
            )

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // -----------------------------
    // PARKING IDS
    // -----------------------------
    override suspend fun getParkingIdsByGarage(garageId: String): Result<List<String>> {
        return try {
            val response = supabase.from("parkings")
                .select {
                    filter {
                        eq("garage_id", garageId)
                        eq("estado", "completada")
                    }
                }

            val json = Json.parseToJsonElement(response.data).jsonArray

            Result.success(json.mapNotNull { it.jsonObject["id"]?.jsonPrimitive?.content })

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // -----------------------------
    // PARSERS
    // -----------------------------
    private fun parseDailyOccupancy(el: JsonElement?) =
        el?.jsonArray?.map {
            DailyOccupancy(
                it.jsonObject["date"]!!.jsonPrimitive.content,
                it.jsonObject["vehicle_count"]!!.jsonPrimitive.int
            )
        } ?: emptyList()

    private fun parseEmployeeOccupancy(el: JsonElement?) =
        el?.jsonArray?.map {
            EmployeeOccupancy(
                it.jsonObject["employee_id"]!!.jsonPrimitive.content,
                it.jsonObject["employee_name"]!!.jsonPrimitive.content,
                it.jsonObject["vehicle_count"]!!.jsonPrimitive.int
            )
        } ?: emptyList()

    private fun parseDailyIncome(el: JsonElement?) =
        el?.jsonArray?.map {
            DailyIncome(
                it.jsonObject["date"]!!.jsonPrimitive.content,
                it.jsonObject["income"]!!.jsonPrimitive.double,
                it.jsonObject["transaction_count"]!!.jsonPrimitive.int
            )
        } ?: emptyList()

    private fun parseParkingIncome(el: JsonElement?) =
        el?.jsonArray?.map {
            ParkingIncome(
                it.jsonObject["parking_id"]!!.jsonPrimitive.content,
                it.jsonObject["parking_name"]!!.jsonPrimitive.content,
                it.jsonObject["income"]!!.jsonPrimitive.double,
                it.jsonObject["transaction_count"]!!.jsonPrimitive.int
            )
        } ?: emptyList()
}
