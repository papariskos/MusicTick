# 🎫 MusicTick — Neon Rhythm Ticketing Platform

Ολοκληρωμένο σύστημα αγοράς και διαχείρισης εισιτηρίων συναυλιών, βασισμένο σε **Java + JavaFX + MySQL**. Υποστηρίζει ρόλους Πελάτη, Διοργανωτή και Διαχειριστή (Admin), με πλήρες offline fallback μέσω τοπικών αρχείων `.txt`.

---

## 📋 Περιεχόμενα

1. [Προαπαιτούμενα](#1-προαπαιτούμενα)
2. [Δομή Project](#2-δομή-project)
3. [Εγκατάσταση Βάσης Δεδομένων (MySQL)](#3-εγκατάσταση-βάσης-δεδομένων-mysql)
4. [Εκτέλεση Εφαρμογής](#4-εκτέλεση-εφαρμογής)
5. [Διαπιστευτήρια Χρηστών (Test Credentials)](#5-διαπιστευτήρια-χρηστών-test-credentials)
6. [Εκτέλεση Automated Tests](#6-εκτέλεση-automated-tests)
7. [Offline Mode (Χωρίς Βάση)](#7-offline-mode-χωρίς-βάση)
8. [Βασικά Χαρακτηριστικά](#8-βασικά-χαρακτηριστικά)

---

## 1. Προαπαιτούμενα

Βεβαιωθείτε ότι έχετε εγκαταστήσει τα παρακάτω πριν ξεκινήσετε:

| Εργαλείο | Ελάχιστη Έκδοση | Λήψη |
|---|---|---|
| **Java JDK** | 17+ | https://www.oracle.com/java/technologies/downloads/ |
| **MySQL Server** | 8.0+ | https://dev.mysql.com/downloads/mysql/ |
| **JavaFX SDK** | 17+ | Ήδη ενσωματωμένο στο `libs/javafx-lib/` |
| **MySQL Connector** | — | Ήδη ενσωματωμένο στο `libs/mysql-connector-java.jar` |

> **Σημείωση:** Δεν χρειάζεται να κατεβάσετε JavaFX ή MySQL Connector ξεχωριστά — βρίσκονται ήδη στον φάκελο `libs/`.

---

## 2. Δομή Project

```
MusicTick/
├── src/
│   ├── com/musictick/          # Κύριος κώδικας Java
│   │   ├── controller/         # JavaFX Controllers (UI logic)
│   │   ├── dao/                # Data Access Objects (DB queries)
│   │   ├── manager/            # Business Logic Managers
│   │   ├── Main.java           # Entry point της εφαρμογής
│   │   └── MusicTickTestRunner.java  # Automated test suite
│   ├── database/
│   │   └── musictick.sql       # Script δημιουργίας & αρχικοποίησης DB
│   └── *.fxml                  # UI layout αρχεία (JavaFX)
├── libs/
│   ├── javafx-lib/             # JavaFX JARs
│   └── mysql-connector-java.jar
├── approved_concerts.txt       # Offline fallback: εγκεκριμένες συναυλίες
├── pending_concerts.txt        # Offline fallback: εκκρεμείς συναυλίες
├── forum_reports.txt           # Offline fallback: αναφορές forum
├── forum_posts_mock.txt        # Offline fallback: δημοσιεύσεις forum
├── purchased_tickets.txt       # Offline fallback: εισιτήρια
├── alerts.txt                  # Offline fallback: ειδοποιήσεις
└── run.sh                      # Script εκτέλεσης
```

---

## 3. Εγκατάσταση Βάσης Δεδομένων (MySQL)

### Βήμα 1: Ξεκινήστε τον MySQL Server

**macOS (Homebrew):**
```bash
brew services start mysql
```

**macOS (MySQL.app ή installer):**
Ανοίξτε το MySQL Workbench ή εκκινήστε τον server από τις Προτιμήσεις Συστήματος.

**Linux:**
```bash
sudo systemctl start mysql
```

### Βήμα 2: Εκτελέστε το SQL Script

Ανοίξτε τερματικό (Terminal) και τρέξτε:

```bash
cd /Users/alex/Desktop/CEID/MusicTick
mysql -u root -p < src/database/musictick.sql
```

> Πατήστε **Enter** αν ο root δεν έχει κωδικό (προεπιλογή για τοπική ανάπτυξη).

Το script:
- Δημιουργεί τη βάση δεδομένων `musictick`
- Δημιουργεί όλους τους πίνακες (users, concerts, tickets, orders, forum_posts, κ.α.)
- Εισάγει αρχικά δεδομένα: 3 Venues, 3 Concerts (APPROVED), 3 Users, Ticket Types και Forum Posts

### Βήμα 3: Επαλήθευση (προαιρετικό)

```bash
mysql -u root -p musictick -e "SELECT user_id, email, role FROM users;"
```

Αναμενόμενο αποτέλεσμα:
```
+---------+---------------------------+----------+
| user_id | email                     | role     |
+---------+---------------------------+----------+
|       1 | user@musictick.com        | CUSTOMER |
|       2 | organizer@musictick.com   | ORGANIZER|
|       3 | admin@musictick.com       | ADMIN    |
+---------+---------------------------+----------+
```

> **Σύνδεση DB:** Η εφαρμογή συνδέεται αυτόματα στο `jdbc:mysql://localhost:3306/musictick` με username `root` και κενό κωδικό. Αν έχετε διαφορετικό κωδικό root, αλλάξτε το στο αρχείο `src/com/musictick/dao/BookingDAO.java` (ή στο αντίστοιχο DAO).

---

## 4. Εκτέλεση Εφαρμογής

Από τον κεντρικό φάκελο του project, τρέξτε:

```bash
cd /Users/alex/Desktop/CEID/MusicTick
./run.sh
```

> Αν εμφανιστεί σφάλμα δικαιωμάτων, πρώτα κάντε:
> ```bash
> chmod +x run.sh
> ./run.sh
> ```

Το `run.sh` κάνει αυτόματα:
1. **Μεταγλώττιση (compile)** όλων των `.java` αρχείων στον φάκελο `out/`
2. **Αντιγραφή** των FXML αρχείων UI στον `out/`
3. **Εκκίνηση** της εφαρμογής από την κλάση `com.musictick.Main`

### Χειροκίνητη Μεταγλώττιση & Εκτέλεση

Αν θέλετε να τα τρέξετε ξεχωριστά:

```bash
# Compile
mkdir -p out
javac --module-path libs/javafx-lib \
      --add-modules javafx.controls,javafx.fxml \
      -d out \
      -cp libs/mysql-connector-java.jar \
      $(find src -name "*.java")

# Copy FXML
cp src/*.fxml out/

# Run
java --module-path libs/javafx-lib \
     --add-modules javafx.controls,javafx.fxml \
     --enable-native-access=javafx.graphics \
     --sun-misc-unsafe-memory-access=allow \
     -cp out:libs/mysql-connector-java.jar \
     com.musictick.Main
```

---

## 5. Διαπιστευτήρια Χρηστών (Test Credentials)

Μόλις ανοίξει η εφαρμογή, χρησιμοποιήστε τα παρακάτω για είσοδο:

| Ρόλος | Email / Username | Κωδικός | Περιγραφή |
|---|---|---|---|
| **Admin** | `admin` ή `admin@musictick.com` | `admin` | Πλήρης πρόσβαση: διαχείριση συναυλιών, εγκρίσεις, Forum moderation |
| **Customer** | `user` ή `user@musictick.com` | `user` | Αγορά εισιτηρίων, μεταφορά, ακύρωση, forum |
| **Organizer** | `organizer@musictick.com` | `organizer` | Δημιουργία συναυλιών, διαχείριση events |

> **Tip:** Ο Admin και ο Customer έχουν **shortcut login** — γράψτε απλώς `admin`/`admin` ή `user`/`user`.

---

## 6. Εκτέλεση Automated Tests

Το project περιλαμβάνει μια πλήρη αυτόματη σουίτα δοκιμών που καλύπτει **8 modules** (Happy Paths & Alternative Paths):

```bash
cd /Users/alex/Desktop/CEID/MusicTick

# Compile (αν δεν έχει γίνει ήδη)
mkdir -p out
javac --module-path libs/javafx-lib \
      --add-modules javafx.controls,javafx.fxml \
      -d out \
      -cp libs/mysql-connector-java.jar \
      $(find src -name "*.java")

# Εκτέλεση Tests
java --module-path libs/javafx-lib \
     --add-modules javafx.controls,javafx.fxml \
     --enable-native-access=javafx.graphics \
     --sun-misc-unsafe-memory-access=allow \
     -cp out:libs/mysql-connector-java.jar \
     com.musictick.MusicTickTestRunner
```

### Ενότητες που ελέγχονται:

| # | Module | Περιγραφή |
|---|---|---|
| 1 | **Login & Registration** | Admin/Customer login, λανθασμένα credentials, duplicate email |
| 2 | **Search & Buy** | Αγορά εισιτηρίου, Gateway failure, Circuit Breaker |
| 3 | **Ticket Transfer** | Μεταφορά σε εγγεγραμμένο/ανύπαρκτο χρήστη |
| 4 | **Ticket Cancellation** | Πολιτική ακύρωσης (Allowed / Not Allowed) |
| 5 | **Report Problem** | Αναφορά σε έγκυρο/ανύπαρκτο εισιτήριο |
| 6 | **Forum Moderation** | Δημιουργία thread, κλείδωμα, διαγραφή post |
| 7 | **Notifications** | Φόρτωση ειδοποιήσεων χρήστη |
| 8 | **Admin Concert Deletion** | Διαγραφή συναυλίας (file + DB transactional) |

Αναμενόμενο τελικό αποτέλεσμα:
```
✅ ALL MUSIC TICK SYSTEM TESTS COMPLETED SUCCESSFULLY!
```

---

## 7. Offline Mode (Χωρίς Βάση)

Αν ο MySQL server **δεν είναι ενεργός**, η εφαρμογή λειτουργεί αυτόματα σε **Offline Mode** χρησιμοποιώντας τοπικά αρχεία `.txt`:

| Αρχείο | Περιεχόμενο |
|---|---|
| `approved_concerts.txt` | Εγκεκριμένες συναυλίες |
| `pending_concerts.txt` | Εκκρεμείς συναυλίες προς έγκριση |
| `purchased_tickets.txt` | Αγορασμένα εισιτήρια χρηστών |
| `forum_posts_mock.txt` | Δημοσιεύσεις forum |
| `forum_reports.txt` | Αναφορές περιεχομένου forum |
| `alerts.txt` | Ειδοποιήσεις χρηστών |

> Σε Offline Mode εμφανίζεται σχετικό μήνυμα στο console. Όλες οι λειτουργίες που υποστηρίζονται από αρχεία παραμένουν πλήρως λειτουργικές.

---

## 8. Βασικά Χαρακτηριστικά

- 🔎 **Αναζήτηση Συναυλιών** — Φίλτρα κατά καλλιτέχνη, ημερομηνία, venue
- 🎟️ **Αγορά Εισιτηρίων** — Regular & VIP θέσεις, επεξεργασία πληρωμής με Circuit Breaker
- 🔄 **Μεταφορά Εισιτηρίου** — Transfer σε άλλον εγγεγραμμένο χρήστη
- ❌ **Ακύρωση & Επιστροφή** — Έλεγχος πολιτικής ακύρωσης (<24h block)
- 💎 **VIP Upgrade** — Αναβάθμιση θέσης την τελευταία στιγμή
- 📋 **Forum** — Δημιουργία threads, replies, lock/delete από Admin
- 🔔 **Ειδοποιήσεις** — Αυτόματες alerts για μεταφορές, ακυρώσεις, post deletions
- 🛡️ **Admin Panel** — Έγκριση/Απόρριψη συναυλιών, Ενεργές Συναυλίες, Forum Moderation
- 🗄️ **Dual Mode** — Online (MySQL) + Offline (txt fallback) χωρίς καμία αλλαγή κώδικα

---

## ⚙️ Tech Stack

| Layer | Τεχνολογία |
|---|---|
| **UI** | JavaFX 17 + FXML |
| **Backend Logic** | Java 17 |
| **Database** | MySQL 8.0 |
| **Connectivity** | MySQL Connector/J |
| **Offline Fallback** | Plain text files (`.txt`) |

---

## 🧭 Οδηγός Χρήσης ανά Ρόλο

### 👤 Customer (Πελάτης)

| Ενέργεια | Βήματα |
|---|---|
| **Αγορά εισιτηρίου** | Σύνδεση → Αναζήτηση Συναυλιών → Επιλογή → Επιλογή θέσης (Regular/VIP) → Εισαγωγή κάρτας → Επιβεβαίωση |
| **Μεταφορά εισιτηρίου** | Τα εισιτήριά μου → Επιλογή εισιτηρίου → Transfer → Email παραλήπτη |
| **Ακύρωση εισιτηρίου** | Τα εισιτήριά μου → Ακύρωση → Έλεγχος πολιτικής → Επιβεβαίωση |
| **VIP Upgrade** | Τα εισιτήριά μου → Επιλογή Regular εισιτηρίου → VIP Upgrade |
| **Αναφορά προβλήματος** | Τα εισιτήριά μου → Report Problem → Συμπλήρωση φόρμας |
| **Forum** | Αρχική → Forum → Επιλογή Συναυλίας → Δημιουργία/Απάντηση Post |
| **Ειδοποιήσεις** | Αρχική → Ειδοποιήσεις (🔔) |

> **Shortcut login:** `user` / `user`

---

### 🎤 Organizer (Διοργανωτής)

| Ενέργεια | Βήματα |
|---|---|
| **Δημιουργία συναυλίας** | Σύνδεση → Πίνακας Διοργανωτή → Νέα Συναυλία → Συμπλήρωση στοιχείων → Υποβολή (→ Admin approval) |
| **Διαχείριση events** | Πίνακας Διοργανωτή → Οι Συναυλίες μου |
| **Αποστολή ειδοποιήσεων** | Πίνακας Διοργανωτή → Push Notification σε αγοραστές |

> **Login:** `organizer@musictick.com` / `organizer`

---

### 🛡️ Admin (Διαχειριστής)

| Ενέργεια | Βήματα |
|---|---|
| **Έγκριση/Απόρριψη συναυλίας** | Admin Panel → Εκκρεμείς Συναυλίες → Approve / Reject |
| **Διαγραφή ενεργής συναυλίας** | Admin Panel → Ενεργές Συναυλίες → Επιλογή → Διαγραφή 🗑️ |
| **Forum moderation** | Admin Panel → Αναφορές Forum → Delete / Lock Thread |
| **Διαχείριση χρηστών** | Admin Panel → Χρήστες |

> **Shortcut login:** `admin` / `admin`

---

## 📐 Use Cases — Επισκόπηση

Το σύστημα υλοποιεί τα παρακάτω 10 Use Cases:

| # | Use Case | Actor | Περιγραφή |
|---|---|---|---|
| UC1 | **Register / Login** | Customer | Εγγραφή νέου χρήστη & σύνδεση με έλεγχο duplicate email |
| UC2 | **Search & Buy Ticket** | Customer | Αναζήτηση συναυλίας, επιλογή θέσης, πληρωμή με Circuit Breaker |
| UC3 | **Transfer Ticket** | Customer | Μεταβίβαση εισιτηρίου σε άλλον εγγεγραμμένο χρήστη |
| UC4 | **Cancel Ticket** | Customer | Ακύρωση βάσει πολιτικής χρόνου (<24h block) |
| UC5 | **VIP Upgrade** | Customer | Αναβάθμιση Regular → VIP εισιτηρίου |
| UC6 | **Report Problem** | Customer | Αναφορά προβλήματος σε εισιτήριο προς τον Διοργανωτή |
| UC7 | **Forum** | Customer | Δημιουργία threads & replies ανά συναυλία, admin moderation |
| UC8 | **Manage Concert** | Organizer | Δημιουργία/διαχείριση events + push notifications |
| UC9 | **Approve/Reject Concert** | Admin | Έλεγχος και έγκριση υποβληθεισών συναυλιών |
| UC10 | **Manage Active Concerts** | Admin | Προβολή και ασφαλής διαγραφή ενεργών (APPROVED) συναυλιών |

---

## 🔧 Troubleshooting

### ❌ `chmod: Permission denied` ή `./run.sh: command not found`
```bash
chmod +x run.sh
./run.sh
```

### ❌ `Compilation failed` — `error: package javafx.fxml does not exist`
Βεβαιωθείτε ότι ο φάκελος `libs/javafx-lib/` περιέχει τα αρχεία:
- `javafx-base.jar`
- `javafx-controls.jar`
- `javafx-fxml.jar`
- `javafx-graphics.jar`

Αν λείπουν, κατεβάστε JavaFX 17 SDK από το https://gluonhq.com/products/javafx/ και αποθηκεύστε τα JARs στο `libs/javafx-lib/`.

### ❌ `Communications link failure` — MySQL δεν ανταποκρίνεται
```bash
# macOS — ξεκινήστε MySQL
brew services start mysql
# ή
sudo /usr/local/mysql/support-files/mysql.server start
```
Αν η βάση δεν υπάρχει ακόμα, τρέξτε ξανά το SQL script:
```bash
mysql -u root -p < src/database/musictick.sql
```

### ❌ `Access denied for user 'root'@'localhost'`
Η εφαρμογή χρησιμοποιεί `root` με **κενό κωδικό** ως default. Αν ο MySQL root σας έχει κωδικό, αλλάξτε τη γραμμή σύνδεσης στα αρχεία DAO (π.χ. `BookingDAO.java`, `UserDAO.java`) από:
```java
DriverManager.getConnection(DB_URL, "root", "")
```
σε:
```java
DriverManager.getConnection(DB_URL, "root", "ο_κωδικος_σας")
```

### ❌ `Test Assertion Failed` στα Automated Tests
Εκτελέστε ξανά το SQL script για να επαναφέρετε τα seed data:
```bash
mysql -u root -p < src/database/musictick.sql
```
Στη συνέχεια τρέξτε ξανά τα tests.

### ❌ `NullPointerException` κατά την εκκίνηση
Βεβαιωθείτε ότι αντιγράψατε τα FXML αρχεία στον φάκελο `out/`:
```bash
cp src/*.fxml out/
```

### ⚠️ Η εφαρμογή τρέχει αλλά εμφανίζει κενά δεδομένα
Αν η MySQL είναι offline, η εφαρμογή χρησιμοποιεί τα τοπικά `.txt` αρχεία. Αν και αυτά είναι κενά, είναι αναμενόμενη συμπεριφορά — συνδεθείτε στη βάση για πλήρη δεδομένα.
