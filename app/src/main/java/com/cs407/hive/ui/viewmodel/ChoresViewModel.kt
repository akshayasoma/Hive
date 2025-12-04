package com.cs407.hive.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.hive.data.model.AddChoreRequest
import com.cs407.hive.data.repository.ChoreRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChoresViewModel(
    private val repository: ChoreRepository,
    private val groupId: String,
    private val deviceId: String
) : ViewModel() {

    val chores: StateFlow<List<com.cs407.hive.data.local.chore.ChoreEntity>> =
        repository.observeChores()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            repository.syncChores(groupId)
        }
    }

    fun addChore(name: String, description: String, points: Int) {
        viewModelScope.launch {
            repository.addChore(
                AddChoreRequest(
                    groupId = groupId,
                    deviceId = deviceId,
                    name = name,
                    description = description,
                    points = points
                )
            )
            refresh()
        }
    }
}

