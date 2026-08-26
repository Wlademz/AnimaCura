package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
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
import com.example.data.local.entity.Pet
import com.example.ui.components.AddForumPostDialog
import com.example.ui.components.AddMedicalRecordDialog
import com.example.ui.components.AddMedicationDialog
import com.example.ui.components.AddPetDialog
import com.example.ui.components.AddPhotoDialog
import com.example.ui.components.AddReminderDialog
import com.example.ui.components.AddWeightDialog
import com.example.ui.components.ConfirmDeletePetDialog
import com.example.ui.components.EditPetDialog
import com.example.ui.components.ExportVetDialog
import com.example.ui.components.PetTopSelectorBar
import com.example.ui.screens.CommunityForumScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EmergencyClinicsScreen
import com.example.ui.screens.MedicalRecordsScreen
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

    var currentNavIndex by remember { mutableIntStateOf(0) }

    // Dialog state controllers
    var showAddPetDialog by remember { mutableStateOf(false) }
    var showEditPetDialog by remember { mutableStateOf(false) }
    var showConfirmDeletePetDialog by remember { mutableStateOf(false) }
    var petToEditOrDelete by remember { mutableStateOf<Pet?>(null) }
    var showAddRecordDialog by remember { mutableStateOf(false) }
    var showAddMedDialog by remember { mutableStateOf(false) }
    var drugToAssign by remember { mutableStateOf<com.example.data.local.VeterinaryDrug?>(null) }
    var showAddWeightDialog by remember { mutableStateOf(false) }
    var showAddPhotoDialog by remember { mutableStateOf(false) }
    var showAddReminderDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showAddForumPostDialog by remember { mutableStateOf(false) }

    val navItems = listOf(
        NavItem("Prehľad", Icons.Filled.Pets, Icons.Outlined.Pets, "nav_dashboard"),
        NavItem("Zdravie", Icons.Filled.Medication, Icons.Outlined.Medication, "nav_meds"),
        NavItem("Váha & Foto", Icons.Filled.Scale, Icons.Outlined.Scale, "nav_weight"),
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
                        selected = currentNavIndex == index,
                        onClick = { currentNavIndex = index },
                        icon = {
                            Icon(
                                imageVector = if (currentNavIndex == index) item.selectedIcon else item.unselectedIcon,
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
                    }
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
        }
    }

    // Interactive Dialogs
    if (showAddPetDialog) {
        AddPetDialog(
            onDismiss = { showAddPetDialog = false },
            onConfirm = { name, species, breed, birthDate, gender, chip, passport, vetName, vetClinic, vetPhone, vetEmail, targetWeight, colorHex ->
                viewModel.addPet(
                    name, species, breed, birthDate, gender, chip, passport,
                    vetName, vetClinic, vetPhone, vetEmail, targetWeight, colorHex
                )
                showAddPetDialog = false
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
}

data class NavItem(
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)
