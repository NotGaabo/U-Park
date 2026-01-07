package com.kotlin.u_park.presentation.screens.garage

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.u_park.domain.model.*
import com.kotlin.u_park.domain.repository.GarageReportRepository
import com.kotlin.u_park.presentation.utils.PdfGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class GarageDashboardViewModel(
    private val repository: GarageReportRepository
) : ViewModel() {

    private val _reportState = MutableStateFlow<ReportState>(ReportState.Idle)
    val reportState: StateFlow<ReportState> = _reportState

    private val _occupancyReport = MutableStateFlow<OccupancyReport?>(null)
    val occupancyReport: StateFlow<OccupancyReport?> = _occupancyReport

    private val _incomeReport = MutableStateFlow<IncomeReport?>(null)
    val incomeReport: StateFlow<IncomeReport?> = _incomeReport

    fun resetReportState() {
        _reportState.value = ReportState.Idle
    }

    // -------------------------
    // OCCUPANCY
    // -------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    fun generateOccupancyReport(
        context: Context,
        garageId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ) {
        viewModelScope.launch {
            _reportState.value = ReportState.Loading

            repository.getGarageOccupancyReport(garageId, startDate, endDate)
                .onSuccess { report ->
                    _occupancyReport.value = report

                    val file = PdfGenerator.generateOccupancyReport(context, report)
                    _reportState.value = ReportState.Success(file.absolutePath)
                }
                .onFailure {
                    _reportState.value = ReportState.Error(it.message ?: "Error")
                }
        }
    }

    // -------------------------
    // INCOME
    // -------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    fun generateIncomeReport(
        context: Context,
        garageId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ) {
        viewModelScope.launch {
            _reportState.value = ReportState.Loading

            repository.getParkingIdsByGarage(garageId)
                .onSuccess { parkingIds ->

                    repository.getGarageIncomeReport(
                        garageId,
                        parkingIds,
                        startDate,
                        endDate
                    ).onSuccess { report ->

                        _incomeReport.value = report

                        val file = PdfGenerator.generateIncomeReport(context, report)
                        _reportState.value = ReportState.Success(file.absolutePath)

                    }.onFailure {
                        _reportState.value = ReportState.Error(it.message ?: "Error")
                    }

                }
                .onFailure {
                    _reportState.value = ReportState.Error(it.message ?: "Error")
                }
        }
    }
}
