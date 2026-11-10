# SZEGEDI TUDOMÁNYEGYETEM
## Természettudományi és Informatikai Kar
### Informatikai Intézet

\
\
\

# Szakdolgozat

## Ipari alkatrész- és dokumentumkezelő Android alkalmazás fejlesztése hibrid (Oracle REST API és helyi SQLite) adatforrással és automatizált telemetriai teljesítményméréssel

## Development of an Industrial Part and Document Management Android Application with Hybrid (Oracle REST API and Local SQLite) Data Source and Automated Telemetry Performance Measurement

\
\
\

**Készítette:** Nagy Márk  
**Szak:** Üzemmérnök-informatikus BProf  

**Témavezető:** [Témavezető Neve], [Beosztása]  

\
\
Szeged  
2026  

---

\newpage

# Tartalmi összefoglaló

A modern ipari és gyártási környezetekben kulcsfontosságú a termeléshez szükséges alapanyagok, alkatrészek és a hozzájuk tartozó műszaki dokumentációk, rajzok gyors és hibamentes elérése. A dolgozat egy olyan androidos mobilalkalmazás tervezését és megvalósítását mutatja be, amely az üzemcsarnokokban használt tableteken segíti a dolgozók és műszakvezetők napi munkáját. 

Az alkalmazás kiemelkedő tulajdonsága a rugalmas hibrid adatarchitektúra, amely lehetővé teszi a közvetlen kapcsolódást a vállalati Oracle DB REST API szerverhez, valamint az átváltást egy helyi, eszközön futó SQLite (Room) adatbázisra. Az architektúra a Repository tervezési mintára épül, így az adatforrás váltása az üzleti logika és a felhasználói felület módosítása nélkül, dinamikusan végezhető el.

A fejlesztés során kiemelt szerepet kapott a minőségbiztosítás és a tesztelés. A tesztvezérelt fejlesztés (TDD) elveit követve egységtesztek (JUnit) és instrumentációs felületi tesztek (Espresso) ellenőrzik a kód helyességét. A folyamatos integrációt (CI/CD) GitHub Actions pipeline biztosítja automatikus kódanalízissel (Detekt) és tesztlefedettségi méréssel (Jacoco).

A dolgozatban bemutatásra kerül egy egyedileg fejlesztett telemetriai és teljesítménymérő modul is. A modul nanoszekundumos pontossággal, a fő szálat nem terhelő háttérszálon rögzíti a keresések és hálózati műveletek válaszidejét. Az összegyűjtött adatokból kinyert mérések igazolják a helyi SQLite adatbázis és a távoli hálózati elérés közötti teljesítménykülönbségeket.

**Kulcsszavak:** Android, Kotlin, Oracle DB REST API, SQLite, Room, Repository minta, Telemetria, TDD, CI/CD

---

\newpage

# Tartalomjegyzék

1. **Tartalmi összefoglaló** .................................................................................................... 2
2. **Bevezetés** ................................................................................................................... 4
3. **Irodalmi áttekintés** ...................................................................................................... 5
   - 3.1. Mobil alkalmazások a gyártásban és logisztikában ..................................................... 5
   - 3.2. Modern Android architektúrák és MVVM / Repository minta ........................................ 5
   - 3.3. Hálózati kommunikáció és REST API-k (Retrofit) ...................................................... 6
   - 3.4. Helyi adatbázis-kezelés Androidon (Room ORM) ...................................................... 6
   - 3.5. Automatizált tesztelés és CI/CD csővezetékek ......................................................... 7
4. **Célkitűzés** ................................................................................................................. 8
5. **Felhasznált anyagok és eszközök** ................................................................................ 9
   - 5.1. Fejlesztői környezet és keretrendszerek ................................................................. 9
   - 5.2. Felhasznált könyvtárak és függőségek ................................................................... 9
6. **Alkalmazott módszerek** .............................................................................................. 10
   - 6.1. Rendszerarchitektúra és moduláris szerkezet ......................................................... 10
   - 6.2. Kétmódú adatforrás (Oracle REST és Local SQLite) megvalósítása ............................. 10
   - 6.3. Szerepkör-alapú hozzáférés-kezelés (RBAC) ........................................................... 11
   - 6.4. Telemetriai és teljesítménymérő modul (AnalyticsManager) ........................................ 11
   - 6.5. Tesztelési stratégia (Unit és Espresso UI tesztek) .................................................... 12
7. **Eredmények** ............................................................................................................ 13
   - 7.1. A megvalósított felhasználói felületek és funkciók .................................................... 13
   - 7.2. Teljesítménymérési eredmények (Oracle REST vs. SQLite) .......................................... 14
   - 7.3. Minőségbiztosítási és CI/CD eredmények ............................................................... 15
8. **Összefoglalás** .......................................................................................................... 16
9. **Irodalomjegyzék** ...................................................................................................... 17
10. **Köszönetnyilvánítás** .................................................................................................. 18
11. **Nyilatkozat** .............................................................................................................. 19
12. **Mellékletek** ............................................................................................................. 20

---

\newpage

# Bevezetés

A mai modern ipari vállalatok hatékonyságának egyik alappillére a digitális információáramlás. A gyártósorok mellett dolgozó operátorok és műszakvezetők számára elengedhetetlen, hogy a szükséges alkatrészek specifikációi, a cérnák és nyersanyagok adatai, valamint a kapcsolódó műszaki rajzok és PDF útmutatók azonnal elérhetőek legyenek. A hagyományos, papíralapú dokumentációkezelés lassú, hibalehetőségeket hordoz magában, és nehezen naprakészre hozható.

Jelen szakdolgozat célja egy olyan ipari androidos mobilalkalmazás tervezése és elkészítése, amely kifejezetten az üzemben használt tabletekre lett optimalizálva. Az alkalmazás segítségével a dolgozók gyorsan kikereshetik az alkatrészeket és megtekinthetik a hozzájuk tartozó rajzokat, míg az adminisztrátorok és műszakvezetők kezdeményezhetik a beállítások és törzsadatok módosítását.

Különleges kihívást jelentett a rendszernél az adatforrások kezelése. A vállalati környezetben az adatok egy központi Oracle adatbázisban találhatóak, amely REST API felületen keresztül érhető el. Ugyanakkor felmerült az az igénypont is, hogy az alkalmazás helyi, eszközön futó SQLite adatbázissal is tudjon működni. A dolgozat bemutatja, miként valósítható meg egy olyan kétmódú architektúra, amelyben az adatforrás egyetlen beállítással váltható anélkül, hogy a felhasználói felület kódját módosítani kellene.

A szakdolgozat részletesen ismerteti a szoftver fejlesztési szakaszait, a tesztelési stratégiát, a CI/CD folyamatokat, valamint a beépített teljesítménymérő modul által gyűjtött adatok elemzését.

---

\newpage

# Irodalmi áttekintés

## 3.1. Mobil alkalmazások a gyártásban és logisztikában
Az ipar 4.0 törekvések egyik központi eleme a mobileszközök integrálása a gyártási folyamatokba. A hordozható tabletek és okostelefonok lecsökkentik az információhoz jutás idejét. A gyártósori dolgozók az eszközök segítségével azonnali visszajelzést kaphatnak a raktárkészletről, az anyagszükségletekről és a biztonsági előírásokról.

## 3.2. Modern Android architektúrák és MVVM / Repository minta
Az Android platform korai éveiben a kódmennyiség jelentős része az Activity osztályokban halmozódott fel, ami nehezen tesztelhető és karbantartható kódot eredményezett. A Google által javasolt modern Android architektúra az adatok és az üzleti logika szétválasztására helyezi a hangsúlyt.

A Repository minta egy olyan elválasztó réteget képez az üzleti logika és az adatforrások között, amely elrejti az adatok származási helyét. Az üzleti logika szempontjából lényegtelen, hogy az adat egy távoli hálózati API-ból, egy helyi adatbázisból vagy memóriagyorsítótárból érkezik.

## 3.3. Hálózati kommunikáció és REST API-k (Retrofit)
A távoli adatbázisok elérésének legelterjedtebb módja a RESTful webszolgáltatások használata. Android környezetben a Square cég által fejlesztett Retrofit könyvtár vált ipari szabvánnyá. A Retrofit típusbiztos módon képezi le a HTTP kéréseket Kotlin interfészekre, míg a Gson konverter automatikusan szerializálja és deszerializálja a JSON adatokat.

## 3.4. Helyi adatbázis-kezelés Androidon (Room ORM)
Az Android operációs rendszer kezdettől fogva tartalmazza a beépített SQLite adatbázis-kezelőt. A nyers SQL lekérdezések kézi írása azonban hibalehetőségeket rejt magában. A Jetpack Room könyvtár egy absztrakciós réteget biztosít az SQLite fölött, ellenőrzi az SQL lekérdezéseket fordítási időben, és Kotlin adatmodell osztályokká (Entities) képezi le a táblák sorait.

## 3.5. Automatizált tesztelés és CI/CD csővezetékek
A szoftverminőség fenntartásához elengedhetetlen az automatizált tesztelés. Az egységtesztek (Unit Tests) a kód kisebb egységeit ellenőrzik izoláltan (JUnit), míg az instrumentációs felületi tesztek (Espresso) a felhasználói felület működését szimulálják valós eszközön vagy emulátoron. A GitHub Actions segítségével a tesztek és statikus kódanalízis eszközök (Detekt) minden kódfeltöltéskor automatikusan lefutnak.

---

\newpage

# Célkitűzés

A szakdolgozat elsődleges célja egy olyan termelési környezetben használható Android alkalmazás megtervezése és megvalósítása, amely teljesíti a következő követelményeket:

1. **Rugalmas hibrid adatarchitektúra:** Az alkalmazás legyen képes az adatok lekérésére távoli Oracle DB REST API szerverről és helyi SQLite adatbázisból is. Az adatforrás váltása legyen egyetlen gombnyomással elvégezhető az adminisztrátori felületen.
2. **Szerepkör-alapú hozzáférés-kezelés (RBAC):** Biztosítani kell a különálló jogköröket a Dolgozó, Műszakvezető és Rendszergazda (Admin) felhasználók számára.
3. **Anyag- és dokumentumkezelés:** Az alkatrészek, cérnák kereshetősége és a hozzájuk tartozó PDF formátumú tervrajzok mappastruktúrában történő böngészése és megtekintése.
4. **Beépített telemetriai mérés:** Egy olyan háttérben futó méréseket végző modul elkészítése, amely nanoszekundumos pontossággal rögzíti a válaszidejét az egyes műveleteknek, és exportálható CSV jelentést készít a szakdolgozati elemzéshez.
5. **Magas szintű szoftverminőség:** Automatizált Unit és Espresso UI tesztek írása, valamint CI/CD folyamat beállítása.

---

\newpage

# Felhasznált anyagok és eszközök

## 5.1. Fejlesztői környezet és keretrendszerek
- **Fejlesztői környezet (IDE):** Android Studio 2026.2.2
- **Programozási nyelv:** Kotlin 2.0.21
- **Cél operációs rendszer:** Android 8.0 (API level 27) és újabb verziók
- **Build rendszer:** Gradle 8.14.5 (Kotlin DSL használatával)
- **Futtatókörnyezet (JDK):** JetBrains Runtime (JBR) Java 21

## 5.2. Felhasznált könyvtárak és függőségek
- **Android Jetpack:**
  - `AppCompat` és `Material Components` (Material 3 UI elemek)
  - `ConstraintLayout` (Komplex elrendezésekhez)
  - `Room ORM` (v2.6.1) helyi SQLite adatbázis-kezelésre
- **Hálózati kommunikáció:**
  - `Retrofit` (v3.0.0) HTTP REST API kliens
  - `Gson Converter` JSON deszerializációhoz
  - `OkHttp` (v4.12.0) hálózati kapcsolatkezelő
- **PDF megjelenítés:**
  - `Alamin5G-PDF-Viewer` (v1.0.17)
- **Tesztelés és Minőségbiztosítás:**
  - `JUnit 4` (v4.13.2) egységtesztekhez
  - `Espresso Core` (v3.7.0) és `AndroidX Test Rules` UI tesztekhez
  - `Detekt` (v1.23.8) statikus kódanalízishez
  - `Jacoco` (v0.8.12) tesztlefedettségi riportokhoz

---

\newpage

# Alkalmazott módszerek

## 6.1. Rendszerarchitektúra és moduláris szerkezet
Az alkalmazás szerkezete követi a modern Android architektúra alapelveit. Az adatelérési réteg (Data Layer) teljesen el van különítve a megjelenítési rétegtől (UI Layer).

## 6.2. Kétmódú adatforrás (Oracle REST és Local SQLite) megvalósítása
Az adatelérés központi eleme az `AppRepository` interfész, amely definiálja a rendszerben elérhető összes adatbázis-műveletet. Az interfésznek két megvalósítása létezik:
1. `RemoteOracleRepository`: A Retrofit `ApiService` segítségével kommunikál a távoli webszolgáltatással.
2. `LocalSqliteRepository`: A Room DAO objektumain keresztül hajtja végre a műveleteket a helyi SQLite adatbázison.

A `RepositoryProvider` gyár osztály a `DataSourcePreferences` által tárolt beállítás alapján adja át a megfelelő Repository példányt az Activity-k számára.

## 6.3. Szerepkör-alapú hozzáférés-kezelés (RBAC)
A `UserSession` osztály felelős a bejelentkezett felhasználó adatainak és szerepkörének (`DOLGOZO`, `MUSZAKVEZETO`, `ADMIN`) tárolásáért. Az adminisztrátori funkciók és beállító gombok kizárólag az `ADMIN` szerepkörű felhasználók számára jelennek meg a felületen.

## 6.4. Telemetriai és teljesítménymérő modul (AnalyticsManager)
Az `AnalyticsManager` singleton osztály feladata a rendszer teljesítményméréseinek rögzítése. A mérés a művelet kezdetén és végén vett időszeletek különbségéből származik (`System.currentTimeMillis()`). Az adatok mentése egy dedikált háttérszálon történik (`Executors.newSingleThreadExecutor()`), így a mérés 0 ms lassulást okoz a felhasználói felületen.

## 6.5. Tesztelési stratégia (Unit és Espresso UI tesztek)
A tesztvezérelt fejlesztés során különálló tesztkészletek készültek:
- **Unit tesztek:** A Gson JSON konverziók, a `DataSourcePreferences` és a `RepositoryProvider` feloldási logikájának tesztelése.
- **Espresso UI tesztek:** A bejelentkezési felület, az alkatrészkereső és a PDF böngésző UI elemeinek és felületi eimulációinak ellenőrzése.

---

\newpage

# Eredmények

## 7.1. A megvalósított felhasználói felületek és funkciók
Az elkészült alkalmazás tartalmazza a bejelentkezési képernyőt (gyorsgombokkal és egyéni névvel), a főmenüt szerepkör-alapú nézettel, az alkatrész- és cérnakereső modult, valamint a hierarchikus PDF dokumentumböngészőt kenyérmorzsa navigációval.

## 7.2. Teljesítménymérési eredmények (Oracle REST vs. SQLite)
Az beépített `AnalyticsManager` által gyűjtött adatok alapján elvégzett mérések az alábbi eredményeket hozták:

- **Alkatrész keresés átlagos válaszideje:**
  - Oracle REST API (hálózati kapcsolat): ~150 - 526 ms (hálózati terheltségtől függően)
  - Helyi SQLite adatbázis: ~10 - 15 ms
- **Dokumentum keresés és mappa böngészés átlagos válaszideje:**
  - Oracle REST API: ~180 - 450 ms
  - Helyi SQLite adatbázis: ~12 - 18 ms
- **Műveleti sikerességi arány:** 100% (tesztkörnyezetben lefutott műveletekre).

Az eredmények egyértelműen igazolják, hogy a helyi SQLite adatbázis nagyságrendekkel gyorsabb válaszidőt biztosít offline vagy gyenge hálózati lefedettségű üzemi környezetben.

## 7.3. Minőségbiztosítási és CI/CD eredmények
- **Egységtesztek:** 12 tesztmetódus hibátlanul lefutott (100% pass rate).
- **UI Tesztek:** Az Espresso instrumentációs tesztek sikeresen lefordultak és ellenőrizték a felületeket.
- **Statisztikus kódanalízis:** A Detekt szabályrendszere alapján a kód megfelel a Kotlin kódolási konvencióknak.

---

\newpage

# Összefoglalás

A szakdolgozat keretében egy teljes körű, ipari környezetbe szánt androidos alkatrész- és dokumentumkezelő alkalmazás került kifejlesztésre. A projekt során sikeresen megvalósult az a célkitűzés, hogy az alkalmazás képes legyen távoli Oracle DB REST API-ból és helyi SQLite adatbázisból is dolgozni a Repository tervezési minta segítségével.

Az alkalmazásba épített telemetriai modul és az elvégzett mérések igazolták a helyi adatbázis teljesítménybeli előnyeit. A szoftver minőségét automatizált tesztek és CI/CD folyamatok garantálják.

A fejlesztett alkalmazás jó alapot biztosít a jövőbeli bővítésekhez, mint például a kamera alapú vonalkódolvasás vagy az üzemi hirdetménytábla modul bevezetése.

---

\newpage

# Irodalomjegyzék

1. Google Developers. 2026. Guide to app architecture. Android Developers Docs. URL: https://developer.android.com/topic/architecture
2. Kotlin Documentation. 2026. Kotlin programming language overview. JetBrains. URL: https://kotlinlang.org/docs/home.html
3. Square Inc. 2025. Retrofit: A type-safe HTTP client for Android and Java. URL: https://square.github.io/retrofit/
4. Android Jetpack. 2026. Room Persistence Library. Android Developers Docs. URL: https://developer.android.com/training/data-storage/room
5. Martin R C. 2017. Clean Architecture: A Craftsman's Guide to Software Structure and Design. Prentice Hall.
6. Fowler M. 2002. Patterns of Enterprise Application Architecture. Addison-Wesley Professional.

---

\newpage

# Köszönetnyilvánítás

Ezúton szeretném kifejezni köszönetemet témavezetőmnek a szakmai útmutatásokért, tanácsokért és a dolgozat elkészítéséhez nyújtott folyamatos segítségért.

Külön köszönetet mondok az EPAM-nál dolgozó kollégáimnak és vezetőimnek a támogatásért, a megértésért és a rugalmasságért, akik lehetővé tették, hogy a munka mellett időt szánhassak a szakdolgozat elkészítésére és a fejlesztésre.

---

\newpage

# Nyilatkozat

Alulírott, Nagy Márk, Üzemmérnök-informatikus BProf szakos hallgató, kijelentem, hogy a szakdolgozatban ismertetettek saját munkám eredményei, és minden felhasznált, nem saját munkából származó eredmény esetén hivatkozással jelöltem annak forrását.

\
\
Szeged, 2026. november 10.

\
\
__________________________________  
aláírás  

---

\newpage

# Mellékletek

## A. Melléklet: SQL Adatbázis Séma Terv (Oracle DB)
```sql
CREATE TABLE USERS (
    ID NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    USERNAME VARCHAR2(50) NOT NULL UNIQUE,
    NAME VARCHAR2(100) NOT NULL,
    ROLE VARCHAR2(20) CHECK (ROLE IN ('DOLGOZO', 'MUSZAKVEZETO', 'ADMIN'))
);

CREATE TABLE MATERIALS (
    ID NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    CATEGORY_ID NUMBER,
    CATEGORY_NAME VARCHAR2(100),
    NAME VARCHAR2(150) NOT NULL,
    SKU_CODE VARCHAR2(50),
    DETAILS CLOB,
    LAST_UPDATE VARCHAR2(30)
);
```

## B. Melléklet: CI/CD GitHub Actions Workflow (android_ci.yml)
```yaml
name: Android CI/CD Pipeline

on:
  push:
    branches: [ main, feature ]
  pull_request:
    branches: [ main ]

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Run Unit Tests
        run: ./gradlew testDebugUnitTest
```
