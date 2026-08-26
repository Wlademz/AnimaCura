package com.example.data.local

data class VeterinaryDrug(
    val id: String,
    val name: String,
    val activeSubstance: String,
    val targetSpecies: List<String>, // e.g. "Pes", "Mačka", "Kôň", "Hlodavec"
    val category: String, // e.g. "Antiparazitiká", "Antibiotiká", "Bolesť & Kĺby (NSAID)", "Alergie & Koža", "Kardiológia", "Trávenie", "Uši & Oči"
    val dosageGuideline: String,
    val form: String, // Tablety, Spot-on, Sirup, Injekcia, Kvapky, Pasta
    val indications: String,
    val contraindications: String,
    val sideEffects: String,
    val administrationMethod: String,
    val storageInstructions: String,
    val prescriptionRequired: Boolean = true,
    val packageDescription: String = "",
    val badgeColorHex: Long = 0xFF0D9488
)

object VeterinaryDrugDatabase {

    val drugs: List<VeterinaryDrug> = listOf(
        VeterinaryDrug(
            id = "bravecto",
            name = "Bravecto (Fluralaner)",
            activeSubstance = "Fluralaner (112.5mg, 250mg, 500mg, 1000mg, 1400mg)",
            targetSpecies = listOf("Pes", "Mačka"),
            category = "Antiparazitiká (Kliešte & Blchy)",
            dosageGuideline = "25–56 mg fluralaneru na 1 kg živej hmotnosti každých 12 týždňov (3 mesiace).",
            form = "Žuvacia tableta / Spot-on roztok",
            indications = "Liečba a prevencia infestácie blchami (Ctenocephalides felis/canis) a kliešťami (Ixodes ricinus, Rhipicephalus sanguineus, Dermacentor reticulatus). Poskytuje 12 týždňov nepretržitej ochrany. Vhodné aj pri liečbe demodikózy a sarkoptového svrabu.",
            contraindications = "Nepoužívať pri precitlivenosti na liečivo. Opatrnosť u psov s anamnézou epilepsie alebo neurologických záchvatov.",
            sideEffects = "Ojedinele mierne a prechodné gastrointestinálne príznaky ako hnačka, zvracanie, znížená chuť do jedla alebo slinenie.",
            administrationMethod = "Žuvacie tablety sa podávajú v čase kŕmenia alebo spolu s jedlom. Spot-on roztok sa aplikuje priamo na kožu na báze lebky.",
            storageInstructions = "Uchovávajte pri teplote do 30 °C v pôvodnom blistri na ochranu pred vlhkosťou.",
            prescriptionRequired = true,
            packageDescription = "Biela škatuľka s modro-zeleným označením a vyobrazením psa/mačky podľa hmotnostnej kategórie.",
            badgeColorHex = 0xFF0D9488
        ),
        VeterinaryDrug(
            id = "nexgard_spectra",
            name = "NexGard Spectra",
            activeSubstance = "Afoxolaner + Milbemycín oxím",
            targetSpecies = listOf("Pes"),
            category = "Komplexné Antiparazitiká",
            dosageGuideline = "2.5–5.36 mg afoxolaneru a 0.5–1.07 mg milbemycínu na 1 kg hmotnosti raz mesačne.",
            form = "Mäkká žuvacia tableta s hovädzou príchuťou",
            indications = "Kombinovaná ochrana proti blchám, kliešťom, črevným hlístovcom (škrkavky, machovky, tenkohlavce) a prevencia srdcovej a pľúcnej červivosti (Dirofilaria, Angiostrongylus).",
            contraindications = "Nepoužívať u šteniat mladších ako 8 týždňov alebo s hmotnosťou pod 1,35 kg. Opatrnosť u kólií a príbuzných plemien (MDR1 gén).",
            sideEffects = "Zriedkavo hnačka, zvracanie, letargia alebo pruritus.",
            administrationMethod = "Podávať priamo do tlamy alebo s krmivom 1x za mesiac.",
            storageInstructions = "Uchovávajte v pôvodnom obale pri teplote do 30 °C.",
            prescriptionRequired = true,
            packageDescription = "Červeno-biela škatuľka s výrazným logom NexGard Spectra a farebným kódovaním podľa váhy.",
            badgeColorHex = 0xFFE07A5F
        ),
        VeterinaryDrug(
            id = "apoquel",
            name = "Apoquel (Oclacitinib)",
            activeSubstance = "Oclacitinib maleát (3.6mg, 5.4mg, 16mg)",
            targetSpecies = listOf("Pes"),
            category = "Alergie & Dermatológia",
            dosageGuideline = "0.4 až 0.6 mg/kg živej hmotnosti 2x denne počas prvých 14 dní, potom udržiavacia dávka 1x denne.",
            form = "Filmom obalené / žuvacie tablety",
            indications = "Kontrola pruritu (svrbenia) spojeného s alergickou a atopickou dermatitídou u psov. Rýchly nástup účinku do 4 hodín.",
            contraindications = "Nepoužívať u psov mladších ako 12 mesiacov alebo s hmotnosťou pod 3 kg. Nepoužívať u psov so závažnou imunosupresiou alebo progresívnym nádorovým ochorením.",
            sideEffects = "Môže zvýšiť náchylnosť na infekcie (pyodermia, demodikóza), občasné zvracanie alebo riedka stolica.",
            administrationMethod = "Podáva sa perorálne, s jedlom alebo nalačno.",
            storageInstructions = "Uchovávajte pri teplote do 25 °C na suchom mieste mimo dosahu detí.",
            prescriptionRequired = true,
            packageDescription = "Bielo-oranžová plastová fľaštička alebo blister s modrým nápisom Apoquel.",
            badgeColorHex = 0xFFF59E0B
        ),
        VeterinaryDrug(
            id = "simparica_trio",
            name = "Simparica Trio",
            activeSubstance = "Sarolaner + Moksidektín + Pyrantel",
            targetSpecies = listOf("Pes"),
            category = "Komplexné Antiparazitiká",
            dosageGuideline = "1 žuvacia tableta mesačne zodpovedajúca hmotnostnej kategórii psa.",
            form = "Žuvacia tableta s pečeňovou príchuťou",
            indications = "Ochrana pred kliešťami, blchami, črevnými parazitmi (škrkavky, machovky) a prevencia dirofilariózy (srdcovej červivosti).",
            contraindications = "Nepoužívať pri precitlivenosti na liečivá. Nevhodné pre šteňatá do 8 týždňov veku.",
            sideEffects = "Veľmi zriedkavo mierne gastrointestinálne poruchy, ataxia, svalový tremor.",
            administrationMethod = "Podávať raz za mesiac spolu s krmivom alebo samostatne ako maškrtu.",
            storageInstructions = "Uchovávajte pri teplote do 30 °C.",
            prescriptionRequired = true,
            packageDescription = "Zeleno-biela škatuľka s označením Simparica Trio.",
            badgeColorHex = 0xFF10B981
        ),
        VeterinaryDrug(
            id = "milpro",
            name = "Milpro (Milbemycín + Praziquantel)",
            activeSubstance = "Milbemycín oxím + Praziquantel",
            targetSpecies = listOf("Pes", "Mačka"),
            category = "Odčervenie (Endoparazitiká)",
            dosageGuideline = "Pes: 0.5 mg milbemycínu a 5 mg praziquantelu na kg. Mačka: 2 mg milbemycínu a 5 mg praziquantelu na kg.",
            form = "Oválne deliteľné ochutené tablety",
            indications = "Liečba a kontrola zmiešaných infekcií dospelými pásomnicami (Cestoda) a hlístovcami (Nematoda). Prevencia srdcovej červivosti.",
            contraindications = "Nepoužívať u psov s hmotnosťou pod 5 kg pri verzii pre veľké psy. U mačiatok nepoužívať do 6 týždňov veku / pod 0.5 kg.",
            sideEffects = "Zriedkavo hypersenzitivita, letargia, zvracanie po masívnom úhyne parazitov.",
            administrationMethod = "Podávať perorálne priamo alebo s malým množstvom potravy.",
            storageInstructions = "Polovičné tablety spotrebovať do 6 mesiacov.",
            prescriptionRequired = true,
            packageDescription = "Modrá škatuľka pre psov / fialová škatuľka pre mačky s deliteľnými tabletkami.",
            badgeColorHex = 0xFF8B5CF6
        ),
        VeterinaryDrug(
            id = "clavaseptin",
            name = "Clavaseptin / Synulox (Amoxicilín + Kyselina klavulánová)",
            activeSubstance = "Amoxicillinum (trihydricum) + Acidum clavulanicum",
            targetSpecies = listOf("Pes", "Mačka"),
            category = "Antibiotiká",
            dosageGuideline = "10 mg amoxicilínu / 2.5 mg kyseliny klavulánovej na 1 kg ž.hm. 2-krát denne (každých 12 hodín) počas 7–14 dní.",
            form = "Ochutené deliteľné tablety (50mg, 62.5mg, 250mg, 500mg)",
            indications = "Liečba bakteriálnych infekcií kože (pyodermia), rán, abscesov, infekcií močových ciest, dýchacích ciest a zápalov ďasien.",
            contraindications = "Nepoužívať u zvierat s precitlivenosťou na penicilíny. Nepodávať králikom, morčatám, škrečkom a činčilám (fatálne narušenie črevnej mikroflóry).",
            sideEffects = "Mierne tráviace ťažkosti (hnačka, nevoľnosť). V prípade silnej hnačky konzultujte veterinára.",
            administrationMethod = "Podávať perorálne na začiatku kŕmenia na zníženie rizika gastrointestinálneho diskomfortu.",
            storageInstructions = "Uchovávajte v pôvodnom hliníkovom blistri pri teplote do 25 °C.",
            prescriptionRequired = true,
            packageDescription = "Klinická biela škatuľka s červeným alebo modrým pruhom a blistrovými pásikmi.",
            badgeColorHex = 0xFF3B82F6
        ),
        VeterinaryDrug(
            id = "meloxidyl",
            name = "Meloxidyl / Metacam (Meloxikam)",
            activeSubstance = "Meloxicam (1.5 mg/ml pre psov, 0.5 mg/ml pre mačky)",
            targetSpecies = listOf("Pes", "Mačka", "Kôň"),
            category = "Bolesť & Kĺby (NSAID)",
            dosageGuideline = "Pes: Úvodná dávka 0.2 mg/kg prvý deň, následne udržiavacia dávka 0.1 mg/kg 1x denne. Mačka: podľa presnej kalibrovanej striekačky na váhu mačky.",
            form = "Perorálna suspenzia s dávkovacou striekačkou",
            indications = "Zmiernenie zápalu a bolesti pri akútnych a chronických ochoreniach pohybového aparátu (artróza, dysplázia), pooperačná analgézia.",
            contraindications = "Nepoužívať u gravidných zvierat, pri vredoch gastrointestinálneho traktu, pri závažnom zlyhávaní obličiek, pečene alebo srdca.",
            sideEffects = "Gastrointestinálne reakcie (strata chuti do jedla, vracanie, meléna - tmavá stolica, apatia). Pri výskyte ihneď prerušiť liečbu.",
            administrationMethod = "Podávať vždy premiešané s malým množstvom krmiva alebo priamo do tlamy po jedle.",
            storageInstructions = "Po prvom otvorení spotrebovať do 6 mesiacov.",
            prescriptionRequired = true,
            packageDescription = "Fľaštička s kvapkadlom/adaptérom a presnou odmernou striekačkou ciachovanou v kg.",
            badgeColorHex = 0xFFD97706
        ),
        VeterinaryDrug(
            id = "previcox",
            name = "Previcox (Firokoxib)",
            activeSubstance = "Firocoxib (57mg, 227mg)",
            targetSpecies = listOf("Pes", "Kôň"),
            category = "Bolesť & Kĺby (NSAID)",
            dosageGuideline = "5 mg/kg živej hmotnosti raz denne.",
            form = "Ochutené deliteľné tablety",
            indications = "Liečba bolesti a zápalu spojeného s osteoartritídou u psov, úľava od pooperačnej bolesti po ortopedických zákrokoch.",
            contraindications = "Nepoužívať u psov mladších ako 10 týždňov alebo s hmotnosťou pod 3 kg. Nekombinovať s inými nesteroidnými antiflogistikami ani kortikosteroidmi.",
            sideEffects = "Zvracanie, hnačka, prechodné nechutenstvo.",
            administrationMethod = "Podávať perorálne s jedlom alebo bez jedla 1x denne.",
            storageInstructions = "Uchovávajte pri teplote do 30 °C.",
            prescriptionRequired = true,
            packageDescription = "Hnedo-zelená škatuľka s deliteľnými hnedými tabletami.",
            badgeColorHex = 0xFF059669
        ),
        VeterinaryDrug(
            id = "cerenia",
            name = "Cerenia (Maropitant)",
            activeSubstance = "Maropitant citrát (16mg, 24mg, 60mg, 160mg)",
            targetSpecies = listOf("Pes", "Mačka"),
            category = "Trávenie & Zvracanie",
            dosageGuideline = "Prevencia kinetózy (nevoľnosť v aute): 8 mg/kg 2 hodiny pred cestou. Liečba zvracania: 2 mg/kg 1x denne.",
            form = "Obalené tablety / Injekčný roztok",
            indications = "Prevencia zvracania vyvolaného cestovaním autom, liečba a prevencia nauzey a vracania pri gastritíde, chemoterapii či pankreatitíde.",
            contraindications = "Nepoužívať u šteniat mladších ako 8 týždňov (alebo pod 16 týždňov pri vysokej dávke proti kinetóze).",
            sideEffects = "Zriedkavo letargia, ospalosť.",
            administrationMethod = "Pred cestou podať ľahké jedlo (nepodávať na úplne prázdny žalúdok ani nezabaliť do mastného jedla).",
            storageInstructions = "Uchovávajte v pôvodnom obale pri izbovej teplote.",
            prescriptionRequired = true,
            packageDescription = "Svetlomodrá škatuľka s nápisom Cerenia a blistrom so 4 tabletami.",
            badgeColorHex = 0xFF2563EB
        ),
        VeterinaryDrug(
            id = "vetmedin",
            name = "Vetmedin (Pimobendan)",
            activeSubstance = "Pimobendan (1.25mg, 2.5mg, 5mg, 10mg)",
            targetSpecies = listOf("Pes"),
            category = "Kardiológia",
            dosageGuideline = "0.5 mg/kg rozdelené do dvoch dávok (0.25 mg/kg ráno a 0.25 mg/kg večer cca 1 hodinu pred kŕmením).",
            form = "Ochutené žuvacie tablety / Kapsuly",
            indications = "Liečba kongestívneho zlyhania srdca u psov zapríčineného dilatačnou kardiomyopatiou alebo chlopňovou insuficienciou (myxomatózna degenerácia mitrálnej chlopne).",
            contraindications = "Nepoužívať pri hypertrofickej kardiomyopatii alebo stenóze aorty.",
            sideEffects = "Mierne zvýšenie srdcovej frekvencie, zriedkavo zvracanie.",
            administrationMethod = "Podávať nalačno aspoň 1 hodinu pred kŕmením, inak sa znižuje vstrebávanie liečiva.",
            storageInstructions = "Uchovávajte v pevne uzavretej fľaštičke s vysúšadlom pri teplote do 25 °C.",
            prescriptionRequired = true,
            packageDescription = "Biela plastová fľaštička s fialovým štítkom a vyobrazením srdiečka so psom.",
            badgeColorHex = 0xFF7C3AED
        ),
        VeterinaryDrug(
            id = "surolan",
            name = "Surolan ušné / kožné kvapky",
            activeSubstance = "Mikonazol nitrát + Polymyxín B sulfát + Prednizolón acetát",
            targetSpecies = listOf("Pes", "Mačka"),
            category = "Uši & Oči & Koža",
            dosageGuideline = "3 až 5 kvapiek do postihnutého zvukovodu 2x denne počas 7–14 dní.",
            form = "Suspenzia - ušné a kožné kvapky",
            indications = "Liečba zápalov vonkajšieho zvukovodu (otitis externa) a kožných infekcií vyvolaných kvasinkami (Malassezia), plesňami a grampozitívnymi/gramnegatívnymi baktériami sprevádzaných svrbením.",
            contraindications = "Nepoužívať pri perforovanom (prasknutom) bubienku.",
            sideEffects = "Pri dlhodobom používaní lokálne atrofie kože.",
            administrationMethod = "Pred aplikáciou ucho mechanicky vyčistite. Po nakvapkaní jemne masírujte bázu ucha.",
            storageInstructions = "Pred každým použitím dôkladne pretrepať! Uchovávajte pri teplote do 25 °C.",
            prescriptionRequired = true,
            packageDescription = "Biela plastová fľaštička 15 ml alebo 30 ml s aplikátorom v zeleno-bielom obale.",
            badgeColorHex = 0xFF0D9488
        ),
        VeterinaryDrug(
            id = "caniquantel_plus",
            name = "Caniquantel Plus",
            activeSubstance = "Fenbendazol (500mg) + Praziquantel (50mg)",
            targetSpecies = listOf("Pes", "Mačka"),
            category = "Odčervenie (Endoparazitiká)",
            dosageGuideline = "1 tableta na 10 kg živej hmotnosti jednorazovo (alebo pasta pre mačky/šteniatka).",
            form = "Tablety / Perorálna pasta",
            indications = "Odčervenie proti oblým a plochým červom (škrkavky Toxocara, Toxascaris, machovky Ancylostoma, pásomnice Dipylidium, Taenia, Echinococcus).",
            contraindications = "Nepodávať gravidným sučkám v prvej polovici gravidity.",
            sideEffects = "Veľmi dobrá znášanlivosť, výnimočne prechodné zvracanie.",
            administrationMethod = "Podávať priamo do tlamy alebo zabalené v kúsku mäsa/syra.",
            storageInstructions = "Skladujte na suchom mieste pri teplote do 25 °C.",
            prescriptionRequired = false,
            packageDescription = "Strieborný blister v modro-žltej škatuľke.",
            badgeColorHex = 0xFF6366F1
        ),
        VeterinaryDrug(
            id = "bravecto_plus_cat",
            name = "Bravecto Plus pre mačky",
            activeSubstance = "Fluralaner + Moksidektín",
            targetSpecies = listOf("Mačka"),
            category = "Antiparazitiká pre mačky",
            dosageGuideline = "1 pipeta podľa hmotnosti mačky každých 12 týždňov.",
            form = "Spot-on roztok na nakvapkanie na kožu",
            indications = "Liečba kliešťov, bĺch, ušného svrabu (Otodectes cynotis) a črevných parazitov u mačiek s 12-týždňovým trvaním účinku.",
            contraindications = "Nepoužívať u mačiatok mladších ako 9 týždňov alebo s hmotnosťou pod 1.2 kg.",
            sideEffects = "Prechodná strata srsti alebo začervenanie v mieste aplikácie.",
            administrationMethod = "Aplikovať na suchú kožu na zátylku mačky, kde sa nemôže olízať.",
            storageInstructions = "Uchovávajte v pôvodnom vrecúšku na ochranu pred svetlom.",
            prescriptionRequired = true,
            packageDescription = "Zeleno-fialová škatuľka s pipetou Twist'n'Use.",
            badgeColorHex = 0xFFEC4899
        ),
        VeterinaryDrug(
            id = "cytopoint",
            name = "Cytopoint (Lokivetmab)",
            activeSubstance = "Lokivetmab (monoklonálna protilátka)",
            targetSpecies = listOf("Pes"),
            category = "Alergie & Dermatológia",
            dosageGuideline = "Minimálna dávka 1 mg/kg podkožne raz za 4 až 8 týždňov u veterinárneho lekára.",
            form = "Injekčný roztok (aplikuje veterinár)",
            indications = "Liečba klinických prejavov atopickej dermatitídy a alergického svrbenia u psov. Cielene neutralizuje IL-31 (hlavný spúšťač svrbenia).",
            contraindications = "Nepoužívať pri precitlivenosti. Nepoužívať u psov s hmotnosťou pod 3 kg.",
            sideEffects = "Výnimočne anafylaktoidné reakcie, opuch v mieste vpichu.",
            administrationMethod = "Subkutánna injekcia aplikovaná odborným veterinárnym personálom.",
            storageInstructions = "Uchovávajte v chladničke pri teplote 2 °C až 8 °C. Chráňte pred mrazom.",
            prescriptionRequired = true,
            packageDescription = "Sklenená injekčná liekovka s modrým uzáverom v chladiacom boxe.",
            badgeColorHex = 0xFF0284C7
        ),
        VeterinaryDrug(
            id = "alaptid",
            name = "Alaptid masť",
            activeSubstance = "Alaptidum (syntetický dipeptid)",
            targetSpecies = listOf("Pes", "Mačka", "Kôň", "Hlodavec", "Vták"),
            category = "Rany & Hojenie kože",
            dosageGuideline = "Nanášať v tenkej vrstve na očistenú ranu 1-2x denne až do zhojenia.",
            form = "Dermatologická masť",
            indications = "Rýchle hojenie neinfikovaných rán, odrenín, popálenín, preležanín, operačných rezov a labiek poranených soľou v zime.",
            contraindications = "Žiadne známe kontraindikácie.",
            sideEffects = "Veľmi dobre znášaný, bez vedľajších účinkov.",
            administrationMethod = "Lokálne na pokožku, v prípade potreby prekryť sterilným obväzom.",
            storageInstructions = "Uchovávajte pri teplote 10 °C až 25 °C.",
            prescriptionRequired = false,
            packageDescription = "Hliníková tuba 20g v bielo-oranžovej škatuľke.",
            badgeColorHex = 0xFFEA580C
        )
    )

    fun searchDrugs(query: String, speciesFilter: String = "Všetky", categoryFilter: String = "Všetky"): List<VeterinaryDrug> {
        return drugs.filter { drug ->
            val matchesQuery = query.isBlank() ||
                    drug.name.contains(query, ignoreCase = true) ||
                    drug.activeSubstance.contains(query, ignoreCase = true) ||
                    drug.indications.contains(query, ignoreCase = true) ||
                    drug.category.contains(query, ignoreCase = true)

            val matchesSpecies = speciesFilter == "Všetky" || drug.targetSpecies.contains(speciesFilter)
            val matchesCategory = categoryFilter == "Všetky" || drug.category.contains(categoryFilter, ignoreCase = true)

            matchesQuery && matchesSpecies && matchesCategory
        }
    }
}
