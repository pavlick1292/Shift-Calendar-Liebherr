package com.example.shiftcalendar.ui.people

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shiftcalendar.di.AppContainer
import com.example.shiftcalendar.ui.crews.vmFactory
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    container: AppContainer,
    personId: Long,
    navController: NavController
) {
    val vm: PersonDetailViewModel = viewModel(
        key = "person_$personId",
        factory = vmFactory { PersonDetailViewModel(container, personId) }
    )
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.person?.fullName ?: "Человек") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.person?.let { p ->
                item {
                    Card {
                        Column(Modifier.padding(16.dp)) {
                            Text(p.profession.titleRu,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text("🏠 ${p.residence.titleRu}",
                                style = MaterialTheme.typography.bodyLarge)
                            if (p.phone.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Phone, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(p.phone, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                            if (p.note.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(p.note,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            item {
                Text("Составы",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary)
            }

            items(state.crewNames, key = { it.first }) { (_, crewName) ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(crewName, Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            state.hours?.let { h ->
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Часы за ${h.year}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary)
                }
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            HoursRow("Обычные", h.regularHours)
                            HorizontalDivider(Modifier.padding(vertical = 8.dp))
                            HoursRow("Итого", h.total, bold = true)
                            HoursRow("Норма", h.yearlyNorm)
                            if (h.overtime > 0) {
                                HoursRow("Переработка", h.overtime, accent = true)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HoursRow(label: String, value: Double, bold: Boolean = false, accent: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Text(
            String.format(Locale.US, "%.0f ч", value),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = if (accent) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
        )
    }
}
