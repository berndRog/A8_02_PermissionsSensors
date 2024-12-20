package de.rogallab.mobile.ui.features.people

import androidx.compose.runtime.Immutable
import de.rogallab.mobile.domain.entities.Person

@Immutable
data class PeopleUiState(
   val loading: Boolean = false,
   val people: List<Person> = emptyList()
)