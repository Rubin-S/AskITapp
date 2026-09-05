package com.askit.app.home.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.askit.designsystem.people.AskITAvatar
import com.askit.designsystem.R as DsR
import kotlinx.coroutines.launch

data class ServiceDetailData(
    val id: String,
    val title: String,
    val category: String,
    val providerName: String,
    val providerAvatarUrl: String?,
    val startingPrice: String,
    val rating: Double,
    val reviewCount: Int,
    val responseTime: String,
    val coverageArea: String,
    val description: String,
    val inclusions: List<String>,
)

fun getServiceDetailById(serviceId: String): ServiceDetailData = when (serviceId) {
    "srv-1" -> ServiceDetailData(
        id = serviceId,
        title = "Emergency Plumbing & Drain Cleaning",
        category = "Plumbing",
        providerName = "Apex Plumbing Co.",
        providerAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300",
        startingPrice = "From $75",
        rating = 4.9,
        reviewCount = 54,
        responseTime = "Responds in ~15 mins",
        coverageArea = "Metro Area (within 25 km)",
        description = "Full-service residential and emergency commercial plumbing. Specializing in drain cleaning, pipe repairs, water heater maintenance, faucet replacements, and video sewer line inspections. Available 24/7 with upfront transparent pricing.",
        inclusions = listOf(
            "Complete on-site diagnostic assessment",
            "Professional grade tools & camera inspection",
            "Upfront itemized estimate before work begins",
            "90-day comprehensive service warranty",
        ),
    )
    "srv-2" -> ServiceDetailData(
        id = serviceId,
        title = "Residential Electrical Repairs & Panels",
        category = "Electrical",
        providerName = "Bright Solutions",
        providerAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=300",
        startingPrice = "From $90",
        rating = 4.8,
        reviewCount = 42,
        responseTime = "Responds in ~30 mins",
        coverageArea = "City & Surrounding Suburbs",
        description = "Certified master electricians specializing in residential circuit diagnostics, breaker box replacements, EV charger installations, and modern LED recessed lighting setups.",
        inclusions = listOf(
            "Electrical safety & code compliance inspection",
            "High-grade copper wiring and UL-listed breakers",
            "Clean workspace guarantee",
            "Full labor warranty & permit support",
        ),
    )
    "srv-3" -> ServiceDetailData(
        id = serviceId,
        title = "Custom Cabinetry & Woodworking",
        category = "Carpentry",
        providerName = "Heritage Woodworks",
        providerAvatarUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=300",
        startingPrice = "From $150",
        rating = 4.95,
        reviewCount = 68,
        responseTime = "Responds within 1 hour",
        coverageArea = "Tri-County Area",
        description = "Bespoke hardwood furniture, custom built-in bookshelves, and kitchen cabinetry. Premium hardwoods sourced sustainably with expert joinery and hand-rubbed finishes.",
        inclusions = listOf(
            "In-home measurement and 3D design rendering",
            "Custom material & stain selection",
            "Precision milling and soft-close hardware",
            "Delivery and seamless installation",
        ),
    )
    else -> ServiceDetailData(
        id = serviceId,
        title = "Professional Service ($serviceId)",
        category = "Specialist Service",
        providerName = "Verified Provider",
        providerAvatarUrl = null,
        startingPrice = "From $50",
        rating = 4.9,
        reviewCount = 20,
        responseTime = "Fast Response",
        coverageArea = "Local Area",
        description = "High quality on-demand professional service. Contact provider directly to discuss requirements and get a personalized quote.",
        inclusions = listOf(
            "Initial consultation",
            "Professional equipment and tools",
            "Satisfaction guarantee",
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    serviceId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onRequestService: () -> Unit = {},
) {
    val service = remember(serviceId) { getServiceDetailById(serviceId) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Service Details",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(DsR.drawable.ic_arrow_back),
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Chat initiated with ${service.providerName}")
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Chat")
                    }
                    Button(
                        onClick = {
                            onRequestService()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Service request sent to ${service.providerName}!")
                            }
                        },
                        modifier = Modifier.weight(1.5f),
                    ) {
                        Text("Request Service")
                    }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = service.category,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }

            Text(
                text = service.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier.size(52.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        AskITAvatar(
                            avatarUrl = service.providerAvatarUrl,
                            avatarSize = 52.dp,
                            fallbackIconSize = 32.dp,
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = service.providerName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                painter = painterResource(DsR.drawable.ic_star_filled),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = "%.2f".format(service.rating),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "(${service.reviewCount} reviews)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Starting Price",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = service.startingPrice,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Response Time",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = service.responseTime,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Service Area",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = service.coverageArea,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "About This Service",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = service.description,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Included in Service",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                service.inclusions.forEach { inc ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "✓",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = inc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
