package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.ForumDao
import com.example.data.local.dao.MedicalRecordDao
import com.example.data.local.dao.MedicationDao
import com.example.data.local.dao.PetDao
import com.example.data.local.dao.PetPhotoDao
import com.example.data.local.dao.ReminderDao
import com.example.data.local.dao.VetClinicDao
import com.example.data.local.dao.WeightLogDao
import com.example.data.local.entity.Expense
import com.example.data.local.entity.ForumComment
import com.example.data.local.entity.ForumPost
import com.example.data.local.entity.MedicalRecord
import com.example.data.local.entity.Medication
import com.example.data.local.entity.Pet
import com.example.data.local.entity.PetPhoto
import com.example.data.local.entity.Reminder
import com.example.data.local.entity.VetClinic
import com.example.data.local.entity.WeightLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Pet::class,
        MedicalRecord::class,
        Medication::class,
        WeightLog::class,
        Reminder::class,
        PetPhoto::class,
        VetClinic::class,
        ForumPost::class,
        ForumComment::class,
        Expense::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun medicalRecordDao(): MedicalRecordDao
    abstract fun medicationDao(): MedicationDao
    abstract fun weightLogDao(): WeightLogDao
    abstract fun reminderDao(): ReminderDao
    abstract fun petPhotoDao(): PetPhotoDao
    abstract fun vetClinicDao(): VetClinicDao
    abstract fun forumDao(): ForumDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE pet_photos ADD COLUMN imageUri TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medical_records ADD COLUMN photoUri TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE medical_records ADD COLUMN attachmentUri TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE medical_records ADD COLUMN attachmentName TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE vet_clinics ADD COLUMN userRating INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE vet_clinics ADD COLUMN userReview TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE vet_clinics ADD COLUMN clinicType TEXT NOT NULL DEFAULT 'Pohotovosť'")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pet_health_database"
                )
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        ensureVetClinics(database)
                        ensureForumDiscussions(database)
                        ensureIllustrativePhotos(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(db: AppDatabase) {
            val petDao = db.petDao()
            val medicalDao = db.medicalRecordDao()
            val medDao = db.medicationDao()
            val weightDao = db.weightLogDao()
            val reminderDao = db.reminderDao()
            val photoDao = db.petPhotoDao()
            val clinicDao = db.vetClinicDao()
            val forumDao = db.forumDao()

            // 1. Initial Pets
            val pet1Id = petDao.insertPet(
                Pet(
                    id = 1,
                    name = "Bary",
                    species = "Pes",
                    breed = "Zlatý retriever",
                    birthDate = "15.03.2021",
                    gender = "Samec (kastrovaný)",
                    chipNumber = "941000025893147",
                    passportNumber = "SK-1849204",
                    vetName = "MVDr. Martin Kováč",
                    vetClinic = "Veterinárna klinika Bennett Bratislava",
                    vetPhone = "+421 905 112 233",
                    vetEmail = "kovac@vetclinic.sk",
                    avatarColorHex = 0xFF0D9488,
                    targetWeightKg = 31.5,
                    adoptedFrom = "Útulok Sloboda zvierat Bratislava",
                    adoptedBy = "Vladimír & Rodina",
                    adoptionDate = "20.05.2021",
                    imageUri = "android.resource://com.example/drawable/img_bary_dog_1793267982723"
                )
            )

            val pet2Id = petDao.insertPet(
                Pet(
                    id = 2,
                    name = "Luna",
                    species = "Mačka",
                    breed = "Britská krátkosrstá",
                    birthDate = "10.08.2022",
                    gender = "Samica",
                    chipNumber = "941000049210984",
                    passportNumber = "SK-3021948",
                    vetName = "MVDr. Zuzana Nováková",
                    vetClinic = "VetPoint Pohotovosť & Klinika",
                    vetPhone = "+421 911 887 766",
                    vetEmail = "novakova@vetpoint.sk",
                    avatarColorHex = 0xFFE07A5F,
                    targetWeightKg = 4.3,
                    adoptedFrom = "Chovná stanica Little Whiskers",
                    adoptedBy = "Elena K.",
                    adoptionDate = "15.10.2022",
                    imageUri = "android.resource://com.example/drawable/img_luna_cat_1793268004228"
                )
            )

            // 2. Medical Records (Beyond standard rabies)
            medicalDao.insertRecord(
                MedicalRecord(
                    petId = pet1Id,
                    type = "Očkovanie",
                    title = "Nobivac DHPPi + L4 (Kombinovaná vakcína)",
                    date = "12.01.2024",
                    vetClinic = "Klinika Bennett Bratislava",
                    diagnosis = "Pravidelná revakcinácia: psinka, hepatitída, parvoviróza, parainfluenza a leptospiróza.",
                    costEur = 42.0,
                    notes = "Bez vedľajších reakcií, aplikačné miesto bez opuchu.",
                    nextDueDate = "12.01.2025"
                )
            )
            medicalDao.insertRecord(
                MedicalRecord(
                    petId = pet1Id,
                    type = "Očkovanie",
                    title = "Nobivac Rabies (Besnota)",
                    date = "12.01.2024",
                    vetClinic = "Klinika Bennett Bratislava",
                    diagnosis = "Povinná revakcinácia proti besnote na 1 rok.",
                    costEur = 25.0,
                    notes = "Zapísané v pase aj registri.",
                    nextDueDate = "12.01.2025"
                )
            )
            medicalDao.insertRecord(
                MedicalRecord(
                    petId = pet1Id,
                    type = "Krvné testy",
                    title = "Preventívny biochemický a hematologický profil",
                    date = "15.05.2024",
                    vetClinic = "Klinika Bennett Bratislava",
                    diagnosis = "Mierne zvýšená hodnota pečeňových enzýmov (ALT 92 U/L), obličkové parametre v norme.",
                    costEur = 68.5,
                    notes = "Doporučená pečeňová diéta a hepatoprotektívum (Epato 1500 Plus) na 30 dní.",
                    nextDueDate = "15.11.2024"
                )
            )
            medicalDao.insertRecord(
                MedicalRecord(
                    petId = pet1Id,
                    type = "RTG / Sono",
                    title = "RTG bedrových a lakťových kĺbov",
                    date = "20.10.2023",
                    vetClinic = "Univerzitná vet. nemocnica",
                    diagnosis = "Mierna dysplázia ľavého bedrového kĺbu (stupeň B).",
                    costEur = 95.0,
                    notes = "Nasadená kĺbová výživa Alavis Triple Blend a udržiavanie optimálnej hmotnosti.",
                    nextDueDate = ""
                )
            )
            medicalDao.insertRecord(
                MedicalRecord(
                    petId = pet1Id,
                    type = "Operácia / Zákrok",
                    title = "Kastrácia & preventívne čistenie zubov",
                    date = "15.06.2022",
                    vetClinic = "Klinika Bennett Bratislava",
                    diagnosis = "Elektívny chirurgický zákrok bez komplikácií. Inhalačná anestézia, resorbovateľný šijací materiál.",
                    costEur = 185.0,
                    notes = "Pooperačný priebeh bez opuchu, pacient toleroval golier.",
                    nextDueDate = ""
                )
            )
            medicalDao.insertRecord(
                MedicalRecord(
                    petId = pet1Id,
                    type = "Rozbor moču",
                    title = "Preventívny rozbor moču a sedimentu",
                    date = "18.06.2024",
                    vetClinic = "Klinika Bennett Bratislava",
                    diagnosis = "Špecifická hmotnosť SG 1.030, pH 6.5, bielkoviny neg., glukóza neg., sediment bez kryštálov.",
                    costEur = 22.0,
                    notes = "Všetky parametre v norme, kontrola opäť pri preventívke.",
                    nextDueDate = "18.06.2025"
                )
            )
            medicalDao.insertRecord(
                MedicalRecord(
                    petId = pet1Id,
                    type = "Odčervenie",
                    title = "Milprazon 12.5 mg/125 mg tablety",
                    date = "01.07.2024",
                    vetClinic = "Domáca aplikácia",
                    diagnosis = "Preventívne odčervenie proti oblým a plochým červom.",
                    costEur = 12.0,
                    notes = "Dávka 1.5 tablety podľa váhy.",
                    nextDueDate = "01.10.2024"
                )
            )

            medicalDao.insertRecord(
                MedicalRecord(
                    petId = pet2Id,
                    type = "Očkovanie",
                    title = "Purevax RCPCh (Mačacia trojkombinácia + Chlamýdie)",
                    date = "14.02.2024",
                    vetClinic = "VetPoint",
                    diagnosis = "Revakcinácia: panleukopénia, kaliciviróza, herpesviróza.",
                    costEur = 38.0,
                    notes = "Výborne znášané.",
                    nextDueDate = "14.02.2025"
                )
            )
            medicalDao.insertRecord(
                MedicalRecord(
                    petId = pet2Id,
                    type = "Operácia / Zákrok",
                    title = "Kastrácia / Laparoskopická sterilizácia",
                    date = "15.03.2023",
                    vetClinic = "VetPoint Klinika",
                    diagnosis = "Šetrný laparoskopický zákrok. Rana bez reakcie, zhojená do 7 dní.",
                    costEur = 130.0,
                    notes = "Rekonvalescencia bez komplikácií.",
                    nextDueDate = ""
                )
            )
            medicalDao.insertRecord(
                MedicalRecord(
                    petId = pet2Id,
                    type = "Rozbor moču",
                    title = "Vyšetrenie moču na struvity a hustotu",
                    date = "10.01.2024",
                    vetClinic = "VetPoint Klinika",
                    diagnosis = "pH 6.8, prítomné drobné kryštáliky struvitov, mierne znížený príjem tekutín.",
                    costEur = 20.0,
                    notes = "Doporučené podávať vlhké krmivo a fontánku na pitie.",
                    nextDueDate = "10.07.2024"
                )
            )
            medicalDao.insertRecord(
                MedicalRecord(
                    petId = pet2Id,
                    type = "Zuby",
                    title = "Ultrazvukové čistenie zubného kameňa",
                    date = "05.04.2024",
                    vetClinic = "VetPoint",
                    diagnosis = "Odstránenie zubného povlaku a leštenie zubov v sedácii.",
                    costEur = 75.0,
                    notes = "Doma aplikovať Stomaferin gél na ďasná.",
                    nextDueDate = "05.04.2025"
                )
            )

            // 3. Medications & Dosage Tracking
            medDao.insertMedication(
                Medication(
                    petId = pet1Id,
                    name = "Apoquel 16 mg",
                    dosage = "1 tableta",
                    frequency = "1x denne (ráno po kŕmení)",
                    form = "Tabletka",
                    startDate = "10.08.2024",
                    endDate = "30.08.2024",
                    isActive = true,
                    totalDoses = 20,
                    takenDoses = 14,
                    timingNotes = "Pri svrbení a atopickej dermatitíde, podávať vždy s jedlom.",
                    prescribingVet = "MVDr. Martin Kováč"
                )
            )
            medDao.insertMedication(
                Medication(
                    petId = pet1Id,
                    name = "Alavis Triple Blend Extra Silný",
                    dosage = "1/2 odmerky (cca 2.2 g)",
                    frequency = "1x denne do krmiva",
                    form = "Kĺbová výživa / Doplnok",
                    startDate = "01.01.2024",
                    endDate = "Dlhodobo",
                    isActive = true,
                    totalDoses = 90,
                    takenDoses = 65,
                    timingNotes = "Zmiešať s trochou mokrého krmiva alebo konzervy.",
                    prescribingVet = "MVDr. Martin Kováč"
                )
            )
            medDao.insertMedication(
                Medication(
                    petId = pet1Id,
                    name = "Nexgard Spectra (30-60 kg)",
                    dosage = "1 žuvacia tableta",
                    frequency = "1x mesačne",
                    form = "Tabletka",
                    startDate = "01.08.2024",
                    endDate = "01.08.2024",
                    isActive = false,
                    totalDoses = 1,
                    takenDoses = 1,
                    timingNotes = "Ochrana pred kliešťami, blchami a srdcovou červivosťou.",
                    prescribingVet = "MVDr. Martin Kováč"
                )
            )

            medDao.insertMedication(
                Medication(
                    petId = pet2Id,
                    name = "Stomaferin dentálny gél",
                    dosage = "1 pumpička na ďasná",
                    frequency = "2x denne (ráno a večer)",
                    form = "Masť / Gél",
                    startDate = "06.04.2024",
                    endDate = "06.05.2024",
                    isActive = true,
                    totalDoses = 60,
                    takenDoses = 48,
                    timingNotes = "Neaplikovať bezprostredne pred pitím vody.",
                    prescribingVet = "MVDr. Zuzana Nováková"
                )
            )

            // 4. Weight Logs for Graph Visualization
            val weightDataPet1 = listOf(
                Pair("15.01.2024", 33.2),
                Pair("15.02.2024", 32.8),
                Pair("15.03.2024", 32.4),
                Pair("15.04.2024", 32.0),
                Pair("15.05.2024", 31.8),
                Pair("15.06.2024", 31.6),
                Pair("15.07.2024", 31.5),
                Pair("15.08.2024", 31.4)
            )
            weightDataPet1.forEachIndexed { index, pair ->
                weightDao.insertWeightLog(
                    WeightLog(
                        petId = pet1Id,
                        date = pair.first,
                        timestamp = System.currentTimeMillis() - ((weightDataPet1.size - 1 - index) * 30L * 86400000L),
                        weightKg = pair.second,
                        notes = if (index == 0) "Začiatok úpravy diéty" else "Pravidelná kontrola"
                    )
                )
            }

            val weightDataPet2 = listOf(
                Pair("10.02.2024", 4.1),
                Pair("10.04.2024", 4.2),
                Pair("10.06.2024", 4.3),
                Pair("10.08.2024", 4.35)
            )
            weightDataPet2.forEachIndexed { index, pair ->
                weightDao.insertWeightLog(
                    WeightLog(
                        petId = pet2Id,
                        date = pair.first,
                        timestamp = System.currentTimeMillis() - ((weightDataPet2.size - 1 - index) * 60L * 86400000L),
                        weightKg = pair.second,
                        notes = "Stabilná hmotnosť"
                    )
                )
            }

            // 5. Reminders
            reminderDao.insertReminder(
                Reminder(
                    petId = pet1Id,
                    title = "Ranná dávka Apoquel 16mg",
                    type = "Dávka lieku",
                    dueDate = "Dnes",
                    dueTime = "08:00",
                    isCompleted = false,
                    notes = "Podať s krmivom"
                )
            )
            reminderDao.insertReminder(
                Reminder(
                    petId = pet1Id,
                    title = "Podanie odčervenia (Milprazon)",
                    type = "Odčervenie",
                    dueDate = "01.10.2024",
                    dueTime = "10:00",
                    isCompleted = false,
                    notes = "3-mesačný interval odčervenia"
                )
            )
            reminderDao.insertReminder(
                Reminder(
                    petId = pet1Id,
                    title = "Kontrolný odber krvi (pečeňový profil)",
                    type = "Kontrola u veta",
                    dueDate = "15.11.2024",
                    dueTime = "09:30",
                    isCompleted = false,
                    notes = "Nalačno pred odberom (min. 8 hodín bez krmiva)"
                )
            )
            reminderDao.insertReminder(
                Reminder(
                    petId = pet2Id,
                    title = "Stomaferin gél na ďasná",
                    type = "Dávka lieku",
                    dueDate = "Dnes",
                    dueTime = "20:00",
                    isCompleted = false,
                    notes = "Večerná aplikácia"
                )
            )

            // 6. Photo Gallery for Healing/Teeth/Prescriptions
            photoDao.insertPhoto(
                PetPhoto(
                    petId = pet1Id,
                    title = "Kontrola labky po škrabanci",
                    category = "Hojenie rany",
                    date = "12.08.2024",
                    description = "Rana na vankúšiku je čistá, hojí sa bez zápalu.",
                    imageUri = "android.resource://com.example/drawable/img_bary_paw_1793268027108",
                    drawableResName = "img_bary_paw_1793268027108"
                )
            )
            photoDao.insertPhoto(
                PetPhoto(
                    petId = pet1Id,
                    title = "Portrét Baryho v záhrade",
                    category = "Portrét",
                    date = "01.07.2024",
                    description = "Veselý a zdravý psík, hustá zlatá srsť po prečesaní.",
                    imageUri = "android.resource://com.example/drawable/img_bary_dog_1793267982723",
                    drawableResName = "img_bary_dog_1793267982723"
                )
            )
            photoDao.insertPhoto(
                PetPhoto(
                    petId = pet1Id,
                    title = "Lekárska správa z RTG",
                    category = "Lekárska správa / Recept",
                    date = "20.10.2023",
                    description = "Oficiálny posudok dysplázie bedrových kĺbov.",
                    imageUri = "android.resource://com.example/drawable/vet_medicine_box_icon_1787724951811",
                    drawableResName = "vet_medicine_box_icon_1787724951811"
                )
            )
            photoDao.insertPhoto(
                PetPhoto(
                    petId = pet2Id,
                    title = "Stav ďasien po ultrazvukovom čistení",
                    category = "Zuby / Ústna dutina",
                    date = "10.04.2024",
                    description = "Ďasná bez začervenania, zubný kameň úplne odstránený.",
                    imageUri = "android.resource://com.example/drawable/img_luna_teeth_1793268048085",
                    drawableResName = "img_luna_teeth_1793268048085"
                )
            )
            photoDao.insertPhoto(
                PetPhoto(
                    petId = pet2Id,
                    title = "Portrét Luny na deke",
                    category = "Portrét",
                    date = "10.08.2024",
                    description = "Pokojná a prítulná mačička po veterinárnej kontrole.",
                    imageUri = "android.resource://com.example/drawable/img_luna_cat_1793268004228",
                    drawableResName = "img_luna_cat_1793268004228"
                )
            )

            // 7. Vet Emergency & Clinics Database (Real Slovakia locations)
            clinicDao.insertClinics(getDefaultVetClinics())

            // 8. Discussion Forum for Pet Owners
            val forumPosts = listOf(
                ForumPost(
                    id = 1,
                    authorName = "Peter & Max",
                    petTypeTag = "Liečba & Lieky",
                    title = "Skúsenosti s Apoquelom vs Cytopoint injekciami pri atopii psa",
                    content = "Náš labrador sa na jar a v lete strašne škriabe okolo uší a labiek. Berieme Apoquel 16mg, ktorý zaberá rýchlo, ale vet nám navrhol vyskúšať Cytopoint injekciu raz mesačne. Máte s tým niekto dlhodobejšie skúsenosti? Ako to znášajú?",
                    dateStr = "Pred 2 dňami",
                    likesCount = 14,
                    commentsCount = 3
                ),
                ForumPost(
                    id = 2,
                    authorName = "Lucia - Mačací raj",
                    petTypeTag = "Výživa",
                    title = "Ako naučiť mačku piť viac vody pri prevencii močových kameňov?",
                    content = "Dobrý deň, kastrovaný kocúrik mal nález struvitov v moči. Lekár nám odporučil zvýšiť príjem tekutín a močovú diétu. Kúpili sme keramickú fontánku a pridávame teplú vodu do konzervy. Aké máte ďalšie overené triky?",
                    dateStr = "Včera o 14:20",
                    likesCount = 22,
                    commentsCount = 4
                ),
                ForumPost(
                    id = 3,
                    authorName = "MVDr. Andrea (Poradňa)",
                    petTypeTag = "Očkovanie",
                    title = "Prečo nestačí registrovať len besnotu? Dôležitosť DHPPi a L4",
                    content = "Mnoho majiteľov si myslí, že ak má pes zo zákona besnotu, je plne chránený. Leptospiróza z kaluží či parvoviróza sú však u mladých aj starších psov mimoriadne nebezpečné. Nezabúdajte na každoročné preočkovanie kombinovanej vakcíny!",
                    dateStr = "Pred 4 dňami",
                    likesCount = 38,
                    commentsCount = 5
                )
            )
            forumDao.insertPosts(forumPosts)

            val forumComments = listOf(
                ForumComment(
                    postId = 1,
                    authorName = "Veronika K.",
                    content = "My sme prešli na Cytopoint pred rokom a je to úľava! Žiadne ranné pchanie tabletiek do šunky a účinok drží cca 5-6 týždňov bez vedľajších účinkov.",
                    dateStr = "Pred 1 dňom"
                ),
                ForumComment(
                    postId = 1,
                    authorName = "Tomas_vet",
                    content = "Cytopoint sú monoklonálne protilátky špecifické na IL-31, nezaťažujú pečeň tak ako imunosupresíva, určite stojí za vyskúšanie.",
                    dateStr = "Pred 20 hodinami"
                ),
                ForumComment(
                    postId = 2,
                    authorName = "Monika & Mimi",
                    content = "U nás zabral vývar z kuracieho mäska (bez soli a korenín) zamrazený do kociek ľadu! Hodím kocku do misky s vodou a kocúr to s radosťou vylíže.",
                    dateStr = "Pred 12 hodinami"
                )
            )
            forumDao.insertComments(forumComments)

            val expenseDao = db.expenseDao()
            expenseDao.insertExpense(
                Expense(
                    petId = 1,
                    title = "Granule Acana Grass-Fed Lamb 11.4kg",
                    category = "Krmivo & Maškrty",
                    amountEur = 68.50,
                    date = "15.10.2026",
                    notes = "Mesačná zásoba hypoalergénnych granúl"
                )
            )
            expenseDao.insertExpense(
                Expense(
                    petId = 1,
                    title = "Bravecto žuvacia tableta 20-40kg",
                    category = "Veterinár & Lieky",
                    amountEur = 34.00,
                    date = "01.09.2026",
                    notes = "Ochrana proti kliešťom na 3 mesiace"
                )
            )
            expenseDao.insertExpense(
                Expense(
                    petId = 1,
                    title = "Ročné kombinované očkovanie DHPPi + L4",
                    category = "Veterinár & Lieky",
                    amountEur = 42.00,
                    date = "14.05.2026",
                    notes = "Klinika VetPoint Bratislava"
                )
            )
            expenseDao.insertExpense(
                Expense(
                    petId = 1,
                    title = "Kúpanie a vyčesávanie podsady (Salón)",
                    category = "Salón & Hygiena",
                    amountEur = 38.00,
                    date = "28.09.2026",
                    notes = "Jesenné presrsťovanie"
                )
            )
            expenseDao.insertExpense(
                Expense(
                    petId = 2,
                    title = "Veterinárna diéta Royal Canin Renal 4kg",
                    category = "Krmivo & Maškrty",
                    amountEur = 39.90,
                    date = "10.10.2026",
                    notes = "Renálna podpora obličiek"
                )
            )
            expenseDao.insertExpense(
                Expense(
                    petId = 2,
                    title = "Sonografia brušnej dutiny a kontrola moču",
                    category = "Veterinár & Lieky",
                    amountEur = 55.00,
                    date = "15.08.2026",
                    notes = "Klinika PrimaVet"
                )
            )

            ensureForumDiscussions(db)
        }

        suspend fun ensureForumDiscussions(db: AppDatabase) {
            val forumDao = db.forumDao()
            val postsCount = forumDao.getPostsCount()
            val commentsCount = forumDao.getCommentsCount()

            if (postsCount == 0) {
                val defaultPosts = listOf(
                    ForumPost(
                        id = 1,
                        authorName = "Peter & Max",
                        petTypeTag = "Liečba & Lieky",
                        title = "Skúsenosti s Apoquelom vs Cytopoint injekciami pri atopii psa",
                        content = "Náš labrador sa na jar a v lete strašne škriabe okolo uší a labiek. Berieme Apoquel 16mg, ktorý zaberá rýchlo, ale vet nám navrhol vyskúšať Cytopoint injekciu raz mesačne. Máte s tým niekto dlhodobejšie skúsenosti? Ako to znášajú?",
                        dateStr = "Pred 2 dňami",
                        likesCount = 14,
                        commentsCount = 3
                    ),
                    ForumPost(
                        id = 2,
                        authorName = "Lucia - Mačací raj",
                        petTypeTag = "Výživa",
                        title = "Ako naučiť mačku piť viac vody pri prevencii močových kameňov?",
                        content = "Dobrý deň, kastrovaný kocúrik mal nález struvitov v moči. Lekár nám odporučil zvýšiť príjem tekutín a močovú diétu. Kúpili sme keramickú fontánku a pridávame teplú vodu do konzervy. Aké máte ďalšie overené triky?",
                        dateStr = "Včera o 14:20",
                        likesCount = 22,
                        commentsCount = 3
                    ),
                    ForumPost(
                        id = 3,
                        authorName = "MVDr. Andrea (Poradňa)",
                        petTypeTag = "Očkovanie",
                        title = "Prečo nestačí registrovať len besnotu? Dôležitosť DHPPi a L4",
                        content = "Mnoho majiteľov si myslí, že ak má pes zo zákona besnotu, je plne chránený. Leptospiróza z kaluží či parvoviróza sú však u mladých aj starších psov mimoriadne nebezpečné. Nezabúdajte na každoročné preočkovanie kombinovanej vakcíny!",
                        dateStr = "Pred 4 dňami",
                        likesCount = 38,
                        commentsCount = 3
                    ),
                    ForumPost(
                        id = 4,
                        authorName = "Zuzana & Blesk",
                        petTypeTag = "Liečba & Lieky",
                        title = "Prvá pomoc: Čo robiť pri podozrení na požitie horkej čokolády alebo hrozna?",
                        content = "Dnes sa nám stala nepríjemná vec – pes stiahol zo stola 70% horkú čokoládu na varenie. Hneď sme volali na pohotovosť, vyvolali zvracanie peroxidom 3% a dali živočíšne uhlie. Zdieľam pre ostatných, teobromín je extrémne nebezpečný!",
                        dateStr = "Dnes o 09:15",
                        likesCount = 45,
                        commentsCount = 2
                    )
                )
                forumDao.insertPosts(defaultPosts)
            }

            if (commentsCount == 0) {
                val defaultComments = listOf(
                    ForumComment(
                        postId = 1,
                        authorName = "Veronika K.",
                        content = "My sme prešli na Cytopoint pred rokom a je to úľava! Žiadne ranné pchanie tabletiek do šunky a účinok drží cca 5-6 týždňov bez vedľajších účinkov.",
                        dateStr = "Pred 1 dňom"
                    ),
                    ForumComment(
                        postId = 1,
                        authorName = "Tomas_vet",
                        content = "Cytopoint sú monoklonálne protilátky špecifické na IL-31, nezaťažujú pečeň tak ako imunosupresíva, určite stojí za vyskúšanie.",
                        dateStr = "Pred 20 hodinami"
                    ),
                    ForumComment(
                        postId = 1,
                        authorName = "MVDr. Horváth",
                        content = "Odporúčam začať injekciou v období najvyššej peľovej záťaže. Pes sa prestane škriabať do 24 hodín a pokožka má čas zregenerovať.",
                        dateStr = "Pred 8 hodinami"
                    ),
                    ForumComment(
                        postId = 2,
                        authorName = "Monika & Mimi",
                        content = "U nás zabral vývar z kuracieho mäska (bez soli a korenín) zamrazený do kociek ľadu! Hodím kocku do misky s vodou a kocúr to s radosťou vylíže.",
                        dateStr = "Pred 12 hodinami"
                    ),
                    ForumComment(
                        postId = 2,
                        authorName = "MVDr. Andrea (Poradňa)",
                        content = "Výborná je aj mokrá strava s pridanou lyžicou vlažnej vody do každej porcie a rozmiestnenie misiek s vodou mimo misky s jedlom.",
                        dateStr = "Pred 6 hodinami"
                    ),
                    ForumComment(
                        postId = 2,
                        authorName = "Katka & Leo",
                        content = "Nám pomohla fontánka s filtrom z aktívneho uhlia, mačky neznášajú stojatú vodu pri stene, chcú čerstvú a tečúcu.",
                        dateStr = "Pred 3 hodinami"
                    ),
                    ForumComment(
                        postId = 3,
                        authorName = "Peter K.",
                        content = "Presne tak, náš známy takto prišiel o ročného nemeckého ovčiaka kvôli leptospiróze, ktorú chytil pri jazere. Očkovanie naozaj nepodceňujte.",
                        dateStr = "Pred 3 dňami"
                    ),
                    ForumComment(
                        postId = 3,
                        authorName = "MVDr. Martin",
                        content = "Navyše Leptospiróza je zoonóza – prenosná aj na ľudí, takže očkovaním chránite seba aj celú svoju rodinu.",
                        dateStr = "Pred 2 dňami"
                    ),
                    ForumComment(
                        postId = 3,
                        authorName = "Lenka & Bella",
                        content = "My dávame každoročne Nobivac DHPPi + L4 a fenka to zvláda bez akejkoľvek reakcie alebo únavy.",
                        dateStr = "Pred 1 dňom"
                    ),
                    ForumComment(
                        postId = 4,
                        authorName = "MVDr. Pohotovosť",
                        content = "Skvelá a rýchla reakcia! Vždy pamätajte: pri vyvolávaní zvracania 3% peroxidom max 1-2 ml na kg a iba ak pes nezjedol ostré predmety a je pri vedomí do 2 hodín od požitia.",
                        dateStr = "Dnes o 10:30"
                    ),
                    ForumComment(
                        postId = 4,
                        authorName = "Andrea M.",
                        content = "Ďakujem za pripomenutie, čokoládu na pečenie hneď odkladám do hornej skrinky mimo dosahu!",
                        dateStr = "Dnes o 11:00"
                    )
                )
                forumDao.insertComments(defaultComments)
            }
        }

        suspend fun ensureIllustrativePhotos(db: AppDatabase) {
            val petDao = db.petDao()
            val photoDao = db.petPhotoDao()
            val pets = petDao.getPetsList()
            for (pet in pets) {
                if (pet.name.equals("Bary", ignoreCase = true) && pet.imageUri.isBlank()) {
                    petDao.updatePet(pet.copy(imageUri = "android.resource://com.example/drawable/img_bary_dog_1793267982723"))
                } else if (pet.name.equals("Luna", ignoreCase = true) && pet.imageUri.isBlank()) {
                    petDao.updatePet(pet.copy(imageUri = "android.resource://com.example/drawable/img_luna_cat_1793268004228"))
                }
            }

            val allPhotos = photoDao.getAllPhotosList()
            for (photo in allPhotos) {
                if (photo.imageUri.isBlank()) {
                    val newUri = when {
                        photo.title.contains("labk", ignoreCase = true) -> "android.resource://com.example/drawable/img_bary_paw_1793268027108"
                        photo.title.contains("ďas", ignoreCase = true) || photo.title.contains("zub", ignoreCase = true) -> "android.resource://com.example/drawable/img_luna_teeth_1793268048085"
                        photo.title.contains("RTG", ignoreCase = true) || photo.title.contains("správa", ignoreCase = true) -> "android.resource://com.example/drawable/vet_medicine_box_icon_1787724951811"
                        photo.petId == 1L -> "android.resource://com.example/drawable/img_bary_dog_1793267982723"
                        photo.petId == 2L -> "android.resource://com.example/drawable/img_luna_cat_1793268004228"
                        else -> "android.resource://com.example/drawable/img_bary_dog_1793267982723"
                    }
                    photoDao.updatePhoto(photo.copy(imageUri = newUri))
                }
            }

            val baryId = pets.find { it.name.equals("Bary", ignoreCase = true) }?.id ?: 1L
            val lunaId = pets.find { it.name.equals("Luna", ignoreCase = true) }?.id ?: 2L
            if (allPhotos.none { it.petId == baryId && it.category == "Portrét" }) {
                photoDao.insertPhoto(
                    PetPhoto(
                        petId = baryId,
                        title = "Portrét Baryho v záhrade",
                        category = "Portrét",
                        date = "01.07.2024",
                        description = "Veselý a zdravý psík, hustá zlatá srsť po prečesaní.",
                        imageUri = "android.resource://com.example/drawable/img_bary_dog_1793267982723",
                        drawableResName = "img_bary_dog_1793267982723"
                    )
                )
            }
            if (allPhotos.none { it.petId == lunaId && it.category == "Portrét" }) {
                photoDao.insertPhoto(
                    PetPhoto(
                        petId = lunaId,
                        title = "Portrét Luny na deke",
                        category = "Portrét",
                        date = "10.08.2024",
                        description = "Pokojná a prítulná mačička po veterinárnej kontrole.",
                        imageUri = "android.resource://com.example/drawable/img_luna_cat_1793268004228",
                        drawableResName = "img_luna_cat_1793268004228"
                    )
                )
            }
        }

        suspend fun ensureVetClinics(db: AppDatabase) {
            val clinicDao = db.vetClinicDao()
            val existing = clinicDao.getAllClinicsList()
            val existingIds = existing.map { it.id }.toSet()
            // Clear legacy pre-seeded sample userRating/userReview on id=1 if user didn't write it
            existing.firstOrNull { it.id == 1L && it.userReview.startsWith("Veľmi ochotný a obetavý") }?.let { legacy ->
                clinicDao.updateClinic(legacy.copy(userRating = 0, userReview = "", rating = 0.0))
            }
            val missing = getDefaultVetClinics().filter { it.id !in existingIds }
            if (missing.isNotEmpty()) {
                clinicDao.insertClinics(missing)
            }
        }

        fun getDefaultVetClinics(): List<VetClinic> {
            return listOf(
                // ==================== BANSKOBYSTRICKÝ KRAJ ====================
                // ZVOLEN
                VetClinic(
                    id = 1L,
                    name = "Veterinárna ambulancia - MVDr. Vojtech Korpás",
                    city = "Zvolen",
                    address = "Dukelských hrdinov 25, 960 01 Zvolen",
                    phone = "+421 905 131 437",
                    email = "korpas@vetzvolen.sk",
                    is24hEmergency = true,
                    openingHours = "NONSTOP (24 hodín pohotovosť / na telefóne)",
                    rating = 0.0,
                    emergencyFeeNote = "Akútna pohotovostná služba, chirurgické zákroky, RTG a hospitalizácia",
                    latitude = 48.5772,
                    longitude = 19.1245,
                    clinicType = "Pohotovosť 24/7"
                ),
                VetClinic(
                    id = 2L,
                    name = "Veterinárna ambulancia ZVOLEN",
                    city = "Zvolen",
                    address = "Kuzmányho nábrežie 322/2, 960 01 Zvolen",
                    phone = "+421 908 422 002",
                    email = "info@veterinarna-ambulancia-zvolen.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:00, So 09:00 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "Komplexná veterinárna starostlivosť, čipovanie, pasy, sono a laboratórium",
                    latitude = 48.5735,
                    longitude = 19.1310,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 3L,
                    name = "ZvolenVet - Veterinárna Klinika",
                    city = "Zvolen",
                    address = "Družstevná 8, 960 01 Zvolen",
                    phone = "+421 918 106 630",
                    email = "recepcia@zvolenvet.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 19:00, So 09:00 - 13:00",
                    rating = 4.8,
                    emergencyFeeNote = "Moderná klinika, inhalačná anestézia, kardiologické vyšetrenia a stomatológia",
                    latitude = 48.5801,
                    longitude = 19.1412,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 4L,
                    name = "Veterinárna poliklinika Sliač & Zvolen",
                    city = "Sliač",
                    address = "Kúpeľná 14, 962 31 Sliač (okres Zvolen)",
                    phone = "+421 911 345 678",
                    email = "sliac@vetpoliklinika.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:00, So 09:00 - 12:00 (Pohotovosť na tel.)",
                    rating = 4.9,
                    emergencyFeeNote = "Pohotovostný výjazd a pohotovosť pre okres Zvolen",
                    latitude = 48.6140,
                    longitude = 19.1430,
                    clinicType = "Veterinárna ambulancia"
                ),
                // BANSKÁ BYSTRICA
                VetClinic(
                    id = 5L,
                    name = "VetClinic Banská Bystrica 24h",
                    city = "Banská Bystrica",
                    address = "Zvolenská cesta 14, 974 05 Banská Bystrica",
                    phone = "+421 48 416 33 22",
                    email = "ambulancia@vetbb.sk",
                    is24hEmergency = true,
                    openingHours = "NONSTOP (Pohotovostný lekár na zavolanie)",
                    rating = 4.8,
                    emergencyFeeNote = "Pohotovostný veterinárny lekár v dosahu 24h",
                    latitude = 48.7212,
                    longitude = 19.1398,
                    clinicType = "Pohotovosť 24/7"
                ),
                VetClinic(
                    id = 6L,
                    name = "Veterinárna ambulancia Sásová",
                    city = "Banská Bystrica",
                    address = "Rudohorská 27, 974 11 Banská Bystrica",
                    phone = "+421 915 223 344",
                    email = "sasova@vetbystrica.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 09:00 - 18:00, So 09:00 - 12:00",
                    rating = 4.9,
                    emergencyFeeNote = "Preventívne prehliadky, strihanie pazúrikov, vakcíny",
                    latitude = 48.7540,
                    longitude = 19.1670,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 7L,
                    name = "Veterinárna ambulancia Fončorda",
                    city = "Banská Bystrica",
                    address = "Kyjevské námestie 5, 974 04 Banská Bystrica",
                    phone = "+421 48 413 77 88",
                    email = "foncorda@vetbb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:30, So 09:00 - 12:00",
                    rating = 4.7,
                    emergencyFeeNote = "Komplexná interná starostlivosť a čistenie zubov",
                    latitude = 48.7300,
                    longitude = 19.1200,
                    clinicType = "Veterinárna ambulancia"
                ),
                // BREZNO, DETVA, KRUPINA, ŽIAR NAD HRONOM, BANSKÁ ŠTIAVNICA, LUČENEC, RIMAVSKÁ SOBOTA, VEĽKÝ KRTÍŠ
                VetClinic(
                    id = 8L,
                    name = "Veterinárna ambulancia Brezno",
                    city = "Brezno",
                    address = "Rázusova 18, 977 01 Brezno",
                    phone = "+421 48 611 25 44",
                    email = "brezno@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:00, So 09:00 - 11:30",
                    rating = 4.7,
                    emergencyFeeNote = "Starostlivosť o spoločenské a hospodárske zvieratá",
                    latitude = 48.8050,
                    longitude = 19.6430,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 9L,
                    name = "Veterinárna ambulancia Detva",
                    city = "Detva",
                    address = "Štúrova 24, 962 12 Detva",
                    phone = "+421 45 545 61 23",
                    email = "detva@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:00",
                    rating = 4.8,
                    emergencyFeeNote = "Všeobecná veterinárna prax, vakcinácie a odčervenie",
                    latitude = 48.5610,
                    longitude = 19.4200,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 10L,
                    name = "Veterinárne stredisko Krupina",
                    city = "Krupina",
                    address = "ČSA 35, 963 01 Krupina",
                    phone = "+421 45 551 12 34",
                    email = "krupina@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 16:30, So 09:00 - 11:00",
                    rating = 4.7,
                    emergencyFeeNote = "Veterinárna prevencia a liečba",
                    latitude = 48.3540,
                    longitude = 19.0660,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 11L,
                    name = "Veterinárna klinika Žiar nad Hronom",
                    city = "Žiar nad Hronom",
                    address = "Ulica SNP 118, 965 01 Žiar nad Hronom",
                    phone = "+421 45 672 44 11",
                    email = "ziar@vetklinika.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:00, So 09:00 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "Diagnostika, chirurgické zákroky, RTG a sono",
                    latitude = 48.5860,
                    longitude = 18.8520,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 12L,
                    name = "Veterinárna ambulancia Banská Štiavnica",
                    city = "Banská Štiavnica",
                    address = "Dolná 15, 969 01 Banská Štiavnica",
                    phone = "+421 45 692 11 88",
                    email = "stiavnica@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 16:30, So na objednanie",
                    rating = 4.8,
                    emergencyFeeNote = "Všeobecná starostlivosť a urgentný výjazd",
                    latitude = 48.4590,
                    longitude = 18.8930,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 13L,
                    name = "Veterinárna nemocnica Lučenec",
                    city = "Lučenec",
                    address = "Vajanského 44, 984 01 Lučenec",
                    phone = "+421 47 433 11 00",
                    email = "lucenec@vetnemocnica.sk",
                    is24hEmergency = true,
                    openingHours = "NONSTOP 24/7 (Pohotovosť na telefóne)",
                    rating = 4.8,
                    emergencyFeeNote = "Hospitalizácia a akútna pohotovostná starostlivosť",
                    latitude = 48.3320,
                    longitude = 19.6640,
                    clinicType = "Pohotovosť 24/7"
                ),
                VetClinic(
                    id = 14L,
                    name = "Veterinárna ambulancia Gemer Rimavská Sobota",
                    city = "Rimavská Sobota",
                    address = "Cukrovarská 12, 979 01 Rimavská Sobota",
                    phone = "+421 47 562 33 22",
                    email = "rimavska@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:30, So 08:30 - 11:30",
                    rating = 4.7,
                    emergencyFeeNote = "Komplexná starostlivosť pre okres Rimavská Sobota",
                    latitude = 48.3830,
                    longitude = 20.0210,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 15L,
                    name = "Veterinárne stredisko Veľký Krtíš",
                    city = "Veľký Krtíš",
                    address = "Nemocničná 8, 990 01 Veľký Krtíš",
                    phone = "+421 47 483 14 55",
                    email = "krtis@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 16:30",
                    rating = 4.6,
                    emergencyFeeNote = "Očkovanie, odčervenie, interná starostlivosť",
                    latitude = 48.2110,
                    longitude = 19.3500,
                    clinicType = "Veterinárna ambulancia"
                ),

                // ==================== BRATISLAVSKÝ KRAJ ====================
                VetClinic(
                    id = 16L,
                    name = "Veterinárna pohotovosť VetPoint Nonstop",
                    city = "Bratislava",
                    address = "Ružinovská 42, 821 03 Bratislava",
                    phone = "+421 2 434 23 333",
                    email = "info@vetpoint.sk",
                    is24hEmergency = true,
                    openingHours = "NONSTOP (24/7 vrátane sviatkov)",
                    rating = 4.9,
                    emergencyFeeNote = "Príjem urgentných pacientov nonstop, RTG, sono, hospitalizácia, chirurgický tím",
                    latitude = 48.1568,
                    longitude = 17.1592,
                    clinicType = "Pohotovosť 24/7",
                    userRating = 5,
                    userReview = "Rýchly zásah pri akútnej otrave v noci, psíka zachránili."
                ),
                VetClinic(
                    id = 17L,
                    name = "Veterinárna Nemocnica Bennett",
                    city = "Bratislava",
                    address = "Kopčianska 65, 851 01 Bratislava - Petržalka",
                    phone = "+421 905 500 000",
                    email = "recepcia@bennett.sk",
                    is24hEmergency = true,
                    openingHours = "NONSTOP 24/7",
                    rating = 4.8,
                    emergencyFeeNote = "Kompletná intenzívna starostlivosť, CT, endoskopia, operačné sály",
                    latitude = 48.1152,
                    longitude = 17.0945,
                    clinicType = "Pohotovosť 24/7"
                ),
                VetClinic(
                    id = 18L,
                    name = "Veterinárna ambulancia Petržalka - MVDr. Kováč",
                    city = "Bratislava",
                    address = "Haanova 12, 851 04 Bratislava",
                    phone = "+421 905 112 233",
                    email = "kovac@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:30, So 09:00 - 12:00",
                    rating = 4.9,
                    emergencyFeeNote = "Bežná ambulancia, očkovania, čipovanie, odbery",
                    latitude = 48.1221,
                    longitude = 17.1190,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 19L,
                    name = "Veterinárna klinika Dúbravka",
                    city = "Bratislava",
                    address = "Saratovská 26, 841 02 Bratislava - Dúbravka",
                    phone = "+421 2 644 11 22",
                    email = "dubravka@vetba.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 19:00, So 09:00 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "Interná medicína, vakcinácie a chirurgia mäkkých tkanív",
                    latitude = 48.1920,
                    longitude = 17.0340,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 20L,
                    name = "Veterinárna ambulancia Pezinok",
                    city = "Pezinok",
                    address = "Moyzesova 18, 902 01 Pezinok",
                    phone = "+421 33 640 12 34",
                    email = "pezinok@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:00, So 09:00 - 12:00",
                    rating = 4.7,
                    emergencyFeeNote = "Prevencia, očkovania a zubný ultrazvuk",
                    latitude = 48.2890,
                    longitude = 17.2680,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 21L,
                    name = "Veterinárna klinika Senec",
                    city = "Senec",
                    address = "Bratislavská 45, 903 01 Senec",
                    phone = "+421 2 459 22 11",
                    email = "senec@vetklinika.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:30, So 09:00 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "Kompletné vyšetrenia, laboratórium a čipovanie",
                    latitude = 48.2190,
                    longitude = 17.3990,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 22L,
                    name = "Veterinárna poliklinika Malacky",
                    city = "Malacky",
                    address = "Záhorácka 31, 901 01 Malacky",
                    phone = "+421 34 772 14 55",
                    email = "malacky@vetpoliklinika.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:00, So 08:30 - 12:00",
                    rating = 4.7,
                    emergencyFeeNote = "Chirurgia, stomatológia a pohotovostný telefón",
                    latitude = 48.4360,
                    longitude = 17.0220,
                    clinicType = "Veterinárna ambulancia"
                ),

                // ==================== TRNAVSKÝ KRAJ ====================
                VetClinic(
                    id = 23L,
                    name = "Veterinárna nemocnica Trnava Nonstop",
                    city = "Trnava",
                    address = "Botanická 22, 917 08 Trnava",
                    phone = "+421 33 550 11 22",
                    email = "trnava@vetnemocnica.sk",
                    is24hEmergency = true,
                    openingHours = "NONSTOP 24/7",
                    rating = 4.8,
                    emergencyFeeNote = "Nonstop pohotovostný tím, RTG, JIS a hospitalizácia",
                    latitude = 48.3710,
                    longitude = 17.5850,
                    clinicType = "Pohotovosť 24/7"
                ),
                VetClinic(
                    id = 24L,
                    name = "Veterinárna klinika Zavarská",
                    city = "Trnava",
                    address = "Zavarská 11, 917 01 Trnava",
                    phone = "+421 918 456 789",
                    email = "trnava@vetzavarska.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 19:00, So 08:30 - 12:00",
                    rating = 4.7,
                    emergencyFeeNote = "Všeobecná veterinárna starostlivosť a chirurgia mäkkých tkanív",
                    latitude = 48.3750,
                    longitude = 17.6025,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 25L,
                    name = "Veterinárna ambulancia Piešťany",
                    city = "Piešťany",
                    address = "Bratislavská 88, 921 01 Piešťany",
                    phone = "+421 33 774 12 34",
                    email = "piestany@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:00, So 09:00 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "Komplexná starostlivosť a laserová terapia",
                    latitude = 48.5860,
                    longitude = 17.8280,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 26L,
                    name = "Veterinárne stredisko Hlohovec",
                    city = "Hlohovec",
                    address = "Nitrianska 12, 920 01 Hlohovec",
                    phone = "+421 33 732 14 55",
                    email = "hlohovec@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:30, So 09:00 - 11:30",
                    rating = 4.7,
                    emergencyFeeNote = "Všeobecná prax a preventívne prehliadky",
                    latitude = 48.4280,
                    longitude = 17.8010,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 27L,
                    name = "Veterinárna ambulancia Senica",
                    city = "Senica",
                    address = "Sotinská 14, 905 01 Senica",
                    phone = "+421 34 651 22 33",
                    email = "senica@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:00, So 08:30 - 11:30",
                    rating = 4.7,
                    emergencyFeeNote = "Chirurgia, sono a laboratórne testy",
                    latitude = 48.6810,
                    longitude = 17.3680,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 28L,
                    name = "Veterinárna klinika Dunajská Streda",
                    city = "Dunajská Streda",
                    address = "Hlavná 52, 929 01 Dunajská Streda",
                    phone = "+421 31 552 44 00",
                    email = "ds@vetklinika.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:00, So 08:30 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "Dvojjazyčná obsluha (SK/HU), RTG, chirurgia",
                    latitude = 47.9940,
                    longitude = 17.6180,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 29L,
                    name = "Veterinárna ambulancia Galanta",
                    city = "Galanta",
                    address = "Kpt. Nálepku 7, 924 01 Galanta",
                    phone = "+421 31 780 15 22",
                    email = "galanta@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:00, So 09:00 - 12:00",
                    rating = 4.7,
                    emergencyFeeNote = "Všeobecná medicína, očkovanie a kastrácie",
                    latitude = 48.1900,
                    longitude = 17.7280,
                    clinicType = "Veterinárna ambulancia"
                ),

                // ==================== TRENČIANSKY KRAJ ====================
                VetClinic(
                    id = 30L,
                    name = "Veterinárna poliklinika Sibírska",
                    city = "Trenčín",
                    address = "Soblahovská 32, 911 01 Trenčín",
                    phone = "+421 32 652 44 11",
                    email = "trencin@vetpoliklinika.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:00, So 09:00 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "Moderná diagnostika, stomatológia, RTG a sono",
                    latitude = 48.8856,
                    longitude = 18.0412,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 31L,
                    name = "Pohotovostná veterinárna služba Trenčín 24/7",
                    city = "Trenčín",
                    address = "M. R. Štefánika 68, 911 01 Trenčín",
                    phone = "+421 905 600 700",
                    email = "pohotovost@vettrencin.sk",
                    is24hEmergency = true,
                    openingHours = "NONSTOP (24/7 pohotovostný príjem)",
                    rating = 4.9,
                    emergencyFeeNote = "Akútna hospitalizácia a nočná pohotovosť",
                    latitude = 48.8920,
                    longitude = 18.0470,
                    clinicType = "Pohotovosť 24/7"
                ),
                VetClinic(
                    id = 32L,
                    name = "Veterinárna klinika Prievidza",
                    city = "Prievidza",
                    address = "Bojnická cesta 39, 971 01 Prievidza",
                    phone = "+421 46 542 33 00",
                    email = "prievidza@vetklinika.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:00, So 09:00 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "Diagnostika, chirurgické zákroky a predaj krmív",
                    latitude = 48.7750,
                    longitude = 18.6050,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 33L,
                    name = "Veterinárna klinika Považská Bystrica",
                    city = "Považská Bystrica",
                    address = "Centrum 16, 017 01 Považská Bystrica",
                    phone = "+421 42 432 14 55",
                    email = "pb@vetklinika.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:00, So 09:00 - 12:00",
                    rating = 4.7,
                    emergencyFeeNote = "RTG, sono, biochemické laboratórium",
                    latitude = 49.1160,
                    longitude = 18.4480,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 34L,
                    name = "Veterinárna ambulancia Púchov",
                    city = "Púchov",
                    address = "Moravská 34, 020 01 Púchov",
                    phone = "+421 42 463 22 11",
                    email = "puchov@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:30, So 09:00 - 11:30",
                    rating = 4.8,
                    emergencyFeeNote = "Očkovanie, prevencia a chirurgia",
                    latitude = 49.1240,
                    longitude = 18.3260,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 35L,
                    name = "Veterinárna ambulancia Partizánske",
                    city = "Partizánske",
                    address = "Generála Svobodu 8, 958 01 Partizánske",
                    phone = "+421 38 749 22 88",
                    email = "partizanske@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:30, So 09:00 - 11:30",
                    rating = 4.7,
                    emergencyFeeNote = "Všeobecná ambulantná prax",
                    latitude = 48.6270,
                    longitude = 18.3750,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 36L,
                    name = "Veterinárna ambulancia Nové Mesto nad Váhom",
                    city = "Nové Mesto nad Váhom",
                    address = "Čsl. armády 18, 915 01 Nové Mesto nad Váhom",
                    phone = "+421 32 771 22 44",
                    email = "nmnv@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:00, So 09:00 - 12:00",
                    rating = 4.7,
                    emergencyFeeNote = "Preventívna a liečebná činnosť",
                    latitude = 48.7560,
                    longitude = 17.8300,
                    clinicType = "Veterinárna ambulancia"
                ),

                // ==================== NITRIANSKY KRAJ ====================
                VetClinic(
                    id = 37L,
                    name = "Veterinárna nemocnica SPU Nitra Nonstop",
                    city = "Nitra",
                    address = "Akademická 2, 949 76 Nitra",
                    phone = "+421 37 641 55 55",
                    email = "nemocnica@spu.sk",
                    is24hEmergency = true,
                    openingHours = "NONSTOP 24/7",
                    rating = 4.9,
                    emergencyFeeNote = "Univerzitné klinické pracovisko, JIS, CT, špecializovaná chirurgia",
                    latitude = 48.3040,
                    longitude = 18.0930,
                    clinicType = "Pohotovosť 24/7"
                ),
                VetClinic(
                    id = 38L,
                    name = "Veterinárne centrum Nitra",
                    city = "Nitra",
                    address = "Štefánikova trieda 70, 949 01 Nitra",
                    phone = "+421 948 123 456",
                    email = "nitra@vetcentrum.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 19:00, So 09:00 - 13:00",
                    rating = 4.6,
                    emergencyFeeNote = "Špecializovaná kardiológia, sono a dermatológia",
                    latitude = 48.3061,
                    longitude = 18.0863,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 39L,
                    name = "Veterinárna ambulancia Nové Zámky",
                    city = "Nové Zámky",
                    address = "Gogoľova 14, 940 01 Nové Zámky",
                    phone = "+421 35 640 11 22",
                    email = "nz@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:00, So 09:00 - 12:00",
                    rating = 4.7,
                    emergencyFeeNote = "Preventívna a liečebná činnosť pre psy a mačky",
                    latitude = 47.9856,
                    longitude = 18.1611,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 40L,
                    name = "Veterinárna klinika Levice",
                    city = "Levice",
                    address = "Kalvínske námestie 4, 934 01 Levice",
                    phone = "+421 36 631 22 88",
                    email = "levice@vetklinika.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:00, So 09:00 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "RTG, sono a operačný sál",
                    latitude = 48.2180,
                    longitude = 18.6080,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 41L,
                    name = "Veterinárna ambulancia Komárno",
                    city = "Komárno",
                    address = "Dunajské nábrežie 16, 945 01 Komárno",
                    phone = "+421 35 770 12 34",
                    email = "komarno@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:00, So 09:00 - 12:00",
                    rating = 4.7,
                    emergencyFeeNote = "Dvojjazyčná starostlivosť, čipovanie a pasy",
                    latitude = 47.7630,
                    longitude = 18.1320,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 42L,
                    name = "Veterinárna klinika Topoľčany",
                    city = "Topoľčany",
                    address = "Stummerova 22, 955 01 Topoľčany",
                    phone = "+421 38 532 11 44",
                    email = "topolcany@vetklinika.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:00, So 09:00 - 12:00",
                    rating = 4.7,
                    emergencyFeeNote = "Chirurgia, stomatológia a vakcinácie",
                    latitude = 48.5600,
                    longitude = 18.1750,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 43L,
                    name = "Veterinárna ambulancia Šaľa",
                    city = "Šaľa",
                    address = "Vlčanská 12, 927 01 Šaľa",
                    phone = "+421 31 770 44 22",
                    email = "sala@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:30, So 09:00 - 11:30",
                    rating = 4.7,
                    emergencyFeeNote = "Všeobecná starostlivosť a rýchle krvné testy",
                    latitude = 48.1520,
                    longitude = 17.8760,
                    clinicType = "Veterinárna ambulancia"
                ),

                // ==================== ŽILINSKÝ KRAJ ====================
                VetClinic(
                    id = 44L,
                    name = "Veterinárna pohotovostná klinika Žilina 24/7",
                    city = "Žilina",
                    address = "Vysokoškolákov 8, 010 08 Žilina",
                    phone = "+421 903 600 700",
                    email = "kontakt@vetza.sk",
                    is24hEmergency = true,
                    openingHours = "NONSTOP (24/7 pohotovosť)",
                    rating = 4.8,
                    emergencyFeeNote = "Akútne zákroky, nočná pohotovosť na telefóne",
                    latitude = 49.2155,
                    longitude = 18.7523,
                    clinicType = "Pohotovosť 24/7"
                ),
                VetClinic(
                    id = 45L,
                    name = "Veterinárna ambulancia Sever Martin",
                    city = "Martin",
                    address = "Jilemnického 22, 036 01 Martin",
                    phone = "+421 43 422 11 00",
                    email = "martin@vetsever.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:30, So 09:00 - 11:30",
                    rating = 4.8,
                    emergencyFeeNote = "Starostlivosť o malé zvieratá, čipovanie, pasy a dietetika",
                    latitude = 49.0664,
                    longitude = 18.9238,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 46L,
                    name = "Veterinárna klinika Kysuce Čadca",
                    city = "Čadca",
                    address = "Májová 28, 022 01 Čadca",
                    phone = "+421 41 433 22 11",
                    email = "cadca@vetklinika.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:00, So 09:00 - 12:00",
                    rating = 4.7,
                    emergencyFeeNote = "Komplexné ošetrenie psov, mačiek a drobných cicavcov",
                    latitude = 49.4380,
                    longitude = 18.7890,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 47L,
                    name = "Veterinárna ambulancia Orava Dolný Kubín",
                    city = "Dolný Kubín",
                    address = "Nemocničná 14, 026 01 Dolný Kubín",
                    phone = "+421 43 586 22 44",
                    email = "kubin@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:30, So 09:00 - 11:30",
                    rating = 4.8,
                    emergencyFeeNote = "Veterinárna starostlivosť pre celú dolnú Oravu",
                    latitude = 49.2080,
                    longitude = 19.2970,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 48L,
                    name = "Veterinárna klinika Ružomberok",
                    city = "Ružomberok",
                    address = "Zarevúca 29, 034 01 Ružomberok",
                    phone = "+421 44 432 15 66",
                    email = "ruzomberok@vetklinika.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:00, So 09:00 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "RTG, sono a chirurgická sála",
                    latitude = 49.0760,
                    longitude = 19.3080,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 49L,
                    name = "Veterinárna ambulancia Liptov Liptovský Mikuláš",
                    city = "Liptovský Mikuláš",
                    address = "Garbiarska 19, 031 01 Liptovský Mikuláš",
                    phone = "+421 908 667 788",
                    email = "liptov@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:30, So 09:00 - 11:30",
                    rating = 4.7,
                    emergencyFeeNote = "Očkovanie, antiparazitárne programy, vyšetrenie uší a očí",
                    latitude = 49.0833,
                    longitude = 19.6167,
                    clinicType = "Veterinárna ambulancia"
                ),

                // ==================== PREŠOVSKÝ KRAJ ====================
                VetClinic(
                    id = 50L,
                    name = "Veterinárna pohotovostná ambulancia Prešov 24/7",
                    city = "Prešov",
                    address = "Solivarská 28, 080 05 Prešov",
                    phone = "+421 905 889 900",
                    email = "pohotovost@vetpresov.sk",
                    is24hEmergency = true,
                    openingHours = "NONSTOP (24/7 pohotovosť na telefóne)",
                    rating = 4.9,
                    emergencyFeeNote = "Akútna hospitalizácia a nočné zásahy",
                    latitude = 48.9820,
                    longitude = 21.2580,
                    clinicType = "Pohotovosť 24/7"
                ),
                VetClinic(
                    id = 51L,
                    name = "Veterinárna poliklinika Prešov",
                    city = "Prešov",
                    address = "Prostějovská 34, 080 01 Prešov - Sídlisko III",
                    phone = "+421 51 771 99 22",
                    email = "presov@vetpoliklinika.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 19:00, So 09:00 - 12:00",
                    rating = 4.7,
                    emergencyFeeNote = "Všeobecná prax, čistenie zubov ultrazvukom, sonografia",
                    latitude = 49.0060,
                    longitude = 21.2220,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 52L,
                    name = "Veterinárna klinika Tatry Poprad",
                    city = "Poprad",
                    address = "Partizánska 88, 058 01 Poprad",
                    phone = "+421 52 772 31 10",
                    email = "tatry@vetpoprad.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:00, So 09:00 - 12:00 (Pohotovosť na tel.)",
                    rating = 4.8,
                    emergencyFeeNote = "Kompletné ošetrenie psov, mačiek a drobných cicavcov",
                    latitude = 49.0543,
                    longitude = 20.2976,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 53L,
                    name = "Veterinárna ambulancia Bardejov",
                    city = "Bardejov",
                    address = "Dlhý rad 16, 085 01 Bardejov",
                    phone = "+421 54 472 18 44",
                    email = "bardejov@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:30, So 08:30 - 11:30",
                    rating = 4.8,
                    emergencyFeeNote = "Všeobecná medicína, odbery krvi, chirurgia",
                    latitude = 49.2940,
                    longitude = 21.2750,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 54L,
                    name = "Veterinárna ambulancia Humenné",
                    city = "Humenné",
                    address = "Kukorelliho 22, 066 01 Humenné",
                    phone = "+421 57 775 33 22",
                    email = "humenne@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:30, So 09:00 - 11:30",
                    rating = 4.7,
                    emergencyFeeNote = "Očkovanie, antiparazitárna ochrana, sono",
                    latitude = 48.9370,
                    longitude = 21.9050,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 55L,
                    name = "Veterinárna ambulancia Vranov nad Topľou",
                    city = "Vranov nad Topľou",
                    address = "Dlhopltecká 14, 093 01 Vranov nad Topľou",
                    phone = "+421 57 442 11 00",
                    email = "vranov@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:00, So 09:00 - 11:00",
                    rating = 4.7,
                    emergencyFeeNote = "Veterinárna starostlivosť a čipovanie",
                    latitude = 48.8890,
                    longitude = 21.6840,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 56L,
                    name = "Veterinárne stredisko Stará Ľubovňa",
                    city = "Stará Ľubovňa",
                    address = "Mierová 42, 064 01 Stará Ľubovňa",
                    phone = "+421 52 432 18 55",
                    email = "lubovna@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 16:30, So 09:00 - 11:00",
                    rating = 4.7,
                    emergencyFeeNote = "Očkovanie, odčervenie, interná starostlivosť",
                    latitude = 49.3010,
                    longitude = 20.6900,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 57L,
                    name = "Veterinárna ambulancia Kežmarok",
                    city = "Kežmarok",
                    address = "Tvarožnianska 9, 060 01 Kežmarok",
                    phone = "+421 52 452 22 11",
                    email = "kezmarok@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:00, So 08:30 - 11:00",
                    rating = 4.7,
                    emergencyFeeNote = "Prevencia a ošetrenie drobných zvierat",
                    latitude = 49.1350,
                    longitude = 20.4320,
                    clinicType = "Veterinárna ambulancia"
                ),

                // ==================== KOŠICKÝ KRAJ ====================
                VetClinic(
                    id = 58L,
                    name = "Univerzitná veterinárna nemocnica UVLF 24h",
                    city = "Košice",
                    address = "Komenského 73, 041 81 Košice",
                    phone = "+421 915 984 689",
                    email = "nemocnica@uvlf.sk",
                    is24hEmergency = true,
                    openingHours = "NONSTOP pohotovosť 24/7",
                    rating = 4.9,
                    emergencyFeeNote = "Špičková klinika s JIS, pohotovostná chirurgická a interná služba",
                    latitude = 48.7389,
                    longitude = 21.2467,
                    clinicType = "Pohotovosť 24/7"
                ),
                VetClinic(
                    id = 59L,
                    name = "Veterinárna ambulancia Terasa Košice",
                    city = "Košice",
                    address = "Trieda SNP 61, 040 11 Košice",
                    phone = "+421 55 643 22 11",
                    email = "terasa@vetke.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:30, So 09:00 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "Praktický veterinár pre sídlisko a okolie, rýchle krvné testy",
                    latitude = 48.7180,
                    longitude = 21.2340,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 60L,
                    name = "Veterinárna klinika Cassovet Košice",
                    city = "Košice",
                    address = "Karpatská 11, 040 01 Košice",
                    phone = "+421 55 632 14 55",
                    email = "cassovet@vetke.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 19:00, So 09:00 - 13:00",
                    rating = 4.8,
                    emergencyFeeNote = "Komplexná chirurgia mäkkých tkanív a ortopédia",
                    latitude = 48.7290,
                    longitude = 21.2580,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 61L,
                    name = "Veterinárne stredisko Zemplín Michalovce",
                    city = "Michalovce",
                    address = "Hollého 58, 071 01 Michalovce",
                    phone = "+421 56 642 18 55",
                    email = "michalovce@vetzemplin.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:30, So 08:30 - 11:30",
                    rating = 4.7,
                    emergencyFeeNote = "Očkovanie, ošetrenie poranení, interná medicína",
                    latitude = 48.7560,
                    longitude = 21.9190,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 62L,
                    name = "Veterinárna klinika Spiš Spišská Nová Ves",
                    city = "Spišská Nová Ves",
                    address = "Duklianska 32, 052 01 Spišská Nová Ves",
                    phone = "+421 53 442 18 99",
                    email = "spis@vetklinika.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:00, So 09:00 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "RTG, sono a operačný sál pre región Spiš",
                    latitude = 48.9430,
                    longitude = 20.5620,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 63L,
                    name = "Veterinárna ambulancia Trebišov",
                    city = "Trebišov",
                    address = "M. R. Štefánika 112, 075 01 Trebišov",
                    phone = "+421 56 672 33 44",
                    email = "trebisov@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:00, So 09:00 - 11:30",
                    rating = 4.7,
                    emergencyFeeNote = "Starostlivosť o malé aj hospodárske zvieratá",
                    latitude = 48.6290,
                    longitude = 21.7210,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 64L,
                    name = "Veterinárna ambulancia Rožňava",
                    city = "Rožňava",
                    address = "Šafárikova 48, 048 01 Rožňava",
                    phone = "+421 58 732 14 22",
                    email = "roznava@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:00, So 08:30 - 11:00",
                    rating = 4.7,
                    emergencyFeeNote = "Všeobecná prax, čipovanie a chirurgia",
                    latitude = 48.6600,
                    longitude = 20.5340,
                    clinicType = "Veterinárna ambulancia"
                ),

                // ==================== ROZŠÍRENÁ SIEŤ AMBULANCIÍ A POHOTOVOSTÍ PO CELOM SLOVENSKU ====================
                // BRATISLAVSKÝ KRAJ (doplnenie mestských častí a okolia)
                VetClinic(
                    id = 65L,
                    name = "Veterinárna klinika Staré Mesto 24h",
                    city = "Bratislava",
                    address = "Šancová 48, 811 05 Bratislava",
                    phone = "+421 2 524 91 222",
                    email = "staremesto@vetba.sk",
                    is24hEmergency = true,
                    openingHours = "NONSTOP (24/7 pohotovosť v centre BA)",
                    rating = 4.8,
                    emergencyFeeNote = "Akútne chirurgické a interné ošetrenie, rýchle sono",
                    latitude = 48.1560,
                    longitude = 17.1140,
                    clinicType = "Pohotovosť 24/7"
                ),
                VetClinic(
                    id = 66L,
                    name = "Veterinárna klinika Nové Mesto & Kramáre",
                    city = "Bratislava",
                    address = "Limbová 14, 831 01 Bratislava",
                    phone = "+421 2 547 72 111",
                    email = "kramare@vetba.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 19:30, So 09:00 - 13:00",
                    rating = 4.9,
                    emergencyFeeNote = "Ortopédia, neurológia, inhalačná anestézia",
                    latitude = 48.1670,
                    longitude = 17.0920,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 67L,
                    name = "Veterinárna ambulancia Karlova Ves",
                    city = "Bratislava",
                    address = "Karloveská 29, 841 04 Bratislava",
                    phone = "+421 903 441 552",
                    email = "karlovaves@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:30, So 09:00 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "Všeobecná prax, čipovanie, pasy a laboratórium",
                    latitude = 48.1540,
                    longitude = 17.0540,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 68L,
                    name = "Veterinárna ambulancia Rača",
                    city = "Bratislava",
                    address = "Detvianska 18, 831 06 Bratislava - Rača",
                    phone = "+421 2 448 72 334",
                    email = "raca@vetba.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:00, So 09:00 - 12:00",
                    rating = 4.7,
                    emergencyFeeNote = "Preventívne prehliadky, vakcinácie a stomatológia",
                    latitude = 48.2090,
                    longitude = 17.1520,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 69L,
                    name = "Veterinárna klinika Podunajské Biskupice & Vrakuňa",
                    city = "Bratislava",
                    address = "Kazanská 42, 821 06 Bratislava",
                    phone = "+421 905 712 889",
                    email = "biskupice@vetba.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 19:00, So 09:00 - 13:00",
                    rating = 4.8,
                    emergencyFeeNote = "RTG, sonografia, ošetrenie psov, mačiek a exotických zvierat",
                    latitude = 48.1330,
                    longitude = 17.2070,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 70L,
                    name = "Veterinárna ambulancia Devínska Nová Ves",
                    city = "Bratislava",
                    address = "Eisnerova 25, 841 07 Bratislava",
                    phone = "+421 2 647 71 223",
                    email = "dnv@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:00, So 09:00 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "Ambulancia pre psy, mačky a drobné hlodavce",
                    latitude = 48.2090,
                    longitude = 16.9760,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 71L,
                    name = "Veterinárna ambulancia Rusovce & Čunovo",
                    city = "Bratislava",
                    address = "Balkánska 102, 851 10 Bratislava - Rusovce",
                    phone = "+421 904 512 310",
                    email = "rusovce@vetba.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 09:00 - 18:00, So 09:00 - 11:30",
                    rating = 4.8,
                    emergencyFeeNote = "Prevencia, očkovanie, čipovanie a interná starostlivosť",
                    latitude = 48.0540,
                    longitude = 17.1480,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 72L,
                    name = "Veterinárna ambulancia Modra",
                    city = "Modra",
                    address = "Štúrova 54, 900 01 Modra",
                    phone = "+421 33 647 21 90",
                    email = "modra@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:30, So 09:00 - 11:30",
                    rating = 4.8,
                    emergencyFeeNote = "Komplexná starostlivosť, chirurgia a čipovanie",
                    latitude = 48.3330,
                    longitude = 17.3070,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 73L,
                    name = "Veterinárna klinika Stupava",
                    city = "Stupava",
                    address = "Hlavná 38, 900 31 Stupava",
                    phone = "+421 2 659 34 221",
                    email = "stupava@vetklinika.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:30, So 09:00 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "Diagnostika, sono, očkovania a výjazdy",
                    latitude = 48.2730,
                    longitude = 17.0310,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 74L,
                    name = "Veterinárna ambulancia Bernolákovo",
                    city = "Bernolákovo",
                    address = "Hlavná 112, 900 27 Bernolákovo",
                    phone = "+421 911 224 890",
                    email = "bernolakovo@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:00, So 09:00 - 11:30",
                    rating = 4.7,
                    emergencyFeeNote = "Ošetrenie drobných zvierat, kastrácie a odbery",
                    latitude = 48.2000,
                    longitude = 17.3000,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 75L,
                    name = "Veterinárne stredisko Svätý Jur",
                    city = "Svätý Jur",
                    address = "Prostredná 29, 900 21 Svätý Jur",
                    phone = "+421 2 449 71 500",
                    email = "svatyjur@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 09:00 - 17:30, So na objednanie",
                    rating = 4.7,
                    emergencyFeeNote = "Preventívna starostlivosť, ultrazvukové čistenie zubov",
                    latitude = 48.2520,
                    longitude = 17.2140,
                    clinicType = "Veterinárna ambulancia"
                ),

                // TRNAVSKÝ KRAJ (doplnenie)
                VetClinic(
                    id = 76L,
                    name = "Veterinárna ambulancia Družba Trnava",
                    city = "Trnava",
                    address = "J. G. Tajovského 18, 917 01 Trnava",
                    phone = "+421 33 551 44 88",
                    email = "druzba@vettrnava.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:00, So 09:00 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "Očkovania, odčervenia, interná medicína a kožné testy",
                    latitude = 48.3820,
                    longitude = 17.5960,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 77L,
                    name = "Veterinárna pohotovosť Piešťany 24/7",
                    city = "Piešťany",
                    address = "Krajinská cesta 36, 921 01 Piešťany",
                    phone = "+421 905 411 299",
                    email = "pohotovost@vetpiestany.sk",
                    is24hEmergency = true,
                    openingHours = "NONSTOP (Pohotovostný príjem 24/7)",
                    rating = 4.8,
                    emergencyFeeNote = "Akútne zásahy a hospitalizácia pre Piešťany a okolie",
                    latitude = 48.5910,
                    longitude = 17.8190,
                    clinicType = "Pohotovosť 24/7"
                ),
                VetClinic(
                    id = 78L,
                    name = "Veterinárna klinika Skalica",
                    city = "Skalica",
                    address = "Mallého 17, 909 01 Skalica",
                    phone = "+421 34 664 21 33",
                    email = "skalica@vetklinika.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:00, So 09:00 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "RTG, sono, chirurgia a stomatológia pre Záhorie",
                    latitude = 48.8440,
                    longitude = 17.2260,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 79L,
                    name = "Veterinárna ambulancia Holíč",
                    city = "Holíč",
                    address = "Bratislavská 21, 908 51 Holíč",
                    phone = "+421 34 668 32 10",
                    email = "holic@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:00, So 08:30 - 11:00",
                    rating = 4.7,
                    emergencyFeeNote = "Všeobecná prax, vakcinácie a pasy",
                    latitude = 48.8090,
                    longitude = 17.1610,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 80L,
                    name = "Veterinárna klinika Šamorín",
                    city = "Šamorín",
                    address = "Bratislavská 74, 931 01 Šamorín",
                    phone = "+421 31 562 44 77",
                    email = "samorin@vetklinika.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:30, So 09:00 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "Starostlivosť o spoločenské zvieratá aj kone (x-ray, sono)",
                    latitude = 48.0280,
                    longitude = 17.3090,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 81L,
                    name = "Veterinárna ambulancia Sereď",
                    city = "Sereď",
                    address = "Námestie slobody 14, 926 01 Sereď",
                    phone = "+421 31 789 22 15",
                    email = "sered@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:30, So 09:00 - 11:30",
                    rating = 4.7,
                    emergencyFeeNote = "Prevencia, kastrácie, diagnostika kožných ochorení",
                    latitude = 48.2880,
                    longitude = 17.7370,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 82L,
                    name = "Veterinárna ambulancia Veľký Meder",
                    city = "Veľký Meder",
                    address = "Komárňanská 28, 932 01 Veľký Meder",
                    phone = "+421 31 555 21 90",
                    email = "meder@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:00, So 09:00 - 11:30",
                    rating = 4.7,
                    emergencyFeeNote = "Dvojjazyčná ambulancia pre psy, mačky a hospodárske zvieratá",
                    latitude = 47.8560,
                    longitude = 17.7720,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 83L,
                    name = "Veterinárne stredisko Vrbové",
                    city = "Vrbové",
                    address = "Beňovského 19, 922 03 Vrbové",
                    phone = "+421 33 779 21 44",
                    email = "vrbove@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 16:30",
                    rating = 4.7,
                    emergencyFeeNote = "Očkovania, čipovanie, ošetrenie rán",
                    latitude = 48.6200,
                    longitude = 17.7230,
                    clinicType = "Veterinárna ambulancia"
                ),

                // TRENČIANSKY KRAJ (doplnenie)
                VetClinic(
                    id = 84L,
                    name = "Veterinárna klinika Bánovce nad Bebravou",
                    city = "Bánovce nad Bebravou",
                    address = "Svätoplukova 15, 957 01 Bánovce nad Bebravou",
                    phone = "+421 38 760 22 44",
                    email = "banovce@vetklinika.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:00, So 09:00 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "RTG, sono, chirurgia a biochemické vyšetrenie krvi",
                    latitude = 48.7210,
                    longitude = 18.2570,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 85L,
                    name = "Veterinárna ambulancia Myjava",
                    city = "Myjava",
                    address = "Námestie M. R. Štefánika 32, 907 01 Myjava",
                    phone = "+421 34 621 33 11",
                    email = "myjava@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:30, So 09:00 - 11:30",
                    rating = 4.7,
                    emergencyFeeNote = "Starostlivosť o spoločenské zvieratá pre Kopanice",
                    latitude = 48.7530,
                    longitude = 17.5680,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 86L,
                    name = "Veterinárna ambulancia Dubnica nad Váhom",
                    city = "Dubnica nad Váhom",
                    address = "Pod hájom 104, 018 41 Dubnica nad Váhom",
                    phone = "+421 42 442 55 66",
                    email = "dubnica@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:00, So 09:00 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "Komplexné vyšetrenia, sono a stomatológia",
                    latitude = 48.9600,
                    longitude = 18.1740,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 87L,
                    name = "Veterinárne stredisko Ilava",
                    city = "Ilava",
                    address = "Mierové námestie 24, 019 01 Ilava",
                    phone = "+421 42 446 51 22",
                    email = "ilava@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:00, So 08:30 - 11:00",
                    rating = 4.7,
                    emergencyFeeNote = "Očkovania, čipovanie a bežné zákroky",
                    latitude = 48.9970,
                    longitude = 18.2340,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 88L,
                    name = "Veterinárna ambulancia Nová Dubnica",
                    city = "Nová Dubnica",
                    address = "Trenčianska 45, 018 51 Nová Dubnica",
                    phone = "+421 42 443 18 99",
                    email = "novadubnica@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:30",
                    rating = 4.8,
                    emergencyFeeNote = "Interná medicína a kožná diagnostika",
                    latitude = 48.9350,
                    longitude = 18.1440,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 89L,
                    name = "Veterinárna klinika Bojnice",
                    city = "Bojnice",
                    address = "Sládkovičova 16, 972 01 Bojnice",
                    phone = "+421 46 543 21 88",
                    email = "bojnice@vetklinika.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:30, So 09:00 - 12:00",
                    rating = 4.9,
                    emergencyFeeNote = "Chirurgia, ortopédia, kardiológia a rehabilitácia",
                    latitude = 48.7810,
                    longitude = 18.5840,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 90L,
                    name = "Veterinárna ambulancia Handlová",
                    city = "Handlová",
                    address = "Námestie baníkov 9, 972 51 Handlová",
                    phone = "+421 46 547 12 34",
                    email = "handlova@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:00, So 09:00 - 11:00",
                    rating = 4.7,
                    emergencyFeeNote = "Očkovanie, odčervenie a akútne ošetrenie rán",
                    latitude = 48.7270,
                    longitude = 18.7600,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 91L,
                    name = "Veterinárna ambulancia Stará Turá",
                    city = "Stará Turá",
                    address = "SNP 122, 916 01 Stará Turá",
                    phone = "+421 32 776 31 12",
                    email = "staratura@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:00",
                    rating = 4.7,
                    emergencyFeeNote = "Všeobecná prax pre malé zvieratá",
                    latitude = 48.7770,
                    longitude = 17.6940,
                    clinicType = "Veterinárna ambulancia"
                ),

                // NITRIANSKY KRAJ (doplnenie)
                VetClinic(
                    id = 92L,
                    name = "Veterinárna pohotovosť Nitra Klokočina 24/7",
                    city = "Nitra",
                    address = "Hviezdoslavova trieda 12, 949 11 Nitra",
                    phone = "+421 905 334 455",
                    email = "klokocina@vetnitra.sk",
                    is24hEmergency = true,
                    openingHours = "NONSTOP (Pohotovosť 24/7)",
                    rating = 4.8,
                    emergencyFeeNote = "Rýchla nočná a víkendová pomoc, RTG a infúzna terapia",
                    latitude = 48.3010,
                    longitude = 18.0610,
                    clinicType = "Pohotovosť 24/7"
                ),
                VetClinic(
                    id = 93L,
                    name = "Veterinárna ambulancia Chrenová Nitra",
                    city = "Nitra",
                    address = "Trieda Andreja Hlinku 28, 949 01 Nitra",
                    phone = "+421 37 652 14 22",
                    email = "chrenova@vetnitra.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:30, So 09:00 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "Prevencia, očkovanie, ultrazvuk zubov",
                    latitude = 48.3080,
                    longitude = 18.1010,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 94L,
                    name = "Veterinárna klinika Zlaté Moravce",
                    city = "Zlaté Moravce",
                    address = "Župná 18, 953 01 Zlaté Moravce",
                    phone = "+421 37 642 18 90",
                    email = "zm@vetklinika.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:00, So 09:00 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "Chirurgia, sono a komplexné vyšetrenie krvi",
                    latitude = 48.3850,
                    longitude = 18.4000,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 95L,
                    name = "Veterinárna ambulancia Štúrovo",
                    city = "Štúrovo",
                    address = "Hlavná 33, 943 01 Štúrovo",
                    phone = "+421 36 751 22 44",
                    email = "sturovo@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:30, So 09:00 - 11:30",
                    rating = 4.7,
                    emergencyFeeNote = "Dvojjazyčná obsluha (SK/HU), pasy a cestovné certifikáty",
                    latitude = 47.7980,
                    longitude = 18.7180,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 96L,
                    name = "Veterinárna ambulancia Šurany",
                    city = "Šurany",
                    address = "SNP 12, 942 01 Šurany",
                    phone = "+421 35 650 14 22",
                    email = "surany@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:30, So 09:00 - 11:30",
                    rating = 4.7,
                    emergencyFeeNote = "Všeobecná veterinárna starostlivosť",
                    latitude = 48.0860,
                    longitude = 18.1840,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 97L,
                    name = "Veterinárna ambulancia Vráble",
                    city = "Vráble",
                    address = "Levická 18, 952 01 Vráble",
                    phone = "+421 37 783 22 10",
                    email = "vrable@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:30, So 09:00 - 11:30",
                    rating = 4.7,
                    emergencyFeeNote = "Očkovanie, čipovanie, ošetrenie drobných zvierat",
                    latitude = 48.2430,
                    longitude = 18.3080,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 98L,
                    name = "Veterinárne stredisko Kolárovo",
                    city = "Kolárovo",
                    address = "Hlavná 24, 946 03 Kolárovo",
                    phone = "+421 35 777 12 40",
                    email = "kolarovo@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:00",
                    rating = 4.7,
                    emergencyFeeNote = "Prevencia, kastrácie a odčervenie",
                    latitude = 47.9230,
                    longitude = 17.9970,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 99L,
                    name = "Veterinárna ambulancia Hurbanovo",
                    city = "Hurbanovo",
                    address = "Komárňanská 66, 947 01 Hurbanovo",
                    phone = "+421 35 760 22 11",
                    email = "hurbanovo@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:00, So 09:00 - 11:00",
                    rating = 4.6,
                    emergencyFeeNote = "Bežné veterinárne zákroky a vakcinácie",
                    latitude = 47.8750,
                    longitude = 18.1930,
                    clinicType = "Veterinárna ambulancia"
                ),

                // ŽILINSKÝ KRAJ (doplnenie)
                VetClinic(
                    id = 100L,
                    name = "Veterinárna ambulancia Vlčince Žilina",
                    city = "Žilina",
                    address = "Obchodná 9, 010 08 Žilina",
                    phone = "+421 41 565 22 11",
                    email = "vlcince@vetza.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:30, So 09:00 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "Rýchle vyšetrenia pre sídliská Vlčince, Solinky a Hliny",
                    latitude = 49.2080,
                    longitude = 18.7650,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 101L,
                    name = "Veterinárna pohotovosť Martin 24/7",
                    city = "Martin",
                    address = "Československej armády 16, 036 01 Martin",
                    phone = "+421 905 788 123",
                    email = "pohotovost@vetmartin.sk",
                    is24hEmergency = true,
                    openingHours = "NONSTOP (24/7 na telefóne pre Turiec)",
                    rating = 4.9,
                    emergencyFeeNote = "Akútne zásahy, torzia žalúdka, cisárske rezy a JIS",
                    latitude = 49.0630,
                    longitude = 18.9180,
                    clinicType = "Pohotovosť 24/7"
                ),
                VetClinic(
                    id = 102L,
                    name = "Veterinárna klinika Námestovo",
                    city = "Námestovo",
                    address = "Hviezdoslavova 34, 029 01 Námestovo",
                    phone = "+421 43 552 14 55",
                    email = "namestovo@vetklinika.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:00, So 09:00 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "Komplexná starostlivosť pre hornú Oravu, RTG a sono",
                    latitude = 49.4070,
                    longitude = 19.4800,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 103L,
                    name = "Veterinárna ambulancia Trstená",
                    city = "Trstená",
                    address = "Železničiarov 28, 028 01 Trstená",
                    phone = "+421 43 539 12 34",
                    email = "trstena@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:00, So 09:00 - 11:30",
                    rating = 4.7,
                    emergencyFeeNote = "Očkovanie, chirurgia, ošetrenie malých aj veľkých zvierat",
                    latitude = 49.3610,
                    longitude = 19.6120,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 104L,
                    name = "Veterinárna ambulancia Tvrdošín",
                    city = "Tvrdošín",
                    address = "Medvedzie 142, 027 44 Tvrdošín",
                    phone = "+421 43 532 22 11",
                    email = "tvrdosin@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:30",
                    rating = 4.7,
                    emergencyFeeNote = "Preventívna medicína, čipovanie a ultrazvuk",
                    latitude = 49.3350,
                    longitude = 19.5550,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 105L,
                    name = "Veterinárna ambulancia Kysucké Nové Mesto",
                    city = "Kysucké Nové Mesto",
                    address = "Belanského 18, 024 01 Kysucké Nové Mesto",
                    phone = "+421 41 421 33 22",
                    email = "knm@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:00, So 09:00 - 11:30",
                    rating = 4.8,
                    emergencyFeeNote = "Chirurgia, čistenie zubov a odbery krvi",
                    latitude = 49.3020,
                    longitude = 18.7860,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 106L,
                    name = "Veterinárna ambulancia Bytča",
                    city = "Bytča",
                    address = "Námestie SR 14, 014 01 Bytča",
                    phone = "+421 41 553 21 88",
                    email = "bytca@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:30, So 09:00 - 11:30",
                    rating = 4.7,
                    emergencyFeeNote = "Všeobecná prax pre psy, mačky a králiky",
                    latitude = 49.2220,
                    longitude = 18.5590,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 107L,
                    name = "Veterinárne stredisko Turčianske Teplice",
                    city = "Turčianske Teplice",
                    address = "Partizánska 22, 039 01 Turčianske Teplice",
                    phone = "+421 43 492 22 44",
                    email = "teplice@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:00, So 09:00 - 11:00",
                    rating = 4.7,
                    emergencyFeeNote = "Očkovania, odčervenia a interné vyšetrenia",
                    latitude = 48.8620,
                    longitude = 18.8600,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 108L,
                    name = "Veterinárna ambulancia Liptovský Hrádok",
                    city = "Liptovský Hrádok",
                    address = "Hviezdoslavova 11, 033 01 Liptovský Hrádok",
                    phone = "+421 44 522 14 55",
                    email = "hradok@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:00",
                    rating = 4.8,
                    emergencyFeeNote = "Starostlivosť o spoločenské zvieratá pre horný Liptov",
                    latitude = 49.0380,
                    longitude = 19.7240,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 109L,
                    name = "Veterinárna ambulancia Vrútky",
                    city = "Vrútky",
                    address = "1. čsl. brigády 16, 038 61 Vrútky",
                    phone = "+421 43 428 11 22",
                    email = "vrutky@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:30, So 09:00 - 11:30",
                    rating = 4.7,
                    emergencyFeeNote = "Preventívne vyšetrenia a drobné zákroky",
                    latitude = 49.1130,
                    longitude = 18.9220,
                    clinicType = "Veterinárna ambulancia"
                ),

                // BANSKOBYSTRICKÝ KRAJ (doplnenie)
                VetClinic(
                    id = 110L,
                    name = "Veterinárna ambulancia Sekier Zvolen",
                    city = "Zvolen",
                    address = "Okružná 142, 960 01 Zvolen - Sekier",
                    phone = "+421 907 321 654",
                    email = "sekier@vetzvolen.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:00, So 09:00 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "Rýchle vyšetrenia, očkovania, poradenstvo v diéte",
                    latitude = 48.5670,
                    longitude = 19.1620,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 111L,
                    name = "Veterinárna klinika Radvaň Banská Bystrica",
                    city = "Banská Bystrica",
                    address = "Sládkovičova 32, 974 05 Banská Bystrica",
                    phone = "+421 48 416 77 99",
                    email = "radvan@vetbb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 19:00, So 09:00 - 13:00",
                    rating = 4.8,
                    emergencyFeeNote = "RTG, ultrazvuk, chirurgia a kardiologické vyšetrenie",
                    latitude = 48.7160,
                    longitude = 19.1310,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 112L,
                    name = "Veterinárna ambulancia Podlavice Banská Bystrica",
                    city = "Banská Bystrica",
                    address = "Gaštanová 18, 974 09 Banská Bystrica",
                    phone = "+421 911 456 221",
                    email = "podlavice@vetbb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 09:00 - 18:00, So 09:00 - 11:30",
                    rating = 4.8,
                    emergencyFeeNote = "Priateľská ambulancia pre psy, mačky a králiky",
                    latitude = 48.7380,
                    longitude = 19.1040,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 113L,
                    name = "Veterinárna ambulancia Revúca",
                    city = "Revúca",
                    address = "Muránska 12, 050 01 Revúca",
                    phone = "+421 58 442 18 33",
                    email = "revuca@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:00, So 09:00 - 11:00",
                    rating = 4.7,
                    emergencyFeeNote = "Všeobecná veterinárna prax pre okres Revúca",
                    latitude = 48.6830,
                    longitude = 20.1140,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 114L,
                    name = "Veterinárne stredisko Poltár",
                    city = "Poltár",
                    address = "Sklárska 14, 987 01 Poltár",
                    phone = "+421 47 422 21 14",
                    email = "poltar@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 16:30",
                    rating = 4.7,
                    emergencyFeeNote = "Očkovania, čipovanie, ošetrenie rán",
                    latitude = 48.4310,
                    longitude = 19.7940,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 115L,
                    name = "Veterinárna ambulancia Žarnovica",
                    city = "Žarnovica",
                    address = "Bystrická 34, 966 81 Žarnovica",
                    phone = "+421 45 681 22 11",
                    email = "zarnovica@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:00, So 09:00 - 11:00",
                    rating = 4.7,
                    emergencyFeeNote = "Starostlivosť o spoločenské zvieratá a výjazdy",
                    latitude = 48.4810,
                    longitude = 18.7160,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 116L,
                    name = "Veterinárna ambulancia Nová Baňa",
                    city = "Nová Baňa",
                    address = "Kollárova 8, 968 01 Nová Baňa",
                    phone = "+421 45 685 21 90",
                    email = "novabana@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:00",
                    rating = 4.7,
                    emergencyFeeNote = "Prevencia, kastrácie a laboratórne testy",
                    latitude = 48.4230,
                    longitude = 18.6390,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 117L,
                    name = "Veterinárna ambulancia Hriňová",
                    city = "Hriňová",
                    address = "Lúčna 19, 962 05 Hriňová",
                    phone = "+421 45 549 71 22",
                    email = "hrinova@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 16:30, So 09:00 - 11:00",
                    rating = 4.8,
                    emergencyFeeNote = "Ošetrenie drobných aj hospodárskych zvierat na Podpoľaní",
                    latitude = 48.5760,
                    longitude = 19.5280,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 118L,
                    name = "Veterinárna ambulancia Fiľakovo",
                    city = "Fiľakovo",
                    address = "Biskupická 16, 986 01 Fiľakovo",
                    phone = "+421 47 438 14 55",
                    email = "filakovo@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:00",
                    rating = 4.7,
                    emergencyFeeNote = "Dvojjazyčná prax (SK/HU), vakcinácie a pasy",
                    latitude = 48.2710,
                    longitude = 19.8260,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 119L,
                    name = "Veterinárna ambulancia Tornaľa",
                    city = "Tornaľa",
                    address = "Mierová 21, 982 01 Tornaľa",
                    phone = "+421 47 552 14 22",
                    email = "tornala@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 16:30",
                    rating = 4.6,
                    emergencyFeeNote = "Preventívna starostlivosť a ošetrenie úrazov",
                    latitude = 48.4230,
                    longitude = 20.3260,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 120L,
                    name = "Veterinárna ambulancia Kremnica",
                    city = "Kremnica",
                    address = "Kutnohorská 14, 967 01 Kremnica",
                    phone = "+421 45 674 22 11",
                    email = "kremnica@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:00",
                    rating = 4.8,
                    emergencyFeeNote = "Komplexná starostlivosť pre psy, mačky a drobné zvieratá",
                    latitude = 48.7050,
                    longitude = 18.9180,
                    clinicType = "Veterinárna ambulancia"
                ),

                // PREŠOVSKÝ KRAJ (doplnenie)
                VetClinic(
                    id = 121L,
                    name = "Veterinárna pohotovosť Vysoké Tatry & Poprad 24/7",
                    city = "Poprad",
                    address = "Hraničná 16, 058 01 Poprad",
                    phone = "+421 905 712 400",
                    email = "pohotovost@vetpoprad.sk",
                    is24hEmergency = true,
                    openingHours = "NONSTOP (24/7 pohotovosť pod Tatrami)",
                    rating = 4.9,
                    emergencyFeeNote = "Nonstop príjem, chirurgia, RTG, hospitalizácia",
                    latitude = 49.0610,
                    longitude = 20.3050,
                    clinicType = "Pohotovosť 24/7"
                ),
                VetClinic(
                    id = 122L,
                    name = "Veterinárna ambulancia Sekčov Prešov",
                    city = "Prešov",
                    address = "Exnárova 22, 080 01 Prešov",
                    phone = "+421 51 770 44 22",
                    email = "sekcov@vetpresov.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:30, So 09:00 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "Krvné rozbory na počkanie, ultrazvuk a čipovanie",
                    latitude = 48.9850,
                    longitude = 21.2690,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 123L,
                    name = "Veterinárna ambulancia Snina",
                    city = "Snina",
                    address = "Strojárska 42, 069 01 Snina",
                    phone = "+421 57 762 14 55",
                    email = "snina@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:00, So 09:00 - 11:30",
                    rating = 4.7,
                    emergencyFeeNote = "Starostlivosť o spoločenské zvieratá pre región Poloniny",
                    latitude = 48.9880,
                    longitude = 22.1550,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 124L,
                    name = "Veterinárna ambulancia Svidník",
                    city = "Svidník",
                    address = "Sovietskych hrdinov 28, 089 01 Svidník",
                    phone = "+421 54 752 22 33",
                    email = "svidnik@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:00, So 09:00 - 11:00",
                    rating = 4.7,
                    emergencyFeeNote = "Očkovania, kastrácie a chirurgia mäkkých tkanív",
                    latitude = 49.3070,
                    longitude = 21.5680,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 125L,
                    name = "Veterinárna ambulancia Stropkov",
                    city = "Stropkov",
                    address = "Hlavná 52, 091 01 Stropkov",
                    phone = "+421 54 742 18 90",
                    email = "stropkov@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 16:30",
                    rating = 4.7,
                    emergencyFeeNote = "Preventívne prehliadky a bežné ošetrenia",
                    latitude = 49.2050,
                    longitude = 21.6520,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 126L,
                    name = "Veterinárna klinika Sabinov",
                    city = "Sabinov",
                    address = "Námestie slobody 31, 083 01 Sabinov",
                    phone = "+421 51 452 21 88",
                    email = "sabinov@vetklinika.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:00, So 09:00 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "Diagnostika, RTG, sono a interná medicína",
                    latitude = 49.1030,
                    longitude = 21.0980,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 127L,
                    name = "Veterinárna ambulancia Levoča",
                    city = "Levoča",
                    address = "Košická 16, 054 01 Levoča",
                    phone = "+421 53 451 22 44",
                    email = "levoca@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:30, So 09:00 - 11:30",
                    rating = 4.8,
                    emergencyFeeNote = "Komplexné ošetrenie psov, mačiek a hospodárskych zvierat",
                    latitude = 49.0260,
                    longitude = 20.5890,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 128L,
                    name = "Veterinárne stredisko Medzilaborce",
                    city = "Medzilaborce",
                    address = "Mierová 14, 068 01 Medzilaborce",
                    phone = "+421 57 732 14 55",
                    email = "medzilaborce@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 16:30",
                    rating = 4.7,
                    emergencyFeeNote = "Očkovania, odčervenia a chirurgická prax",
                    latitude = 49.2720,
                    longitude = 21.9030,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 129L,
                    name = "Veterinárna ambulancia Svit",
                    city = "Svit",
                    address = "Štúrova 18, 059 21 Svit",
                    phone = "+421 52 775 21 90",
                    email = "svit@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:30, So 09:00 - 11:30",
                    rating = 4.8,
                    emergencyFeeNote = "Starostlivosť o spoločenské zvieratá, stomatológia",
                    latitude = 49.0590,
                    longitude = 20.2070,
                    clinicType = "Veterinárna ambulancia"
                ),

                // KOŠICKÝ KRAJ (doplnenie)
                VetClinic(
                    id = 130L,
                    name = "Veterinárna ambulancia Nad jazerom Košice",
                    city = "Košice",
                    address = "Ulica meteorová 10, 040 12 Košice",
                    phone = "+421 55 674 22 11",
                    email = "jazero@vetke.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:30, So 09:00 - 12:00",
                    rating = 4.8,
                    emergencyFeeNote = "Očkovanie, dermatológia, sono a kastrácie",
                    latitude = 48.6900,
                    longitude = 21.2810,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 131L,
                    name = "Veterinárna klinika Furča & Ťahanovce Košice",
                    city = "Košice",
                    address = "Tokajícka 14, 040 13 Košice",
                    phone = "+421 55 636 44 55",
                    email = "tahanovce@vetke.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 19:00, So 09:00 - 12:30",
                    rating = 4.8,
                    emergencyFeeNote = "Komplexné laboratórium, RTG a stomatológia",
                    latitude = 48.7450,
                    longitude = 21.2690,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 132L,
                    name = "Veterinárna pohotovosť Zemplín Michalovce 24/7",
                    city = "Michalovce",
                    address = "Humenská 24, 071 01 Michalovce",
                    phone = "+421 905 642 999",
                    email = "pohotovost@vetmichalovce.sk",
                    is24hEmergency = true,
                    openingHours = "NONSTOP (24/7 pohotovosť pre Zemplín)",
                    rating = 4.9,
                    emergencyFeeNote = "Akútne operácie, infúzna terapia a urgentné úrazy",
                    latitude = 48.7630,
                    longitude = 21.9150,
                    clinicType = "Pohotovosť 24/7"
                ),
                VetClinic(
                    id = 133L,
                    name = "Veterinárna ambulancia Moldava nad Bodvou",
                    city = "Moldava nad Bodvou",
                    address = "Hlavná 58, 045 01 Moldava nad Bodvou",
                    phone = "+421 55 460 22 11",
                    email = "moldava@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:00, So 09:00 - 11:30",
                    rating = 4.7,
                    emergencyFeeNote = "Dvojjazyčná prax (SK/HU), pasy a prevencia",
                    latitude = 48.6130,
                    longitude = 21.0000,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 134L,
                    name = "Veterinárna ambulancia Gelnica",
                    city = "Gelnica",
                    address = "Slovenská 24, 056 01 Gelnica",
                    phone = "+421 53 482 14 55",
                    email = "gelnica@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 16:30",
                    rating = 4.7,
                    emergencyFeeNote = "Všeobecná ambulancia pre Hnileckú dolinu",
                    latitude = 48.8550,
                    longitude = 20.9370,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 135L,
                    name = "Veterinárna ambulancia Sobrance",
                    city = "Sobrance",
                    address = "Štefánikova 18, 073 01 Sobrance",
                    phone = "+421 56 652 21 33",
                    email = "sobrance@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 16:30",
                    rating = 4.7,
                    emergencyFeeNote = "Očkovanie, čipovanie a interná medicína",
                    latitude = 48.7450,
                    longitude = 22.1810,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 136L,
                    name = "Veterinárna ambulancia Krompachy",
                    city = "Krompachy",
                    address = "Hlavná 38, 053 42 Krompachy",
                    phone = "+421 53 447 22 11",
                    email = "krompachy@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:00, So 09:00 - 11:00",
                    rating = 4.7,
                    emergencyFeeNote = "Všeobecná starostlivosť o psy a mačky",
                    latitude = 48.9160,
                    longitude = 20.8750,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 137L,
                    name = "Veterinárne stredisko Kráľovský Chlmec",
                    city = "Kráľovský Chlmec",
                    address = "Hlavná 82, 077 01 Kráľovský Chlmec",
                    phone = "+421 56 632 14 22",
                    email = "chlmec@vetamb.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:00",
                    rating = 0.0,
                    emergencyFeeNote = "Starostlivosť pre Medzibodrožie (SK/HU)",
                    latitude = 48.4230,
                    longitude = 21.9780,
                    clinicType = "Veterinárna ambulancia"
                ),

                // DOPLNENIE REGIONÁLNYCH AMBULANCIÍ (LIESKOVET / LIESKOVEC, POHRONIE, POVAŽIE, ZÁPAD & SEVER SR)
                VetClinic(
                    id = 138L,
                    name = "Veterinárna ambulancia Lieskovet (MVDr. Dušan Gogola)",
                    city = "Lieskovec (pri Zvolene)",
                    address = "Horná 47, 962 21 Lieskovec / T. G. Masaryka 1119/6, Zvolen",
                    phone = "+421 905 279 290",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia podľa ordinačných hodín / telefonicky",
                    rating = 0.0,
                    emergencyFeeNote = "Psy, mačky, drobné, exotické aj hospodárske zvieratá (Lieskovec & okr. Zvolen)",
                    latitude = 48.5752,
                    longitude = 19.1932,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 139L,
                    name = "A-Vet Veterinárna ambulancia (MVDr. Adriana Dospělová)",
                    city = "Zvolen",
                    address = "Okružná 135 (Môťová), 960 01 Zvolen",
                    phone = "+421 908 230 621",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:30",
                    rating = 0.0,
                    emergencyFeeNote = "Prevencia, čipovanie, pasy, digitalizované RTG",
                    latitude = 48.5675,
                    longitude = 19.1565,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 140L,
                    name = "Veterinárna ambulancia CANINO (MVDr. Carmen Lietavová)",
                    city = "Zvolen",
                    address = "Nográdyho 8939 (Zlatý potok), 960 01 Zvolen",
                    phone = "+421 905 733 951",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:00",
                    rating = 0.0,
                    emergencyFeeNote = "Malá prax pre psy a mačky, očkovanie a interná medicína",
                    latitude = 48.5872,
                    longitude = 19.1415,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 141L,
                    name = "Veterinárna klinika – MVDr. Juraj Nemček",
                    city = "Zvolen",
                    address = "J. Švermu 886/21, 960 01 Zvolen",
                    phone = "+421 905 267 425",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:00, So 09:00 - 12:00",
                    rating = 0.0,
                    emergencyFeeNote = "Psy, mačky, exoty a chirurgia",
                    latitude = 48.5738,
                    longitude = 19.1180,
                    clinicType = "Veterinárna klinika"
                ),
                VetClinic(
                    id = 142L,
                    name = "Veterina Slatina (MVDr. Július Gogola)",
                    city = "Zvolenská Slatina",
                    address = "SNP 25, 962 01 Zvolenská Slatina",
                    phone = "+421 905 279 290",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia podľa dohody",
                    rating = 0.0,
                    emergencyFeeNote = "Všeobecná veterinárna starostlivosť pre Zvolenskú Slatinu a okolie",
                    latitude = 48.5632,
                    longitude = 19.2591,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 143L,
                    name = "HM VET Kováčová",
                    city = "Kováčová",
                    address = "Kúpeľná 89/19, 962 37 Kováčová",
                    phone = "+421 915 046 639",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 09:00 - 17:00",
                    rating = 0.0,
                    emergencyFeeNote = "Ambulancia pre spoločenské zvieratá pri Zvolene",
                    latitude = 48.6020,
                    longitude = 19.1060,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 144L,
                    name = "Veterinárna klinika Sliač (MVDr. Olajec, Michalák, Raniak)",
                    city = "Sliač",
                    address = "ČSA 963/1810 (Hájniky), 962 31 Sliač",
                    phone = "+421 948 877 578",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:00, So 08:30 - 12:00",
                    rating = 0.0,
                    emergencyFeeNote = "Objednávky, chirurgia, diagnostika a interná medicína",
                    latitude = 48.6135,
                    longitude = 19.1418,
                    clinicType = "Veterinárna klinika"
                ),
                VetClinic(
                    id = 145L,
                    name = "Apivet – MVDr. Vladimír Kukliš",
                    city = "Detva",
                    address = "M. R. Štefánika 858/1, 962 12 Detva",
                    phone = "+421 905 508 784",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:00",
                    rating = 0.0,
                    emergencyFeeNote = "Komplexná ambulantná starostlivosť pre Podpoľanie",
                    latitude = 48.5562,
                    longitude = 19.4188,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 146L,
                    name = "Veterinárna ambulancia – MVDr. Juraj Veselý",
                    city = "Detva",
                    address = "Lúčna štvrť 46, 962 12 Detva",
                    phone = "+421 905 361 459",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 16:30",
                    rating = 0.0,
                    emergencyFeeNote = "Preventívna starostlivosť, očkovanie a výjazdy",
                    latitude = 48.5515,
                    longitude = 19.4235,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 147L,
                    name = "Veterinárna ambulancia Hriňová (MVDr. Dominika Paučová)",
                    city = "Hriňová",
                    address = "Jarmočná 1476, 962 05 Hriňová",
                    phone = "+421 905 473 446",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:00",
                    rating = 0.0,
                    emergencyFeeNote = "Ošetrenie psov, mačiek a drobných cicavcov",
                    latitude = 48.5758,
                    longitude = 19.5270,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 148L,
                    name = "Veterinárna ambulancia Krupina (MVDr. Mária Chudáčiková)",
                    city = "Krupina",
                    address = "Gliarska 11, 963 01 Krupina",
                    phone = "+421 949 353 261",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 16:30",
                    rating = 0.0,
                    emergencyFeeNote = "Očkovanie, čipovanie, pasy a základná chirurgia",
                    latitude = 48.3526,
                    longitude = 19.0672,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 149L,
                    name = "Veterinárna klinika ANIVET CENTRUM (MVDr. Ľubomír Kaprálik)",
                    city = "Nová Baňa",
                    address = "Školská 25, 968 01 Nová Baňa",
                    phone = "+421 915 901 364",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:30, So 09:00 - 11:30",
                    rating = 0.0,
                    emergencyFeeNote = "Diagnostika, laboratórium, chirurgia pre Novú Baňu a okolie",
                    latitude = 48.4232,
                    longitude = 18.6398,
                    clinicType = "Veterinárna klinika"
                ),
                VetClinic(
                    id = 150L,
                    name = "Veterinárna ambulancia – MVDr. Marek Ulbrik",
                    city = "Žarnovica",
                    address = "Partizánska 37/24, 966 81 Žarnovica",
                    phone = "+421 949 750 887",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:00",
                    rating = 0.0,
                    emergencyFeeNote = "Ambulancia pre psy, mačky a úprava srsti",
                    latitude = 48.4810,
                    longitude = 18.7160,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 151L,
                    name = "Veterinárna ambulancia – MVDr. Gizela Maďarová",
                    city = "Žarnovica",
                    address = "Slobody 18, 966 81 Žarnovica",
                    phone = "+421 905 384 288",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 16:30",
                    rating = 0.0,
                    emergencyFeeNote = "Prevencia, očkovanie a čipovanie",
                    latitude = 48.4825,
                    longitude = 18.7145,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 152L,
                    name = "Pet Medical – Veterinárne centrum Kremnica",
                    city = "Kremnica",
                    address = "Jula Horvátha 890/10, 967 01 Kremnica",
                    phone = "+421 915 840 245",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:00",
                    rating = 0.0,
                    emergencyFeeNote = "USG, laboratórium, dentálna hygiena a chirurgia",
                    latitude = 48.7058,
                    longitude = 18.9182,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 153L,
                    name = "VETKO – Veterinárna ambulancia (MVDr. Andrea Kolimárová)",
                    city = "Fiľakovo",
                    address = "Námestie Slobody 46, 986 01 Fiľakovo",
                    phone = "+421 908 315 623",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:00",
                    rating = 0.0,
                    emergencyFeeNote = "Ošetrenie spoločenských zvierat (SK/HU)",
                    latitude = 48.2688,
                    longitude = 19.8256,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 154L,
                    name = "Svet Zvierat – Veterinárna ambulancia Sereď",
                    city = "Sereď",
                    address = "Čepeňská 2586/12, 926 01 Sereď",
                    phone = "+421 918 799 961",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:00, So 09:00 - 12:00",
                    rating = 0.0,
                    emergencyFeeNote = "Komplexná prax vrátane akútneho ošetrenia na telefóne",
                    latitude = 48.2889,
                    longitude = 17.7340,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 155L,
                    name = "Veterinárna ambulancia – MVDr. Radovan Bozalka",
                    city = "Sereď",
                    address = "Železničná 1038/15, 926 00 Sereď",
                    phone = "+421 904 612 069",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:30",
                    rating = 0.0,
                    emergencyFeeNote = "Interná medicína, chirurgia a prevencia",
                    latitude = 48.2852,
                    longitude = 17.7365,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 156L,
                    name = "Veterinárna ambulancia – MVDr. Petra Hřebíčková",
                    city = "Sereď",
                    address = "Svätoplukova 2020/20, 926 01 Sereď",
                    phone = "+421 903 272 548",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 09:00 - 17:30",
                    rating = 0.0,
                    emergencyFeeNote = "Ambulancia pre psy, mačky a drobné cicavce",
                    latitude = 48.2865,
                    longitude = 17.7398,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 157L,
                    name = "Veterinárna klinika AMIGO Šaľa",
                    city = "Šaľa",
                    address = "Nemocničná 4, 927 01 Šaľa",
                    phone = "+421 903 245 442",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:30, So 09:00 - 12:00",
                    rating = 0.0,
                    emergencyFeeNote = "RTG, USG, chirurgia a hospitalizácia",
                    latitude = 48.1520,
                    longitude = 17.8780,
                    clinicType = "Veterinárna klinika"
                ),
                VetClinic(
                    id = 158L,
                    name = "Veterinárna ambulancia – MVDr. Eduard Laczko",
                    city = "Šaľa",
                    address = "Školská 5, 927 01 Šaľa",
                    phone = "+421 905 541 331",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:00",
                    rating = 0.0,
                    emergencyFeeNote = "Všeobecná prax a očkovanie",
                    latitude = 48.1510,
                    longitude = 17.8732,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 159L,
                    name = "AnimalCARE (MVDr. Kristína Bartošová)",
                    city = "Dubnica nad Váhom",
                    address = "Bratislavská 435/12, 018 41 Dubnica nad Váhom",
                    phone = "+421 948 457 030",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:00",
                    rating = 0.0,
                    emergencyFeeNote = "Preventívna a interná medicína spoločenských zvierat",
                    latitude = 48.9598,
                    longitude = 18.1664,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 160L,
                    name = "Veterinárna ambulancia BestVet",
                    city = "Dubnica nad Váhom",
                    address = "Dukelská štvrť 1402, 018 41 Dubnica nad Váhom",
                    phone = "+421 910 530 383",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:30",
                    rating = 0.0,
                    emergencyFeeNote = "Očkovanie, čipovanie, pasy a laboratórium",
                    latitude = 48.9562,
                    longitude = 18.1745,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 161L,
                    name = "Veterinárna ambulancia – MVDr. Veronika Rakytová",
                    city = "Púchov",
                    address = "Janka Kráľa 1271/85, 020 01 Púchov",
                    phone = "+421 911 831 777",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:30",
                    rating = 0.0,
                    emergencyFeeNote = "Starostlivosť o psy, mačky a drobné zvieratá",
                    latitude = 49.1245,
                    longitude = 18.3282,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 162L,
                    name = "Veterinárna ambulancia – MVDr. Rudolf Jakubík",
                    city = "Púchov",
                    address = "Zábreh 1510/21, 020 01 Púchov",
                    phone = "+421 908 775 383",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 17:00",
                    rating = 0.0,
                    emergencyFeeNote = "Všeobecná ambulancia a chirurgia",
                    latitude = 49.1292,
                    longitude = 18.3168,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 163L,
                    name = "Veterinárna ambulancia – MVDr. Evald Vítek",
                    city = "Púchov",
                    address = "Komenského 1343, 020 01 Púchov",
                    phone = "+421 903 849 779",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 16:30",
                    rating = 0.0,
                    emergencyFeeNote = "Ambulancia pre malé aj hospodárske zvieratá",
                    latitude = 49.1210,
                    longitude = 18.3230,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 164L,
                    name = "Veterinárna klinika VETFIX Šamorín",
                    city = "Šamorín",
                    address = "Pomlejská 16, 931 01 Šamorín",
                    phone = "+421 31 562 24 13",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 18:30, So 09:00 - 12:00",
                    rating = 0.0,
                    emergencyFeeNote = "RTG, USG, inhalačná anestézia a hospitalizácia",
                    latitude = 48.0315,
                    longitude = 17.3095,
                    clinicType = "Veterinárna klinika"
                ),
                VetClinic(
                    id = 165L,
                    name = "Veterinárna ambulancia – MVDr. Gaál & MVDr. Orsovics",
                    city = "Šamorín",
                    address = "Kováčka 19A, 931 01 Šamorín",
                    phone = "+421 31 562 24 13",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:30",
                    rating = 0.0,
                    emergencyFeeNote = "Prevencia, očkovanie a čipovanie (SK/HU)",
                    latitude = 48.0272,
                    longitude = 17.3118,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 166L,
                    name = "Veterinárna ambulancia Stupava – MVDr. Robert Kazarka",
                    city = "Stupava",
                    address = "Hlavná, 900 31 Stupava",
                    phone = "+421 905 407 442",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:00",
                    rating = 0.0,
                    emergencyFeeNote = "Sonografia, laboratórne vyšetrenia a terapia",
                    latitude = 48.2748,
                    longitude = 17.0318,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 167L,
                    name = "Veterinárna ambulancia Stupava – MVDr. Radoslav Rigler",
                    city = "Stupava",
                    address = "Agátová 5, 900 31 Stupava",
                    phone = "+421 902 359 790",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:30, So 09:00 - 12:00",
                    rating = 0.0,
                    emergencyFeeNote = "Chirurgia, inhalačná anestézia, USG a laboratórium",
                    latitude = 48.2712,
                    longitude = 17.0365,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 168L,
                    name = "Veterinárna ošetrovňa Bernolákovo – MVDr. Dušan Valenta",
                    city = "Bernolákovo",
                    address = "Železničná 93, 900 27 Bernolákovo",
                    phone = "+421 905 195 385",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 17:30",
                    rating = 0.0,
                    emergencyFeeNote = "Ošetrovňa pre psy, mačky a drobné zvieratá",
                    latitude = 48.1995,
                    longitude = 17.2990,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 169L,
                    name = "Veterinári Modra",
                    city = "Modra",
                    address = "Dolná 8, 900 01 Modra",
                    phone = "+421 904 944 558",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:00",
                    rating = 0.0,
                    emergencyFeeNote = "Ambulancia pre psy a mačky",
                    latitude = 48.3324,
                    longitude = 17.3075,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 170L,
                    name = "CARPATHIA VET Modra (MVDr. Roman Strieženec)",
                    city = "Modra",
                    address = "Štefánikova 22, 900 01 Modra",
                    phone = "+421 908 593 783",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 09:00 - 18:00",
                    rating = 0.0,
                    emergencyFeeNote = "Preventívna medicína, dentálna hygiena a diagnostika",
                    latitude = 48.3348,
                    longitude = 17.3092,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 171L,
                    name = "Veterinárna ambulancia Ivanka pri Dunaji (MVDr. Repta)",
                    city = "Ivanka pri Dunaji",
                    address = "Poľovnícka 17, 900 28 Ivanka pri Dunaji",
                    phone = "+421 940 622 312",
                    email = "",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:30 - 18:00",
                    rating = 0.0,
                    emergencyFeeNote = "Preventívna a chirurgická prax pre malé zvieratá",
                    latitude = 48.1872,
                    longitude = 17.2564,
                    clinicType = "Veterinárna ambulancia"
                ),
                VetClinic(
                    id = 172L,
                    name = "Veterinárna ambulancia LiVeS Liptovský Hrádok",
                    city = "Liptovský Hrádok",
                    address = "SNP 200, 033 01 Liptovský Hrádok",
                    phone = "+421 905 505 015",
                    email = "",
                    is24hEmergency = true,
                    openingHours = "Po-Pia + NONSTOP sieťová pohotovosť 24/7",
                    rating = 0.0,
                    emergencyFeeNote = "Súčasť pohotovostnej siete LiVeS Liptov (24/7 na telefóne)",
                    latitude = 49.0385,
                    longitude = 19.7235,
                    clinicType = "Pohotovosť 24/7"
                )
            )
        }
    }
}
