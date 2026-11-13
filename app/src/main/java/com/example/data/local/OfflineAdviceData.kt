package com.example.data.local

data class OfflineGuideItem(
    val id: Long,
    val category: String,
    val title: String,
    val badgeLabel: String,
    val urgencyHint: String,
    val summary: String,
    val steps: List<String>,
    val avoidList: List<String> = emptyList()
)

object OfflineAdviceData {

    val categories = listOf(
        "Všetky",
        "Prvá pomoc",
        "Parazity",
        "Očkovanie",
        "Výživa",
        "Cestovanie"
    )

    val guides: List<OfflineGuideItem> = listOf(
        OfflineGuideItem(
            id = 1L,
            category = "Prvá pomoc",
            title = "Požitie čokolády, hrozna alebo xylitolu",
            badgeLabel = "AKÚTNE • TOXIKOLÓGIA",
            urgencyHint = "Konajte do 30–60 minút a volajte pohotovosť.",
            summary = "Teobromín v čokoláde, hrozienka a umelé sladidlo xylitol patria medzi najčastejšie akútne otravy u psov a mačiek.",
            steps = listOf(
                "Odstráňte zvyšky potraviny z dosahu zvieratka a odložte obal (kvôli zloženiu a % kakaa).",
                "Zapíšte si odhadovanú hmotnosť zvieratka, čas požitia a množstvo.",
                "Ihneď zavolajte na veterinárnu pohotovosť – prípadný výplach alebo živočíšne uhlie má najväčší účinok do 1 hodiny.",
                "Pri bezvedomí alebo kŕčoch zvieratko uložte na bok s natiahnutým krkom a zabezpečte okamžitý prevoz."
            ),
            avoidList = listOf(
                "Nevyvolávajte zvracanie bez pokynu veterinára (najmä pri ospalosti, kŕčoch alebo požití žieravín).",
                "Nepodávajte mlieko ani ľudské lieky proti bolesti (paracetamol a ibuprofén sú pre mačky a psy toxické)."
            )
        ),
        OfflineGuideItem(
            id = 2L,
            category = "Prvá pomoc",
            title = "Prehriatie a úpal v letných mesiacoch",
            badgeLabel = "AKÚTNE • TEPLOTA",
            urgencyHint = "Okamžité postupné ochladzovanie a odvoz k lekárovi.",
            summary = "Psy a mačky sa ochladzujú takmer výlučne dychčaním. Krátkolebé plemená (mops, buldoček, perzská mačka) kolabujú už pri 25 °C.",
            steps = listOf(
                "Okamžite preneste zviera do tieňa alebo klimatizovanej miestnosti.",
                "Postupne ochladzujte labky, brucho a triesla vlažnými (nie ľadovými) mokrými uterákmi.",
                "Ponúknite malé dúšky vlažnej vody, ak je zviera pri vedomí a dokáže prehĺtať.",
                "Sledujte farbu ďasien – tehlovočervené, fialové alebo veľmi bledé ďasná vyžadujú infúznu liečbu na klinike."
            ),
            avoidList = listOf(
                "Neoblievajte zviera ľadovou vodou – prudké stiahnutie ciev bráni úniku tepla z jadra tela.",
                "Nenechávajte zviera v aute ani 'na 5 minút', vnútri teplota stúpa nad 50 °C za pár minút."
            )
        ),
        OfflineGuideItem(
            id = 3L,
            category = "Prvá pomoc",
            title = "Torzia (pretočenie) žalúdka u väčších psov",
            badgeLabel = "KRITICKÉ • MINÚTY ROZHODUJÚ",
            urgencyHint = "Nestrácajte čas – okamžitý chirurgický zákrok.",
            summary = "Život ohrozujúci stav typický pre veľké a hlbokohrudné plemená po kŕmení alebo záťaži.",
            steps = listOf(
                "Sledujte typické varovné príznaky: napínanie na zvracanie bez vyvrátenia obsahu, nepokoj a tvrdé nafúknuté brucho.",
                "Všimnite si nadmerné slinenie, zrýchlený dych a slabosť alebo kolaps.",
                "Okamžite telefonujte na najbližšiu 24/7 kliniku s chirurgickou sálou, aby bol tím pripravený pred príchodom."
            ),
            avoidList = listOf(
                "Nečakajte do rána – neliečená torzia vedie k nekróze žalúdka a šoku v priebehu hodín.",
                "Nedávajte psovi piť ani jesť a netlačte na brucho."
            )
        ),
        OfflineGuideItem(
            id = 4L,
            category = "Prvá pomoc",
            title = "Bodnutie hmyzom alebo uštipnutie vretenicou",
            badgeLabel = "ALERGIA • OPUCH",
            urgencyHint = "Sledujte dýchanie a opuch okolo ňufáka a krku.",
            summary = "Väčšina bodnutí včelou či osou spôsobí len lokálny opuch, no opuch dýchacích ciest alebo anafylaxia vyžadujú zásah veterinára.",
            steps = listOf(
                "Ak je viditeľné žihadlo, opatrne ho zoškrabnite nechtom alebo kartou (nestláčajte jedový vačok pinzetou).",
                "Priložte studený obklad zabalený v látke na 10 minút na zmiernenie opuchu.",
                "Ak zviera zvracia, je apatické, opúcha mu hlava alebo chrapotavo dýcha, okamžite vyhľadajte kliniku.",
                "Pri podozrení na hadie uštipnutie zviera preneste v náručí (obmedzte pohyb, aby sa jed nešíril)."
            ),
            avoidList = listOf(
                "Nepodávajte ľudské antihistaminiká alebo kortikoidy bez telefonickej konzultácie dávky s veterinárom.",
                "Nikdy nenarezávajte ranu a nevysávajte jed."
            )
        ),
        OfflineGuideItem(
            id = 5L,
            category = "Parazity",
            title = "Správne vybratie kliešťa a varovné príznaky babeziózy",
            badgeLabel = "PREVENCIA • SEZÓNA",
            urgencyHint = "Na Slovensku je babezióza častá od jari do neskorej jesene.",
            summary = "Rýchle odstránenie prisatého kliešťa znižuje riziko prenosu babeziózy, anaplazmózy a lymskej boreliózy.",
            steps = listOf(
                "Uchopte kliešťa špeciálnym háčikom alebo tenkou pinzetou čo najbližšie pri koži.",
                "Plynulým ťahom kolmo od kože kliešťa vytiahnite (háčik môžete jemne pootočiť bez drvenia tela kliešťa).",
                "Miesto dezinfikujte veterinárnou alebo bežnou kožnou dezinfekciou (napr. roztokom povidón-jódu).",
                "Nasledujúce 2–3 týždne sledujte apetít, energiu, farbu moču (tmavý/hnedočervený) a horúčku."
            ),
            avoidList = listOf(
                "Nekvapkajte na kliešťa olej, krém ani alkohol – pridusený kliešť vyvrhne obsah čriev priamo do rany.",
                "Pri strate chuti do jedla alebo tmavom moči nečakajte – babezióza sa zhoršuje rýchlo."
            )
        ),
        OfflineGuideItem(
            id = 6L,
            category = "Parazity",
            title = "Odčervenie šteniat, mačiat a dospelých zvierat",
            badgeLabel = "HARMONOGRAM • ENDOPARAZITY",
            urgencyHint = "Dávku vždy určujte podľa aktuálnej váhy v kg.",
            summary = "Pravidelné odčervenie chráni nielen zviera pred škrkavkami a pásomnicami, ale aj celú rodinu.",
            steps = listOf(
                "Šteniatka a mačiatka sa zvyčajne odčervujú od 2.–3. týždňa každé 2 týždne až do veku 3 mesiacov.",
                "Od 3 do 6 mesiacov odčervujte raz mesačne podľa odporúčania veterinára.",
                "Dospelé psy a vonkajšie mačky odčervujte každé 3 mesiace (prípadne po koprologickom vyšetrení trusu).",
                "Ak pes loví hlodavce alebo žerie surové mäso, konzultujte s veterinárom častejšie podávanie prípravkov."
            ),
            avoidList = listOf(
                "Nikdy nepoužívajte psie antiparazitiká s permetrínom na mačky – sú pre mačky smrteľne jedovaté!",
                "Nedávkujte tabletu 'od oka', poddávkovanie neúčinkuje a vytvára rezistenciu."
            )
        ),
        OfflineGuideItem(
            id = 7L,
            category = "Očkovanie",
            title = "Základná vakcinačná schéma (DHPPi + L4 / RCP)",
            badgeLabel = "IMUNITA • ZÁKONNÉ POVINNOSTI",
            urgencyHint = "Besnota je na Slovensku povinná zo zákona pre psy aj pri cestovaní.",
            summary = "Prehľad základných infekčných ochorení, proti ktorým sa psy a mačky štandardne očkujú na Slovensku.",
            steps = listOf(
                "Psy – DHPPi: psinka, infekčný zápal pečene, parvoviróza a parainfluenza (základná séria v 6.–16. týždni, neskôr preočkovanie).",
                "Psy – L4 (Leptospiróza): veľmi dôležitá ochrana proti baktériám z kaluží, potokov a hlodavcov – očkuje sa každých 12 mesiacov.",
                "Besnota (R): prvé očkovanie zvyčajne od 12. týždňa veku, ďalšie preočkovanie podľa typu vakcíny (1–3 roky).",
                "Mačky – RCP: panleukopénia (mačací mor), kaliciviróza a herpesvírusová rinotracheitída; pri vonkajších mačkách zvážte aj FeLV."
            ),
            avoidList = listOf(
                "Neoškujte zviera s horúčkou, hnačkou alebo počas akútnej infekcie.",
                "Nezabúdajte pred cestou do zahraničia skontrolovať platnosť besnoty minimálne 21 dní pred odchodom."
            )
        ),
        OfflineGuideItem(
            id = 8L,
            category = "Výživa",
            title = "Bezpečný prechod na nové krmivo a riešenie ľahkej hnačky",
            badgeLabel = "TRÁVENIE • DIÉTA",
            urgencyHint = "Krv v stolici alebo apatia znamená návštevu veterinára.",
            summary = "Náhlou zmenou granúl často vzniká akútne podráždenie čriev. Postupný prechod šetrí mikrobióm.",
            steps = listOf(
                "Nové krmivo primiešavajte postupne 7 dní: dni 1–2 (25 % nového), dni 3–4 (50 %), dni 5–6 (75 %), deň 7 (100 %).",
                "Pri nekomplikovanej jednorazovej mäkkej stolici dospelého psa pomôžu menšie porcie vareného kuracieho mäsa bez kože a kostí s rozvarenou ryžou a veterinárne probiotiká.",
                "Vždy zabezpečte nepretržitý prístup k čerstvej vode."
            ),
            avoidList = listOf(
                "Šteňatá, mačiatka a malé plemená nenechávajte hladovať – hrozí im rýchly pokles cukru v krvi a dehydratácia.",
                "Nedávajte psom ani mačkám varené hydinové či bravčové kosti (štiepia sa a môžu prederaviť črevo)."
            )
        ),
        OfflineGuideItem(
            id = 9L,
            category = "Cestovanie",
            title = "Cestovanie so zvieraťom po EÚ (Pas, čip a doklady)",
            badgeLabel = "LEGISLATÍVA • DOVOLENKA",
            urgencyHint = "Kontrolujte špecifické pravidlá cieľovej krajiny vopred.",
            summary = "Každý pes, mačka alebo fretka cestujúca mimo Slovenska musí spĺňať jednotné veterinárne požiadavky EÚ.",
            steps = listOf(
                "Funkčný mikročip (ISO norma 11784/11785) aplikovaný PRED prvým platným očkovaním proti besnote.",
                "Modrý európsky pas spoločenského zvieraťa (Pet Passport) vydaný oprávneným veterinárom.",
                "Platná vakcinácia proti besnote zapísaná v pase (pri prvom očkovaní platí najskôr po 21 dňoch).",
                "Pri ceste do Veľkej Británie, Írska, Fínska, Malty alebo Nórska sa vyžaduje veterinárom potvrdené odčervenie proti pásomnici Echinococcus (24–120 hodín pred vstupom)."
            ),
            avoidList = listOf(
                "Necestujte mimo SR s čerstvo zaočkovaným šteňaťom pred uplynutím 21-dňovej lehoty."
            )
        )
    )
}
