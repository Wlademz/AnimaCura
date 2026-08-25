package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.ForumDao
import com.example.data.local.dao.MedicalRecordDao
import com.example.data.local.dao.MedicationDao
import com.example.data.local.dao.PetDao
import com.example.data.local.dao.PetPhotoDao
import com.example.data.local.dao.ReminderDao
import com.example.data.local.dao.VetClinicDao
import com.example.data.local.dao.WeightLogDao
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
        ForumComment::class
    ],
    version = 1,
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

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pet_health_database"
                )
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
                    targetWeightKg = 31.5
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
                    targetWeightKg = 4.3
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
                    drawableResName = "pet_med_icon_1787650018098"
                )
            )
            photoDao.insertPhoto(
                PetPhoto(
                    petId = pet1Id,
                    title = "Lekárska správa z RTG",
                    category = "Lekárska správa / Recept",
                    date = "20.10.2023",
                    description = "Oficiálny posudok dysplázie bedrových kĺbov.",
                    drawableResName = "pet_med_icon_1787650018098"
                )
            )
            photoDao.insertPhoto(
                PetPhoto(
                    petId = pet2Id,
                    title = "Stav ďasien po ultrazvukovom čistení",
                    category = "Zuby / Ústna dutina",
                    date = "10.04.2024",
                    description = "Ďasná bez začervenania, zubný kameň úplne odstránený.",
                    drawableResName = "pet_med_icon_1787650018098"
                )
            )

            // 7. Vet Emergency & Clinics Database (Real Slovakia locations)
            val clinics = listOf(
                VetClinic(
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
                    longitude = 17.1592
                ),
                VetClinic(
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
                    longitude = 17.0945
                ),
                VetClinic(
                    name = "Univerzitná veterinárna nemocnica UVLF",
                    city = "Košice",
                    address = "Komenského 73, 041 81 Košice",
                    phone = "+421 915 984 689",
                    email = "nemocnica@uvlf.sk",
                    is24hEmergency = true,
                    openingHours = "NONSTOP pohotovosť",
                    rating = 4.9,
                    emergencyFeeNote = "Špičková klinika s JIS, pohotovostná chirurgická a interná služba",
                    latitude = 48.7389,
                    longitude = 21.2467
                ),
                VetClinic(
                    name = "Veterinárna pohotovostná klinika Žilina",
                    city = "Žilina",
                    address = "Vysokoškolákov 8, 010 08 Žilina",
                    phone = "+421 903 600 700",
                    email = "kontakt@vetza.sk",
                    is24hEmergency = true,
                    openingHours = "NONSTOP (24/7 pohotovosť)",
                    rating = 4.7,
                    emergencyFeeNote = "Akútne zákroky, nočná pohotovosť na telefóne",
                    latitude = 49.2155,
                    longitude = 18.7523
                ),
                VetClinic(
                    name = "VetClinic Banská Bystrica",
                    city = "Banská Bystrica",
                    address = "Zvolenská cesta 14, 974 05 Banská Bystrica",
                    phone = "+421 48 416 33 22",
                    email = "ambulancia@vetbb.sk",
                    is24hEmergency = true,
                    openingHours = "Po-Ne 08:00 - 20:00 (Pohotovosť na zavolanie)",
                    rating = 4.8,
                    emergencyFeeNote = "Pohotovostný veterinárny lekár v dosahu 24h",
                    latitude = 48.7212,
                    longitude = 19.1398
                ),
                VetClinic(
                    name = "Veterinárne centrum Nitra",
                    city = "Nitra",
                    address = "Štefánikova trieda 70, 949 01 Nitra",
                    phone = "+421 948 123 456",
                    email = "nitra@vetcentrum.sk",
                    is24hEmergency = false,
                    openingHours = "Po-Pia 08:00 - 19:00, So 09:00 - 13:00",
                    rating = 4.6,
                    emergencyFeeNote = "Špecializovaná kardiológia a dermatológia",
                    latitude = 48.3061,
                    longitude = 18.0863
                )
            )
            clinicDao.insertClinics(clinics)

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
        }
    }
}
