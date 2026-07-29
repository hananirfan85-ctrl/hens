package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KycStatus
import com.example.data.model.LivestockCategory
import com.example.data.model.TransactionStatus
import com.example.data.model.UserRole
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoultryVestTopBar(
    title: String,
    userRole: UserRole,
    unreadNotificationCount: Int = 0,
    onToggleRole: () -> Unit,
    onNotificationClick: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Egg,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            // Role Switch Badge Button
            AssistChip(
                onClick = onToggleRole,
                label = {
                    Text(
                        text = if (userRole == UserRole.INVESTOR) "Investor" else "Admin",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (userRole == UserRole.INVESTOR) Icons.Default.Person else Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (userRole == UserRole.INVESTOR) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                    labelColor = if (userRole == UserRole.INVESTOR) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                ),
                border = null,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .testTag("role_toggle_chip")
            )

            // Notifications Icon
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier.testTag("notifications_button")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadNotificationCount > 0) {
                            Badge { Text("$unreadNotificationCount") }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    )
}

enum class NavigationTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    MARKETPLACE("Marketplace", Icons.Default.Storefront),
    MY_LIVESTOCK("Hen Flocks", Icons.Default.Egg),
    WALLET("Wallet", Icons.Default.AccountBalanceWallet),
    PROFILE("Profile", Icons.Default.Person)
}

@Composable
fun FarmVestBottomNavigation(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavigationTab.entries.forEach { tab ->
            val selected = tab == currentTab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                },
                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
            )
        }
    }
}

@Composable
fun KycStatusBadge(status: KycStatus) {
    val (bgColor, textColor, label, icon) = when (status) {
        KycStatus.VERIFIED -> Quadruple(
            Color(0xFFE6F4EA),
            StatusActiveGreen,
            "Verified Investor",
            Icons.Default.VerifiedUser
        )
        KycStatus.PENDING -> Quadruple(
            Color(0xFFFEF3C7),
            StatusPendingAmber,
            "KYC Pending",
            Icons.Default.HourglassTop
        )
        KycStatus.NOT_VERIFIED -> Quadruple(
            Color(0xFFFEE2E2),
            StatusFailedRed,
            "KYC Required",
            Icons.Default.Warning
        )
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.wrapContentSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = textColor
            )
        }
    }
}

@Composable
fun TransactionStatusBadge(status: TransactionStatus) {
    val (bgColor, textColor, label) = when (status) {
        TransactionStatus.COMPLETED -> Triple(Color(0xFFE8F5E9), StatusActiveGreen, "COMPLETED")
        TransactionStatus.PENDING -> Triple(Color(0xFFFFF8E1), StatusPendingAmber, "PENDING")
        TransactionStatus.REJECTED -> Triple(Color(0xFFFFEBEE), StatusFailedRed, "REJECTED")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CategoryChip(
    category: LivestockCategory,
    selected: Boolean,
    onClick: () -> Unit
) {
    val label = when (category) {
        LivestockCategory.ALL -> "All Flocks"
        LivestockCategory.LAYER_HENS -> "Egg Layers"
        LivestockCategory.FREE_RANGE_LAYERS -> "Free-Range Hens"
        LivestockCategory.BROILER_BREEDERS -> "Breeder Flocks"
        LivestockCategory.HATCHERY_FLOCKS -> "Hatchery Pullets"
        LivestockCategory.ORGANIC_HERITAGE -> "Organic Heritage"
    }

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        leadingIcon = {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier
            .padding(end = 6.dp)
            .testTag("category_chip_${category.name.lowercase()}")
    )
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
