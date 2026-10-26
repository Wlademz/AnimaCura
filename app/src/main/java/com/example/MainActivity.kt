package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.util.InAppUpdateManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.outlined.Emergency
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.LicenseManager
import com.example.data.local.entity.MedicalRecord
import com.example.data.local.entity.Pet
import com.example.ui.components.AboutAppDialog
import com.example.ui.components.AddForumPostDialog
import com.example.ui.components.AddMedicalRecordDialog
import com.example.ui.components.AddMedicationDialog
import com.example.ui.components.AddPetDialog
import com.example.ui.components.AddPhotoDialog
import com.example.ui.components.AddReminderDialog
import com.example.ui.components.AddWeightDialog
import androidx.activity.compose.BackHandler
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.components.ConfirmDeletePetDialog
import com.example.ui.components.EditPetDialog
import com.example.ui.components.EnterLicenseKeyDialog
import com.example.ui.components.ExportVetDialog
import com.example.ui.components.LicenseExpiredLockScreen
import com.example.ui.components.PetPhotoPickerDialog
import com.example.ui.components.PetQrSyncDialog
import com.example.ui.components.PetTopSelectorBar
import com.example.ui.screens.CommunityForumScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EmergencyClinicsScreen
import com.example.ui.screens.MedicalRecordsScreen
import com.example.ui.screens.PetExpensesScreen
import com.example.ui.screens.ToxicityScreen
import com.example.ui.screens.WeightAndGalleryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PetViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: PetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: PetViewModel) {
    val context = LocalContext.current
    var licenseRefreshCounter by remember { mutableIntStateOf(0) }
    val licenseStatus = remember(licenseRefreshCounter) { LicenseManager.getLicenseStatus(context) }

    // If 7-day trial is expired and application is not activated with a valid key, lock the app
    if (licenseStatus.isExpired && !licenseStatus.isActivated) {
        LicenseExpiredLockScreen(
            licenseStatus = licenseStatus,
            onActivated = { licenseRefreshCounter++ }
        )
        return
    }

    val pets by viewModel.allPets.collectAsStateWithLifecycle()
    val selectedPet by viewModel.selectedPet.collectAsStateWithLifecycle()
    val medicalRecords by viewModel.medicalRecords.collectAsStateWithLifecycle()
    val medications by viewModel.medications.collectAsStateWithLifecycle()
    val weightLogs by viewModel.weightLogs.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    val vetClinics by viewModel.vetClinics.collectAsStateWithLifecycle()
    val forumPosts by viewModel.forumPosts.collectAsStateWithLifecycle()
    val selectedPostId by viewModel.selectedPostId.collectAsStateWithLifecycle()
    val postComments by viewModel.postComments.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()

    var currentNavIndex by remember { mutableIntStateOf(0) }
    var currentSecondaryScreen by remember { mutableStateOf<String?>(null) } // "toxicity", "expenses"
    var showBarcodeDialog by remember { mutableStateOf(false) }
    var showQrSyncDialog by remember { mutableStateOf(false) }

    // Dialog state controllers
    var showAddPetDialog by remember { mutableStateOf(false) }
    var showEditPetDialog by remember { mutableStateOf(false) }
    var showConfirmDeletePetDialog by remember { mutableStateOf(false) }
    var petToEditOrDelete by remember { mutableStateOf<Pet?>(null) }
    var showAddRecordDialog by remember { mutableStateOf(false) }
    var recordToEdit by remember { mutableStateOf<MedicalRecord?>(null) }
    var showAddMedDialog by remember { mutableStateOf(false) }
    var drugToAssign by remember { mutableStateOf<com.example.data.local.VeterinaryDrug?>(null) }
    var showAddWeightDialog by remember { mutableStateOf(false) }
    var showAddPhotoDialog by remember { mutableStateOf(false) }
    var showAddReminderDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showAddForumPostDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showEnterKeyDialog by remember { mutableStateOf(false) }
    var petForPhotoChange by remember { mutableStateOf<Pet?>(null) }
    var showPetPhotoDialog by remember { mutableStateOf(false) }

    val inAppUpdateManager = remember { InAppUpdateManager(context) }
    var isUpdateReadyToInstall by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val activity = context as? android.app.Activity
        inAppUpdateManager.checkForUpdate(
            onUpdateAvailable = { updateInfo ->
                if (activity != null) {
                    inAppUpdateManager.startFlexibleUpdate(activity, updateInfo) {
                        isUpdateReadyToInstall = true
                    }
                }
            },
            onNoUpdate = {},
            onError = {}
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            inAppUpdateManager.unregisterInstallListener()
        }
    }

    val navItems = listOf(
        NavItem("Prehľad", Icons.Filled.Pets, Icons.Outlined.Pets, "nav_dashboard"),
        NavItem("Zdravie", Icons.Filled.Medication, Icons.Outlined.Medication, "nav_meds"),
        NavItem("Váha", Icons.Filled.Scale, Icons.Outlined.Scale, "nav_weight"),
        NavItem("Pohotovosť", Icons.Filled.Emergency, Icons.Outlined.Emergency, "nav_emergency"),
        NavItem("Fórum", Icons.Filled.Forum, Icons.Outlined.Forum, "nav_forum")
    )

    Scaffold(
        topBar = {
            PetTopSelectorBar(
                pets = pets,
                selectedPet = selectedPet,
                onSelectPet = { viewModel.selectPet(it) },
                onAddPetClick = { showAddPetDialog = true },
                onEditPetClick = { pet ->
                    petToEditOrDelete = pet
                    showEditPetDialog = true
                },
                onInfoClick = { showAboutDialog = true },
                onChangePhotoClick = { pet ->
                    petForPhotoChange = pet
                    showPetPhotoDialog = true
                }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .testTag("main_bottom_nav")
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = currentNavIndex == index && currentSecondaryScreen == null,
                        onClick = {
                            currentSecondaryScreen = null
                            currentNavIndex = index
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentNavIndex == index && currentSecondaryScreen == null) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title, maxLines = 1) },
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (currentSecondaryScreen != null) {
                BackHandler {
                    currentSecondaryScreen = null
                }
                when (currentSecondaryScreen) {
                    "toxicity" -> ToxicityScreen(
                        selectedPet = selectedPet,
                        onEmergencyCallClick = {
                            currentSecondaryScreen = null
                            currentNavIndex = 3
                        }
                    )
                    "expenses" -> PetExpensesScreen(
                        pet = selectedPet,
                        expenses = expenses,
                        onAddExpense = { title, category, amount, date, notes ->
                            viewModel.addExpense(title, category, amount, date, notes)
                        },
                        onDeleteExpense = { viewModel.deleteExpense(it) }
                    )
                }
            } else {
                when (currentNavIndex) {
                    0 -> DashboardScreen(
                        pet = selectedPet,
                        medications = medications,
                        medicalRecords = medicalRecords,
                        weightLogs = weightLogs,
                        reminders = reminders,
                        onAddRecordClick = { showAddRecordDialog = true },
                        onAddMedClick = { showAddMedDialog = true },
                        onAddWeightClick = { showAddWeightDialog = true },
                        onAddReminderClick = { showAddReminderDialog = true },
                        onMarkDoseTaken = { viewModel.markDoseTaken(it) },
                        onToggleReminder = { viewModel.toggleReminderCompleted(it) },
                        onEmergencyClick = { currentNavIndex = 3 },
                        onExportClick = { showExportDialog = true },
                        onEditPetClick = { pet ->
                            petToEditOrDelete = pet
                            showEditPetDialog = true
                        },
                        onDeletePetClick = { pet ->
                            petToEditOrDelete = pet
                            showConfirmDeletePetDialog = true
                        },
                        onChangePhotoClick = { pet ->
                            petForPhotoChange = pet
                            showPetPhotoDialog = true
                        },
                        onScanBarcodeClick = { showBarcodeDialog = true },
                        onQrSyncClick = { showQrSyncDialog = true },
                        onToxicityClick = { currentSecondaryScreen = "toxicity" },
                        onExpensesClick = { currentSecondaryScreen = "expenses" },
                        onPrintPdfClick = { viewModel.printPetMedicalPassport(context) },
                        onTestNotificationClick = { viewModel.scheduleTestNotification() }
                    )

                1 -> MedicalRecordsScreen(
                    pet = selectedPet,
                    medicalRecords = medicalRecords,
                    medications = medications,
                    latestWeight = weightLogs.firstOrNull()?.weightKg ?: selectedPet?.targetWeightKg,
                    onAddRecordClick = {
                        drugToAssign = null
                        showAddRecordDialog = true
                    },
                    onEditRecordClick = { record ->
                        recordToEdit = record
                    },
                    onAddMedicationClick = {
                        drugToAssign = null
                        showAddMedDialog = true
                    },
                    onDeleteRecord = { viewModel.deleteMedicalRecord(it) },
                    onDeleteMedication = { viewModel.deleteMedication(it) },
                    onMarkDoseTaken = { viewModel.markDoseTaken(it) },
                    onToggleMedActive = { viewModel.toggleMedicationActive(it) },
                    onExportClick = { showExportDialog = true },
                    onAssignDrugToPet = { drug ->
                        drugToAssign = drug
                        showAddMedDialog = true
                    }
                )

                2 -> WeightAndGalleryScreen(
                    pet = selectedPet,
                    weightLogs = weightLogs,
                    photos = photos,
                    onAddWeightClick = { showAddWeightDialog = true },
                    onAddPhotoClick = { showAddPhotoDialog = true },
                    onDeleteWeightLog = { viewModel.deleteWeightLog(it) },
                    onDeletePhoto = { viewModel.deletePhoto(it) }
                )

                3 -> EmergencyClinicsScreen(
                    clinics = vetClinics
                )

                4 -> CommunityForumScreen(
                    posts = forumPosts,
                    selectedPostId = selectedPostId,
                    postComments = postComments,
                    onSelectPost = { viewModel.selectPost(it) },
                    onLikePost = { viewModel.toggleLikePost(it) },
                    onAddComment = { postId, comment, author ->
                        viewModel.addForumComment(postId, comment, author)
                    },
                    onNewPostClick = { showAddForumPostDialog = true }
                )
            }

            if (isUpdateReadyToInstall) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Nová verzia je pripravená!",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Kliknite pre reštart a inštaláciu aktualizácie.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                        Button(
                            onClick = { inAppUpdateManager.completeUpdate() }
                        ) {
                            Text("Inštalovať")
                        }
                    }
                }
            }
        }
    }
}

    // Interactive Dialogs
    if (showAddPetDialog) {
        AddPetDialog(
            onDismiss = { showAddPetDialog = false },
            onConfirm = { name, species, breed, birthDate, gender, chip, passport, vetName, vetClinic, vetPhone, vetEmail, targetWeight, colorHex, adoptedFrom, adoptedBy, adoptionDate, imageUri ->
                viewModel.addPet(
                    name, species, breed, birthDate, gender, chip, passport,
                    vetName, vetClinic, vetPhone, vetEmail, targetWeight, colorHex,
                    adoptedFrom, adoptedBy, adoptionDate, imageUri
                )
                showAddPetDialog = false
            }
        )
    }

    if (showPetPhotoDialog && petForPhotoChange != null) {
        PetPhotoPickerDialog(
            petName = petForPhotoChange!!.name,
            currentImageUri = petForPhotoChange!!.imageUri,
            onDismiss = {
                showPetPhotoDialog = false
                petForPhotoChange = null
            },
            onPhotoSelected = { newPath ->
                viewModel.updatePetPhoto(petForPhotoChange!!, newPath)
                showPetPhotoDialog = false
                petForPhotoChange = null
            },
            onPhotoRemoved = {
                viewModel.updatePetPhoto(petForPhotoChange!!, "")
                showPetPhotoDialog = false
                petForPhotoChange = null
            }
        )
    }

    if (showEditPetDialog && petToEditOrDelete != null) {
        EditPetDialog(
            pet = petToEditOrDelete!!,
            onDismiss = { showEditPetDialog = false },
            onConfirm = { updatedPet ->
                viewModel.updatePet(updatedPet)
                showEditPetDialog = false
            },
            onDeleteRequest = {
                showEditPetDialog = false
                showConfirmDeletePetDialog = true
            }
        )
    }

    if (showConfirmDeletePetDialog && petToEditOrDelete != null) {
        ConfirmDeletePetDialog(
            petName = petToEditOrDelete!!.name,
            onDismiss = { showConfirmDeletePetDialog = false },
            onConfirmDelete = {
                viewModel.deletePet(petToEditOrDelete!!)
                showConfirmDeletePetDialog = false
                petToEditOrDelete = null
            }
        )
    }

    if (showAddRecordDialog) {
        AddMedicalRecordDialog(
            onDismiss = { showAddRecordDialog = false },
            onConfirm = { type, title, date, clinic, diagnosis, cost, notes, nextDueDate ->
                viewModel.addMedicalRecord(type, title, date, clinic, diagnosis, cost, notes, nextDueDate)
                showAddRecordDialog = false
            }
        )
    }

    if (recordToEdit != null) {
        AddMedicalRecordDialog(
            recordToEdit = recordToEdit,
            onDismiss = { recordToEdit = null },
            onConfirm = { type, title, date, clinic, diagnosis, cost, notes, nextDueDate ->
                viewModel.updateMedicalRecord(
                    recordId = recordToEdit!!.id,
                    type = type,
                    title = title,
                    date = date,
                    clinic = clinic,
                    diagnosis = diagnosis,
                    cost = cost,
                    notes = notes,
                    nextDueDate = nextDueDate
                )
                recordToEdit = null
            }
        )
    }

    if (showAddMedDialog) {
        AddMedicationDialog(
            initialDrug = drugToAssign,
            onDismiss = {
                showAddMedDialog = false
                drugToAssign = null
            },
            onConfirm = { name, dosage, frequency, form, startDate, endDate, totalDoses, timingNotes, prescribingVet ->
                viewModel.addMedication(
                    name, dosage, frequency, form, startDate, endDate, totalDoses, timingNotes, prescribingVet
                )
                showAddMedDialog = false
                drugToAssign = null
            }
        )
    }

    if (showAddWeightDialog) {
        AddWeightDialog(
            onDismiss = { showAddWeightDialog = false },
            onConfirm = { weight, date, notes ->
                viewModel.addWeightLog(weight, date, notes)
                showAddWeightDialog = false
            }
        )
    }

    if (showAddPhotoDialog) {
        AddPhotoDialog(
            onDismiss = { showAddPhotoDialog = false },
            onConfirm = { title, category, date, description ->
                viewModel.addPhoto(title, category, date, description)
                showAddPhotoDialog = false
            }
        )
    }

    if (showAddReminderDialog) {
        AddReminderDialog(
            onDismiss = { showAddReminderDialog = false },
            onConfirm = { title, type, dueDate, dueTime, notes ->
                viewModel.addReminder(title, type, dueDate, dueTime, notes)
                showAddReminderDialog = false
            }
        )
    }

    if (showExportDialog && selectedPet != null) {
        ExportVetDialog(
            pet = selectedPet!!,
            medications = medications,
            records = medicalRecords,
            weightLogs = weightLogs,
            onDismiss = { showExportDialog = false },
            onSendEmail = { note ->
                viewModel.exportMedicalRecordToVet(context, note)
            }
        )
    }

    if (showAddForumPostDialog) {
        AddForumPostDialog(
            onDismiss = { showAddForumPostDialog = false },
            onConfirm = { title, content, author, tag ->
                viewModel.addForumPost(title, content, author, tag)
                showAddForumPostDialog = false
            }
        )
    }

    if (showAboutDialog) {
        AboutAppDialog(
            onDismiss = { showAboutDialog = false },
            onEnterKeyClick = { showEnterKeyDialog = true }
        )
    }

    if (showBarcodeDialog) {
        BarcodeScannerDialog(
            petName = selectedPet?.name ?: "Zvieratko",
            onDismiss = { showBarcodeDialog = false },
            onAddMedication = { drug, dosage, freq ->
                viewModel.addDrugToMedications(drug, dosage, freq)
            },
            onAddExpense = { drug, amount ->
                viewModel.addDrugToExpenses(drug, amount)
            }
        )
    }

    if (showQrSyncDialog) {
        PetQrSyncDialog(
            selectedPet = selectedPet,
            getQrBitmap = { viewModel.getPetSyncQrCodeBitmap(512) },
            getSyncPackage = { viewModel.getPetSyncPackage() },
            onImportSyncData = { sync, onComplete ->
                viewModel.importPetSyncData(sync, onComplete)
            },
            onPrintPdf = { viewModel.printPetMedicalPassport(context) },
            onDismiss = { showQrSyncDialog = false }
        )
    }

    if (showEnterKeyDialog) {
        EnterLicenseKeyDialog(
            licenseStatus = licenseStatus,
            onDismiss = { showEnterKeyDialog = false },
            onActivated = { licenseRefreshCounter++ }
        )
    }
}

data class NavItem(
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)
