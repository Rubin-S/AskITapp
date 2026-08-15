package com.askit.app.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SessionProfile(
    val displayName: String = "You",
    val hasListedService: Boolean = false,
)

class SessionProfileStore(
    initial: SessionProfile = SessionProfile(),
) {
    private val _profile = MutableStateFlow(initial)
    val profile: StateFlow<SessionProfile> = _profile.asStateFlow()

    fun markServiceListed() {
        _profile.update { it.copy(hasListedService = true) }
    }
}
