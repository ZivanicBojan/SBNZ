# SBNZ News Analysis System

Sistem za analizu pouzdanosti vijesti koji koristi **Forward Chaining**, **Backward Chaining** i **Complex Event Processing (CEP)**.

## Pokretanje Aplikacije

### Backend (Spring Boot + Drools)

1. **Navigiraj do service foldera:**

   ```bash
   cd "\Bojan_Zivanic_SV61_2021_GRUPA4\sbnz-integracija-projekta"
   ```
2. **Kompajliraj kjar modul:**

   ```bash
   cd kjar
   mvn clean install
   cd ..
   ```
3. **Kompajliraj model modul:**

   ```bash
   cd model
   mvn clean install
   cd ..
   ```
4. **Pokreni service:**

   ```bash
   cd service
   mvn spring-boot:run
   ```

   Ili alternativno:

   ```bash
   mvn clean compile
   mvn exec:java -Dexec.mainClass="com.ftn.sbnz.service.ServiceApplication"
   ```
5. **Backend će biti dostupan na:** `http://localhost:8080`

### Frontend (HTML/CSS/JavaScript)

1. **Otvori frontend folder:**

   ```bash
   cd "\Bojan_Zivanic_SV61_2021_GRUPA4\sbnz-integracija-projekta\frontend"
   ```
2. **Pokreni HTTP server:**

   **Opcija 1 - Python:**

   ```bash
   python -m http.server 3000
   ```

   **Opcija 2 - Node.js:**

   ```bash
   npx http-server -p 3000
   ```

   **Opcija 3 - Live Server (VS Code extension)**

   - Instaliraj "Live Server" ekstenziju u VS Code
   - Desni klik na `index.html` → "Open with Live Server"
3. **Frontend će biti dostupan na:** `http://localhost:3000`

## API Endpoints

### Forward Chaining

- `GET /api/forward-chaining/demo` - Demo sa test podacima
- `POST /api/forward-chaining/classify` - Klasifikuj listu vijesti
- `POST /api/forward-chaining/classify-single` - Klasifikuj jednu vijest
- `POST /api/forward-chaining/analyze` - Analiziraj sa statistikama

### Backward Chaining

- `GET /api/backward-chaining/demo` - Demo sa test podacima
- `POST /api/backward-chaining/analyze-suspicious` - Analiziraj sumnjive vijesti
- `POST /api/backward-chaining/analyze-trusted` - Analiziraj pouzdane vijesti
- `POST /api/backward-chaining/full-analysis` - Kompleksna analiza

### CEP (Complex Event Processing)

- `GET /api/cep/demo` - Demo sa normalnim/eksplozivnim širenjem
- `GET /api/cep/demo-coordinated` - Demo koordinisanog širenja
- `POST /api/cep/analyze-spread` - Analiziraj pattern širenja
- `POST /api/cep/simulate-spread` - Simuliraj širenje vijesti

## Kako Sistem Funkcioniše

### Forward Chaining

- **Cilj:** Klasifikacija vijesti na osnovu činjenica
- **Proces:** Činjenice → Pravila → Zaključci
- **Kategorije:** POUZDANA, POTENCIJALNO_LAZNA, SUMNJIVA

### Backward Chaining

- **Cilj:** Objašnjava ZAŠTO je vijest klasifikovana na određeni način
- **Proces:** Cilj → Pretraga razloga → Objašnjenje
- **Koristi:** Queries za analizu razloga
- **Prikaz rezultata:** Prikazuje vijest i, ako postoji, originalnu vijest iz trusted lanca (rekurzivno kroz repostove).

### CEP (Complex Event Processing)

- **Cilj:** Analizira patterns širenja vijesti kroz vrijeme
- **Process:** SpreadEvents → Temporal patterns → DistributionType
- **Tipovi:** NORMAL, COORDINATED, EXPLOSIVE

## Test Scenariji

### 1. Forward Chaining Test

```javascript
// Pouzdan izvor + normalan naslov = POUZDANA
{
  "title": "Ekonomska analiza za Q3",
  "source": {"name": "BBC", "reputation": "TRUSTED"},
  "distributionType": "NORMAL"
}

// Nepouzdan izvor + senzacionalizam = SUMNJIVA  
{
  "title": "ŠOK!!! Skandal u vladi!!!",
  "source": {"name": "FakeNews24", "reputation": "UNRELIABLE"},
  "distributionType": "COORDINATED"
}
```

### 2. Backward Chaining Test

- Analizira klasifikovane vijesti i objašnjava razloge
- Koristi queries za pronalaženje objašnjenja
- Postavlja `explanation` field sa detaljnim razlogom

### 3. CEP Test

- Normalno širenje: 3-5 evenata u normalnim intervalima
- Koordinisano: Mnogo izvora u kratkom vremenskom okviru
- Eksplozivno: Veliki broj evenata u vrlo kratkom vremenu

## Tehnologije

- **Backend:** Spring Boot, Drools, Maven
- **Frontend:** HTML5, CSS3, Vanilla JavaScript
- **Reasoning:** Forward/Backward Chaining, CEP
- **API:** RESTful services sa JSON

## Struktura Projekta

```
sbnz-integracija-projekta/
├── kjar/                   # Drools pravila i kmodule
│   ├── src/main/resources/
│   │   ├── rules/
│   │   │   ├── forward.drl
│   │   │   └── backward/
│   │   │       └── backward.drl
│   │   └── META-INF/
│   │       └── kmodule.xml
├── model/                  # Java modeli (News, Source, itd.)
├── service/                # Spring Boot aplikacija
│   ├── src/main/java/com/ftn/sbnz/service/
│   │   ├── ForwardChainingService.java
│   │   ├── BackwardChainingService.java
│   │   ├── CEPService.java
│   │   └── kontroleri...
└── frontend/               # Klijentska aplikacija
    ├── index.html
    ├── style.css
    └── script.js
```

## Frontend Funkcionalnosti

- **Tabbed Interface** - Forward, Backward, CEP
- **Demo Buttons** - Brzi testovi sa predefinisanim podacima
- **Real-time Results** - Dinamički prikaz rezultata analize
