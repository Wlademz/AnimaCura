package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.LicenseManager
import com.example.data.local.LicenseStatus
import com.example.data.local.LicenseTier

/**
 * Fullscreen Lock Screen displayed when trial expires without valid activation
 */
@Composable
fun LicenseExpiredLockScreen(
    licenseStatus: LicenseStatus,
    onActivated: () -> Unit
) {
    val context = LocalContext.current
    var inputKey by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPricingDialog by remember { mutableStateOf(false) }

    if (showPricingDialog) {
        PricingTiersDialog(
            licenseStatus = licenseStatus,
            currentPetCount = 2,
            onDismiss = { showPricingDialog = false },
            onActivated = {
                showPricingDialog = false
                onActivated()
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Aktivácia PetMed Denníka",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "7-dňová bezplatná skúšobná verzia skončila. Pre pokračovanie si aktivujte predplatné cez Google Play (od 1,99 € na 3 mesiace) alebo zadajte licenčný kód.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { showPricingDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Odomknúť cez Google Play", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Device ID container with copy button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Vaše ID zariadenia:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = licenseStatus.deviceId,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Device ID", licenseStatus.deviceId)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "ID zariadenia skopírované", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Kopírovať",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Kopírovať", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = inputKey,
                    onValueChange = {
                        inputKey = it.uppercase()
                        errorMessage = null
                    },
                    label = { Text("Mám kód od autora") },
                    placeholder = { Text("napr. KEY-BAS-XXXX-YYYY...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Kľúč",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("license_key_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        if (inputKey.isBlank()) {
                            errorMessage = "Prosím zadajte aktivačný kód"
                            return@OutlinedButton
                        }
                        val success = LicenseManager.activateWithKey(context, inputKey)
                        if (success) {
                            Toast.makeText(context, "✓ Aplikácia úspešne aktivovaná!", Toast.LENGTH_LONG).show()
                            onActivated()
                        } else {
                            errorMessage = "Neplatný kód. Skontrolujte kód alebo kontaktujte autora."
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Overiť zadaný kód")
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = {
                        LicenseManager.requestUninstall(context)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Odinštalovať",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Odinštalovať aplikáciu", fontSize = 13.sp)
                }
            }
        }
    }
}

/**
 * Interactive Pricing & Tiers Dialog with Google Play In-App Billing
 */
@Composable
fun PricingTiersDialog(
    licenseStatus: LicenseStatus,
    currentPetCount: Int,
    onDismiss: () -> Unit,
    onActivated: () -> Unit
) {
    val context = LocalContext.current
    var inputKey by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showKeyInput by remember { mutableStateOf(false) }
    var selectedTierForConfirm by remember { mutableStateOf<LicenseTier?>(null) }

    if (selectedTierForConfirm != null) {
        val tier = selectedTierForConfirm!!
        AlertDialog(
            onDismissRequest = { selectedTierForConfirm = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text("Objednávka balíka ${tier.title}")
            },
            text = {
                Column {
                    Text(
                        text = "Balík: ${tier.title}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Cena: ${tier.priceEur} (${tier.billingCycle})",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Predplatné a platba prebiehajú cez oficiálnu platobnú bránu Google Play. Licencia sa aktivuje až po úspešnom dokončení a potvrdení platby.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                            ).apply {
                                setPackage("com.android.vending")
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            try {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                                )
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Google Play Store nie je k dispozícii", Toast.LENGTH_SHORT).show()
                            }
                        }
                        Toast.makeText(context, "Presmerovanie do Google Play... Licencia sa aktivuje po dokončení platby.", Toast.LENGTH_LONG).show()
                        selectedTierForConfirm = null
                    }
                ) {
                    Text("Otvoriť Google Play (${tier.priceEur})")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedTierForConfirm = null }) {
                    Text("Zrušiť")
                }
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cenník & Licencie",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zavrieť")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Current tier banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Váš aktuálny stav:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = if (licenseStatus.isActivated) licenseStatus.tier.title else "Skúšobná lehota (${licenseStatus.daysRemaining} dní)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Evidovaných zvierat: $currentPetCount / ${if (licenseStatus.maxPets > 500) "Neobmedzene" else "${licenseStatus.maxPets} zvieratá"}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Vyberte si balík podľa počtu zvierat vo Vašej starostlivosti:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tier 1: ZÁKLAD 1,99 € na 3 mesiace
                TierCard(
                    tier = LicenseTier.BASIC,
                    isCurrent = licenseStatus.isActivated && licenseStatus.tier == LicenseTier.BASIC,
                    onBuyClick = { selectedTierForConfirm = LicenseTier.BASIC }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tier 2: Rodina VIP 4,99 €
                TierCard(
                    tier = LicenseTier.FAMILY_VIP,
                    isCurrent = licenseStatus.isActivated && licenseStatus.tier == LicenseTier.FAMILY_VIP,
                    onBuyClick = { selectedTierForConfirm = LicenseTier.FAMILY_VIP }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tier 3: Chovateľ PRO 14,99 €
                TierCard(
                    tier = LicenseTier.BREEDER_PRO,
                    isCurrent = licenseStatus.isActivated && licenseStatus.tier == LicenseTier.BREEDER_PRO,
                    onBuyClick = { selectedTierForConfirm = LicenseTier.BREEDER_PRO }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tier 4: Útulok & Karanténa 69,99 € na 12 mesiacov
                TierCard(
                    tier = LicenseTier.ORGANIZATION,
                    highlighted = true,
                    isCurrent = licenseStatus.isActivated && licenseStatus.tier == LicenseTier.ORGANIZATION,
                    onBuyClick = { selectedTierForConfirm = LicenseTier.ORGANIZATION }
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (!showKeyInput) {
                    OutlinedButton(
                        onClick = { showKeyInput = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mám promo / aktivačný kód od autora", fontSize = 12.sp)
                    }
                } else {
                    OutlinedTextField(
                        value = inputKey,
                        onValueChange = {
                            inputKey = it.uppercase()
                            errorMessage = null
                        },
                        label = { Text("Zadať promo / aktivačný kód") },
                        placeholder = { Text("napr. KEY-BAS-XXXX-YYYY") },
                        singleLine = true,
                        isError = errorMessage != null,
                        supportingText = {
                            if (errorMessage != null) {
                                Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (inputKey.isBlank()) {
                                errorMessage = "Zadajte kód"
                                return@Button
                            }
                            val success = LicenseManager.activateWithKey(context, inputKey)
                            if (success) {
                                Toast.makeText(context, "✓ Licencia bola úspešne aktivovaná!", Toast.LENGTH_LONG).show()
                                onActivated()
                                onDismiss()
                            } else {
                                errorMessage = "Neplatný kód. Skontrolujte kód alebo kontaktujte autora."
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Overiť a odomknúť licenciu")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Zavrieť")
                }
            }
        }
    }
}

@Composable
private fun TierCard(
    tier: LicenseTier,
    highlighted: Boolean = false,
    isCurrent: Boolean = false,
    onBuyClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (highlighted) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        border = androidx.compose.foundation.BorderStroke(
            width = if (highlighted) 1.5.dp else 1.dp,
            color = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tier.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isCurrent) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "Aktívne",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = tier.priceEur,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = tier.billingCycle,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Kapacita: ${if (tier.maxPets > 500) "NEOBMEDZENE (150+ zvierat)" else "do ${tier.maxPets} zvierat"}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tier.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            if (!isCurrent) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onBuyClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = if (highlighted) {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    } else {
                        ButtonDefaults.filledTonalButtonColors()
                    }
                ) {
                    Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kúpiť (${tier.priceEur})", fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * Dialog allowing user to enter activation key anytime
 */
@Composable
fun EnterLicenseKeyDialog(
    licenseStatus: LicenseStatus,
    onDismiss: () -> Unit,
    onActivated: () -> Unit
) {
    val context = LocalContext.current
    var inputKey by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Aktivácia",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Aktivácia Licencie",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Zadajte promo alebo aktivačný kód od autora.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Device ID info
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ID zariadenia:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = licenseStatus.deviceId,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Device ID", licenseStatus.deviceId)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "ID skopírované", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Kopírovať", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = inputKey,
                    onValueChange = {
                        inputKey = it.uppercase()
                        errorMessage = null
                    },
                    label = { Text("Aktivačný kód") },
                    placeholder = { Text("napr. KEY-BAS-XXXX-YYYY") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (inputKey.isBlank()) {
                            errorMessage = "Prosím zadajte kód"
                            return@Button
                        }
                        val success = LicenseManager.activateWithKey(context, inputKey)
                        if (success) {
                            Toast.makeText(context, "✓ Aplikácia úspešne aktivovaná!", Toast.LENGTH_LONG).show()
                            onActivated()
                            onDismiss()
                        } else {
                            errorMessage = "Neplatný kód. Skontrolujte kód alebo kontaktujte autora."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Overiť a aktivovať", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Zavrieť")
                }
            }
        }
    }
}
