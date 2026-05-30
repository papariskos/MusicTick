# 🎫 MusicTick — Neon Rhythm Ticketing Platform

Ένα ολοκληρωμένο, premium σύστημα αγοράς και διαχείρισης εισιτηρίων συναυλιών, βασισμένο σε **Java + JavaFX + MySQL**. Υποστηρίζει ρόλους Πελάτη (Customer), Διοργανωτή (Organizer) και Διαχειριστή (Admin), με απόλυτη εστίαση σε μια **100% καθαρή αρχιτεκτονική βάσης δεδομένων (Pure Database Only)** χωρίς hybrid ή offline modes.

---

## 📋 Περιεχόμενα

1. [Προαπαιτούμενα](#1-προαπαιτούμενα)
2. [Δομή Project](#2-δομή-project)
3. [Ρύθμιση & Εγκατάσταση Βάσης Δεδομένων (MySQL)](#3-ρύθμιση--εγκατάσταση-βάσης-δεδομένων-mysql)
4. [Εκτέλεση της Εφαρμογής](#4-εκτέλεση-της-εφαρμογής)
5. [Διαπιστευτήρια Χρηστών (Test Credentials)](#5-διαπιστευτήρια-χρηστών-test-credentials)
6. [Εκτέλεση της Αυτόματης Σουίτας Δοκιμών (Automated Tests)](#6-εκτέλεση-της-αυτόματης-σουίτας-δοκιμών-automated-tests)
7. [Αρχιτεκτονική & Χαρακτηριστικά](#7-αρχιτεκτονική--χαρακτηριστικά)
8. [Αντιμετώπιση Προβλημάτων (Troubleshooting)](#8-αντιμετώπιση-προβλημάτων-troubleshooting)

---

## 1. Προαπαιτούμενα

Βεβαιωθείτε ότι έχετε εγκαταστήσει τα παρακάτω στο σύστημά σας:

| Εργαλείο | Ελάχιστη Έκδοση | Λήψη |
|---|---|---|
| **Java JDK** | 17+ (Συνίσταται JDK 21+) | [Oracle Java Downloads](https://www.oracle.com/java/technologies/downloads/) |
| **MySQL Server** | 8.0+ | [MySQL Community Downloads](https://dev.mysql.com/downloads/mysql/) |
| **JavaFX SDK** | 17+ | *Περιλαμβάνεται ήδη στο φάκελο `libs/javafx-lib/`* |
| **MySQL Connector** | — | *Περιλαμβάνεται ήδη στο φάκελο `libs/mysql-connector-java.jar`* |

---

## 2. Δομή Project

```
MusicTick/
├── src/
│   ├── com/musictick/          # Κύριος κώδικας Java
│   │   ├── controller/         # JavaFX Controllers (UI logic)
│   │   ├── dao/                # Data Access Objects (DB queries)
│   │   ├── manager/            # Business Logic Managers
│   │   ├── ConcertManager.java # Service layer διαχείρισης συναυλιών
│   │   ├── DBConfig.java       # Κεντρική ρύθμιση σύνδεσης MySQL
│   │   ├── Main.java           # Entry point της εφαρμογής
│   │   └── MusicTickTestRunner.java  # Σουίτα αυτόματων δοκιμών
│   ├── database/
│   │   └── musictick.sql       # Script δημιουργίας & αρχικοποίησης DB
│   └── *.fxml                  # UI layout αρχεία (JavaFX)
├── libs/
│   ├── javafx-lib/             # JavaFX SDK βιβλιοθήκες (.jar)
│   └── mysql-connector-java.jar # MySQL JDBC Driver
├── README.md                   # Οδηγίες χρήσης
└── run.sh                      # Shell script μεταγλώττισης & εκτέλεσης
```

---

## 3. Ρύθμιση & Εγκατάσταση Βάσης Δεδομένων (MySQL)

### Βήμα 1: Εκκίνηση του MySQL Server

- ** macOS:**
  - Μέσω Homebrew:
    ```bash
    brew services start mysql
    ```
  - Μέσω Installer: Εκκινήστε τον MySQL Server από τα System Settings (Προτιμήσεις Συστήματος).
- **❖ Windows:**
  - Εκκινήστε την υπηρεσία MySQL από τα Windows Services (Υπηρεσίες -> `MySQL80` -> Έναρξη) ή μέσω Command Prompt ως Administrator:
    ```cmd
    net start mysql
    ```
- **🐧 Linux:**
  ```bash
  sudo systemctl start mysql
  ```

### Βήμα 2: Παραμετροποίηση Σύνδεσης (Προαιρετικό)

Όλα τα στοιχεία σύνδεσης της εφαρμογής είναι κεντρικοποιημένα στο αρχείο:
[DBConfig.java](src/com/musictick/DBConfig.java)

Αν ο τοπικός σας MySQL root χρήστης απαιτεί κωδικό, ανοίξτε το αρχείο και τροποποιήστε τις σταθερές:
```java
public static final String DB_USER = "root";
public static final String DB_PASSWORD = "ο_κωδικος_σας";
```

### Βήμα 3: Εισαγωγή της Βάσης Δεδομένων & Seed Data

Ανοίξτε το τερματικό και εκτελέστε το SQL Script για να δημιουργηθούν αυτόματα η βάση δεδομένων `musictick`, οι πίνακες, τα indexes και τα seed δεδομένα:

- **macOS / Linux:**
  ```bash
  cd MusicTick
  mysql -u root -p < src/database/musictick.sql
  ```
- **Windows:**
  ```cmd
  cd MusicTick
  mysql -u root -p < src\database\musictick.sql
  ```

*(Αν δεν έχετε κωδικό root, πατήστε απλώς **Enter**).*

---

## 4. Εκτέλεση της Εφαρμογής

Μπορείτε να εκτελέσετε την εφαρμογή εύκολα χρησιμοποιώντας τα έτοιμα σενάρια (scripts) που αναλαμβάνουν αυτόματα τη μεταγλώττιση, την αντιγραφή των UI πόρων και την εκτέλεση:

###  macOS / Linux
```bash
cd MusicTick
chmod +x run.sh
./run.sh
```

### ❖ Windows
```cmd
cd MusicTick
run.bat
```

### Χειροκίνητη Μεταγλώττιση & Εκτέλεση

Αν επιθυμείτε να εκτελέσετε τα βήματα χειροκίνητα:

####  macOS / Linux
```bash
# 1. Δημιουργία φακέλου εξόδου
mkdir -p out

# 2. Μεταγλώττιση
javac --module-path libs/javafx-lib \
      --add-modules javafx.controls,javafx.fxml \
      -d out \
      -cp libs/mysql-connector-java.jar \
      $(find src -name "*.java")

# 3. Αντιγραφή FXML UI layouts
cp src/*.fxml out/

# 4. Εκτέλεση της εφαρμογής
java --module-path libs/javafx-lib \
     --add-modules javafx.controls,javafx.fxml \
     --enable-native-access=javafx.graphics \
     --sun-misc-unsafe-memory-access=allow \
     -cp out:libs/mysql-connector-java.jar \
     com.musictick.Main
```

#### ❖ Windows
```cmd
:: 1. Δημιουργία φακέλου εξόδου
mkdir out

:: 2. Δημιουργία λίστας αρχείων Java και μεταγλώττιση
dir /s /b src\*.java > sources.txt
javac --module-path libs\javafx-lib --add-modules javafx.controls,javafx.fxml -d out -cp libs\mysql-connector-java.jar @sources.txt
del sources.txt

:: 3. Αντιγραφή FXML UI layouts
copy src\*.fxml out\

:: 4. Εκτέλεση της εφαρμογής
java --module-path libs\javafx-lib --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics --sun-misc-unsafe-memory-access=allow -cp out;libs\mysql-connector-java.jar com.musictick.Main
```

---

## 5. Διαπιστευτήρια Χρηστών (Test Credentials)

Για να δοκιμάσετε την εφαρμογή, χρησιμοποιήστε τους ακόλουθους λογαριασμούς:

| Ρόλος | Email / Username | Κωδικός | Περιγραφή |
|---|---|---|---|
| **Admin** | `admin` ή `admin@musictick.com` | `admin` | Διαχείριση, εγκρίσεις συναυλιών, forum moderation |
| **Customer** | `user` ή `user@musictick.com` | `user` | Αγορά/μεταφορά/ακύρωση εισιτηρίων, forum reviews |
| **Organizer** | `organizer@musictick.com` | `organizer` | Δημιουργία συναυλιών, διαχείριση open reports |

> 💡 **Shortcut Login:** Ο Admin και ο Customer υποστηρίζουν γρήγορη είσοδο πληκτρολογώντας απλώς `admin` ή `user` στα αντίστοιχα πεδία.

---

## 6. Εκτέλεση της Αυτόματης Σουίτας Δοκιμών (Automated Tests)

Το project περιλαμβάνει μια ριζικά αναβαθμισμένη αυτόματη σουίτα δοκιμών η οποία εκτελεί 10 ολοκληρωμένα σενάρια ελέγχου (Happy & Alternative Paths) σε πραγματικό χρόνο πάνω στη ζωντανή βάση δεδομένων:

###  macOS / Linux
```bash
cd MusicTick

# Εκτέλεση της σουίτας δοκιμών
java --module-path libs/javafx-lib \
     --add-modules javafx.controls,javafx.fxml \
     --enable-native-access=javafx.graphics \
     --sun-misc-unsafe-memory-access=allow \
     -cp out:libs/mysql-connector-java.jar \
     com.musictick.MusicTickTestRunner
```

### ❖ Windows
```cmd
cd MusicTick

:: Εκτέλεση της σουίτας δοκιμών
java --module-path libs\javafx-lib --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics --sun-misc-unsafe-memory-access=allow -cp out;libs\mysql-connector-java.jar com.musictick.MusicTickTestRunner
```

### Τι περιλαμβάνουν τα Tests:
1. **Login & Registration**: Έλεγχος shortcut login, duplicate email signups και σφαλμάτων κωδικού.
2. **Search & Buy**: Δυναμική κράτηση θέσεων, simulated Gateway failure και δοκιμή του **Circuit Breaker**.
3. **Ticket Transfer**: Μεταβίβαση εισιτηρίου σε έγκυρο και ανύπαρκτο email.
4. **Ticket Cancellation**: Έλεγχος πολιτικής ακύρωσης βάσει χρόνου (<24h).
5. **Report Problem**: Υποβολή αναφορών προβλημάτων σε active εισιτήρια.
6. **Forum & Moderation**: Δημιουργία thread/reply, κλείδωμα thread, soft-deletion.
7. **Notifications**: Ανάκτηση και προβολή των Alerts του χρήστη.
8. **Admin Concert Deletion**: Ασφαλής και transactional cascading διαγραφή ενεργών συναυλιών.
9. **VIP Upgrade**: Αναβάθμιση θέσης σε VIP, πληρωμή επιπλέον ποσού και επαλήθευση ορατότητας.
10. **Concert Review**: Υποβολή αξιολογήσεων, έλεγχος εξουσιοδότησης (ticket holders only) και duplicate reviews block.

Αναμενόμενο αποτέλεσμα:
```text
✅ ALL MUSIC TICK SYSTEM TESTS COMPLETED SUCCESSFULLY!
```

---

## 7. Αρχιτεκτονική & Χαρακτηριστικά

- **100% Pure Database (No Files)**: Όλες οι εγγραφές, ειδοποιήσεις, εισιτήρια και posts αποθηκεύονται στη MySQL. Δεν υπάρχουν πλέον τοπικά txt αρχεία.
- **SQL Transactions**: Κρίσιμες διαδικασίες (όπως η αγορά εισιτηρίου, η VIP αναβάθμιση και η διαγραφή) εκτελούνται με ACID SQL Transactions (αυτόματο rollback σε περίπτωση αποτυχίας).
- **Circuit Breaker**: Το σύστημα πληρωμών παρακολουθεί τις αποτυχίες της πύλης πληρωμών και "ανοίγει" τον διακόπτη μετά από 3 συνεχόμενες αποτυχίες για να προστατεύσει το σύστημα.
- **Cyberpunk Dark Theme**: Πανέμορφη διεπαφή χρήστη με neon-cyan και hot-pink στοιχεία, dropshadows και micro-animations.

---

## 8. Αντιμετώπιση Προβλημάτων (Troubleshooting)

### ❌ Σφάλμα `Communications link failure`
Η MySQL δεν είναι ενεργή. Εκκινήστε τον MySQL Server (π.χ. `brew services start mysql` στο macOS, `net start mysql` στα Windows, ή `sudo systemctl start mysql` στο Linux) και βεβαιωθείτε ότι τρέχει στη θύρα 3306.

### ❌ Σφάλμα `Access denied for user 'root'@'localhost'`
Ο root χρήστης σας στη MySQL έχει κωδικό πρόσβασης. Ανοίξτε το αρχείο `src/com/musictick/DBConfig.java` και προσθέστε τον κωδικό σας στη μεταβλητή `DB_PASSWORD`.

### ❌ Αποτυχία Test Assertions
Αν κάποιο test αποτύχει λόγω αλλαγής δεδομένων, εκτελέστε ξανά το SQL script για να επαναφέρετε τη βάση στα αρχικά της δεδομένα:
```bash
mysql -u root -p < src/database/musictick.sql
```
