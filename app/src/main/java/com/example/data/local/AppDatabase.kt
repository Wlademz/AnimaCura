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
            if (existing.size < 60) {
                clinicDao.insertClinics(getDefaultVetClinics())
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
                    rating = 4.9,
                    emergencyFeeNote = "Akútna pohotovostná služba, chirurgické zákroky, RTG a hospitalizácia",
                    latitude = 48.5772,
                    longitude = 19.1245,
                    clinicType = "Pohotovosť 24/7",
                    userRating = 5,
                    userReview = "Veľmi ochotný a obetavý pán doktor, pomohol nám urgentne v noci."
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
                )
            )
        }
    }
}
