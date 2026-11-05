package com.example.data.local

enum class ToxicityLevel(val label: String, val badgeText: String, val colorHex: Long) {
    DEADLY("Smrteľne nebezpečné (Okamžite veterina)", "🚨 Smrteľne nebezpečné", 0xFFDC2626),
    HIGH("Vysoké riziko otravy", "⚠️ Vysoké riziko", 0xFFEA580C),
    MODERATE("Mierne riziko / Opatrnosť", "⚡ Mierne riziko", 0xFFEAB308),
    SAFE("Bezpečné pre zvieratá", "✅ Bezpečné", 0xFF16A34A)
}

data class ToxicItem(
    val id: String,
    val name: String,
    val category: String, // Potraviny, Rastliny & Kvety, Lieky pre ľudí, Domáca chémia
    val toxicityLevel: ToxicityLevel,
    val targetAnimals: String, // Pes a Mačka, Hlavne Mačky, Psy
    val toxicSubstance: String,
    val symptoms: String,
    val emergencyAction: String,
    val description: String
)

data class ChocolateToxicityResult(
    val theobromineMgPerKg: Double,
    val totalTheobromineMg: Double,
    val severity: String,
    val severityLevel: ToxicityLevel,
    val symptomsExpected: String,
    val actionRequired: String
)

object ToxicityDatabase {

    val items: List<ToxicItem> = listOf(
        ToxicItem(
            id = "cokolada",
            name = "Čokoláda a Kakao",
            category = "Potraviny",
            toxicityLevel = ToxicityLevel.DEADLY,
            targetAnimals = "Pes a Mačka",
            toxicSubstance = "Teobromín a kofeín",
            symptoms = "Zvracanie, hnačka, nepokoj, zrýchlený tep, kŕče, arytmia, zlyhanie srdca.",
            emergencyAction = "Ak zjedol pred menej ako 2 hodinami, kontaktujte pohotovosť kvôli vyvolaniu zvracania a podaniu čierneho uhlia.",
            description = "Čím je čokoláda tmavšia (viac % kakaa), tým je toxickejšia. Kakaový prášok a čokoláda na varenie sú extrémne nebezpečné už v malom množstve."
        ),
        ToxicItem(
            id = "xylitol",
            name = "Xylitol (Brezový cukor v žuvačkách)",
            category = "Potraviny",
            toxicityLevel = ToxicityLevel.DEADLY,
            targetAnimals = "Pes",
            toxicSubstance = "Xylitol (E967)",
            symptoms = "Masívne vyplavenie inzulínu, ťažká hypoglykémia (do 30 minút), kolaps, kŕče, akútne zlyhanie pečene.",
            emergencyAction = "ŽIVOT OHROZUJÚCI STAV! Okamžite vyrazte na veterinárnu pohotovosť. Ak je pes pri vedomí, potrite mu ďasná medom alebo glukózou.",
            description = "Nachádza sa v žuvačkách bez cukru, zubných pastách, fit sladkostiach a pečive. Pre psa je smrteľná už 1-2 žuvačky."
        ),
        ToxicItem(
            id = "hrozno",
            name = "Hrozno a Hrozienka",
            category = "Potraviny",
            toxicityLevel = ToxicityLevel.DEADLY,
            targetAnimals = "Pes a Mačka",
            toxicSubstance = "Kyselina vínna",
            symptoms = "Zvracanie, letargia, bolesť brucha, akútne anurické zlyhanie obličiek (pes prestane močiť do 24-48 hodín).",
            emergencyAction = "Aj jediné hrozienko môže u citlivého psa spôsobiť zlyhanie obličiek. Okamžite vyhľadajte veterinára kvôli infúznej terapii.",
            description = "Mechanizmus toxicity je nevyspytateľný. Toxické sú čerstvé bobule, hrozienka aj pečené koláče s hrozienkami."
        ),
        ToxicItem(
            id = "cibula_cesnak",
            name = "Cibuľa, Cesnak a Pór (Allium)",
            category = "Potraviny",
            toxicityLevel = ToxicityLevel.HIGH,
            targetAnimals = "Pes a Mačka (Mačky sú 3x citlivejšie)",
            toxicSubstance = "Tiosulfáty (spôsobujú oxidačný rozpad červených krviniek)",
            symptoms = "Hemolytická anémia, bledé až žlté ďasná, tmavý červenohnedý moč, slabosť, dýchavičnosť.",
            emergencyAction = "Veterinárne vyšetrenie krvi. Pozor na varené jedlá, detské výživy a polievky s cibuľovým základom.",
            description = "Toxické v surovom, varenom aj sušenom (korenie) stave. Príznaky anémie sa môžu prejaviť až po 2 až 4 dňoch."
        ),
        ToxicItem(
            id = "paralen_ibuprofen",
            name = "Paralen, Ibalgin a Aspirín (Ľudské lieky)",
            category = "Lieky pre ľudí",
            toxicityLevel = ToxicityLevel.DEADLY,
            targetAnimals = "Pes a Mačka (Paracetamol je pre mačky 100% smrteľný jed)",
            toxicSubstance = "Paracetamol, Ibuprofen, Kyselina acetylsalicylová",
            symptoms = "U mačiek: methemoglobinémia (čokoládovo hnedá krv, dusenie, opuch hlavy). U psov: krvácavé vredy žalúdka, zlyhanie obličiek.",
            emergencyAction = "NIKDY nepodávajte zvieraťu ľudské lieky na bolesť! Okamžite kontaktujte pohotovosť s presným názvom a dávkou.",
            description = "Jediná tabletka Paracetamolu 500mg spoľahlivo usmrtí dospelú mačku z dôvodu nedostatku enzýmu glukuronidázy."
        ),
        ToxicItem(
            id = "lalie",
            name = "Ľalie (Lilium a Hemerocallis)",
            category = "Rastliny & Kvety",
            toxicityLevel = ToxicityLevel.DEADLY,
            targetAnimals = "IBA Mačky (Smrteľné)",
            toxicSubstance = "Neznámy nefrotoxín rozpustný vo vode",
            symptoms = "Zvracanie, slinenie, odmietanie potravy, do 36 hodín úplné zlyhanie obličiek.",
            emergencyAction = "Extrémna pohotovosť! Aj olízanie peľu z kožúška alebo napitie sa vody z vázy, kde boli ľalie, je smrteľné.",
            description = "Ľalie nesmú byť v žiadnej domácnosti s mačkou. Vyžaduje okamžitú agresívnu vnútrožilovú infúziu do 18 hodín."
        ),
        ToxicItem(
            id = "avokado",
            name = "Avokádo",
            category = "Potraviny",
            toxicityLevel = ToxicityLevel.MODERATE,
            targetAnimals = "Pes, Vtáky, Hlodavce",
            toxicSubstance = "Persín a riziko upchatia kôstkou",
            symptoms = "Tráviace ťažkosti, tekutina okolo pľúc a srdca u vtákov. U psov hrozí mechanická nepriechodnosť čriev kôstkou.",
            emergencyAction = "Ak pes prehltol kôstku z avokáda, je nutný okamžitý RTG/sono zákrok pred prechodom do čreva.",
            description = "Dužina má vysoký obsah tukov (riziko pankreatitídy), šupka a kôstka obsahujú fungicídny toxín persín."
        ),
        ToxicItem(
            id = "antifreeze",
            name = "Nemrznúca zmes (Fridex / Etylénglykol)",
            category = "Domáca chémia",
            toxicityLevel = ToxicityLevel.DEADLY,
            targetAnimals = "Pes a Mačka",
            toxicSubstance = "Etylénglykol",
            symptoms = "Opitá chôdza, zvracanie, nadmerné pitie, po 12h kryštalizácia oxalátov v obličkách a smrť.",
            emergencyAction = "Okamžitá hospitalizácia a podanie protijedu (etanol / fomepizol) v prvej hodine od požitia!",
            description = "Má sladkú chuť, zvieratá ju rady vylížu z mláky pod autom v zime. Pár kvapiek stačí na usmrtenie zvieraťa."
        ),
        ToxicItem(
            id = "mrkva_jablko",
            name = "Mrkva, Uhorka a Jablko (bez jadierok)",
            category = "Potraviny",
            toxicityLevel = ToxicityLevel.SAFE,
            targetAnimals = "Pes a Mačka",
            toxicSubstance = "Žiadna (Bezpečné)",
            symptoms = "Žiadne negatívne príznaky. Zdravý zdroj vlákniny a vitamínov.",
            emergencyAction = "Nie je potrebná žiadna akcia. Výborná nízkokalorická maškrta.",
            description = "Vhodné na čistenie zubov a chrumkanie. Z jabĺk vždy odstráňte jadierka, ktoré obsahujú stopové množstvo kyanidu."
        ),
        ToxicItem(
            id = "varene_kura",
            name = "Varené kuracie mäso a ryža",
            category = "Potraviny",
            toxicityLevel = ToxicityLevel.SAFE,
            targetAnimals = "Pes a Mačka",
            toxicSubstance = "Žiadna",
            symptoms = "Šetriaca diéta.",
            emergencyAction = "Bez korenia a solí. Nikdy nepodávajte tepelne upravené kuracie kosti (štiepia sa na ostré ihlice)!",
            description = "Klasická prvá pomoc a diéta pri prechodnom zvracaní alebo miernej hnačke."
        )
    )

    fun calculateChocolateToxicity(
        dogWeightKg: Double,
        chocolateType: String,
        amountGrams: Double
    ): ChocolateToxicityResult {
        val theobrominePerGramMg = when (chocolateType) {
            "Biela čokoláda" -> 0.1
            "Mliečna čokoláda" -> 2.1
            "Horká čokoláda (50%)" -> 5.5
            "Tmavá horká čokoláda (70-85%)" -> 14.5
            "Čokoláda na varenie / Pekárska" -> 16.0
            "Kakaový prášok (čisté kakao)" -> 26.0
            else -> 2.1
        }

        val totalMg = amountGrams * theobrominePerGramMg
        val weight = if (dogWeightKg <= 0.0) 10.0 else dogWeightKg
        val dosePerKg = totalMg / weight

        val (severity, level, symptoms, action) = when {
            dosePerKg < 15.0 -> Quadruple(
                "Nízke riziko (Netoxická dávka)",
                ToxicityLevel.SAFE,
                "Možná mierna nevoľnosť alebo riedka stolica z vysokého obsahu tuku a cukru v čokoláde.",
                "Sledujte psa. Zabezpečte dostatok čerstvej pitnej vody. Ak je pes v poriadku, veterinár nie je nutný."
            )
            dosePerKg in 15.0..35.0 -> Quadruple(
                "Mierna toxicita",
                ToxicityLevel.MODERATE,
                "Zvýšený smäd, zvracanie, nepokoj, hnačka a plynatosť.",
                "Zavolajte veterinárovi. Ak k po požitiu došlo pred menej než 2 hodinami, môže odporučiť vyvolanie zvracania."
            )
            dosePerKg in 35.1..55.0 -> Quadruple(
                "Vážna toxicita (Kardiovaskulárne riziko)",
                ToxicityLevel.HIGH,
                "Zrýchlený a nepravidelný tep (tachykardia/arytmia), svalový tras, hyperaktivita, zrýchlené dýchanie.",
                "URGENTNE vyhľadajte veterinára. Pes potrebuje EKG monitorovanie, infúzie a podanie živočíšneho uhlia."
            )
            else -> Quadruple(
                "SMRTEĽNE NEBEZPEČNÁ DÁVKA!",
                ToxicityLevel.DEADLY,
                "Svalové kŕče, arytmia, vysoká horúčka, bezvedomie, zlyhanie srdca a dýchania.",
                "OKAMŽITE vyrazte na najbližšiu veterinárnu pohotovosť! Každá minúta rozhoduje. Oznámte klinike príchod vopred."
            )
        }

        return ChocolateToxicityResult(
            theobromineMgPerKg = Math.round(dosePerKg * 10.0) / 10.0,
            totalTheobromineMg = Math.round(totalMg * 10.0) / 10.0,
            severity = severity,
            severityLevel = level,
            symptomsExpected = symptoms,
            actionRequired = action
        )
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
