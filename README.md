# Workspace - Offline Notes & Documents App

**A seemingly normal offline notes app with a hidden encrypted vault layer**

---

## 🎯 **Project Concept**

This is NOT just another notes app. It's a **stealth security app** disguised as a boring productivity tool.

### **Why This Design?**
1. ✅ **Everyone uses notes apps** - No suspicion
2. ✅ **Offline-first** - No cloud, no sync, full privacy
3. ✅ **Natural file handling** - Notes, PDFs, documents
4. ✅ **Hidden vault** - Gesture-based access, no visible UI
5. ✅ **Decoy system** - Fake content for forced access

---

## 🏗️ **Architecture**

### **Two-Layer System**

#### **Public Layer** (What Everyone Sees)
- ✅ Simple notes (create, edit, delete, search)
- ✅ Document reader (PDF, TXT support planned)
- ✅ Image viewer
- ✅ Light/Dark mode
- ✅ Material Design 3
- ✅ Completely offline

#### **Hidden Layer** (Secret Vault)
- 🔐 Gesture-based unlock (swipe pattern on empty note)
- 🔐 AES-256 encrypted database (SQLCipher)
- 🔐 Encrypted file storage
- 🔐 No visible vault UI
- 🔐 Failed attempt tracking
- 🔐 Emergency wipe capability

---

## 🔐 **Security Features**

### **Encryption**
- **Database**: SQLCipher with passphrase derived from gesture pattern
- **Content**: AES-256-CBC with PBKDF2 key derivation
- **Files**: Encrypted with same AES-256 algorithm
- **Salt & IV**: Randomly generated for each encryption

### **Gesture System**
- Swipe pattern on special "gateway note" (empty note)
- Pattern converted to SHA-256 hash
- Minimum 4 swipes, no consecutive same directions
- Pattern generates database passphrase

### **Access Control**
- Failed attempt tracking
- Biometric authentication support (optional)
- Auto-lock on app background
- Emergency vault wipe

---

## 📁 **Project Structure**

```
app/src/main/java/com/devstudio/workspace/
├── data/
│   ├── model/
│   │   ├── Note.kt              # Public notes
│   │   ├── Document.kt          # Public documents
│   │   └── VaultItem.kt         # Encrypted vault items
│   ├── dao/
│   │   ├── NoteDao.kt           # Notes database access
│   │   ├── DocumentDao.kt       # Documents database access
│   │   └── VaultDao.kt          # Vault database access
│   ├── database/
│   │   ├── AppDatabase.kt       # Public unencrypted database
│   │   └── VaultDatabase.kt     # Encrypted vault database (SQLCipher)
│   └── repository/
│       ├── NoteRepository.kt    # Notes business logic
│       └── VaultRepository.kt   # Vault with auto encryption/decryption
├── ui/
│   ├── theme/
│   │   ├── Color.kt             # Material 3 colors
│   │   ├── Type.kt              # Typography
│   │   └── Theme.kt             # Theme configuration
│   └── viewmodel/
│       └── NoteViewModel.kt     # Notes state management
├── util/
│   ├── EncryptionUtil.kt        # AES-256 encryption
│   ├── GestureManager.kt        # Gesture detection & validation
│   └── SecurePreferences.kt     # Secure settings storage
└── MainActivity.kt              # App entry point
```

---

## 🛠️ **Tech Stack**

### **Core**
- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Repository Pattern
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 36 (Latest)

### **Libraries**
- **Room**: Local database (public layer)
- **SQLCipher**: Encrypted database (vault layer)
- **DataStore**: Secure preferences
- **Coroutines**: Async operations
- **Compose Navigation**: Screen navigation
- **Biometric**: Fingerprint/Face unlock
- **Gson**: JSON serialization

---

## 🚀 **How It Works**

### **First Time Setup**
1. User opens app → sees normal notes app
2. App creates one special "gateway note" (empty, invisible marker)
3. User can create normal notes freely

### **Vault Initialization**
1. User performs specific gesture on gateway note
2. App prompts to set gesture pattern
3. Pattern generates encryption key
4. Vault database created with SQLCipher
5. Gateway note marked internally

### **Vault Access**
1. User opens gateway note (looks empty)
2. Performs correct gesture pattern
3. Pattern verified against stored hash
4. Vault database unlocked with derived key
5. Vault UI shown (separate from normal notes)

### **Wrong Gesture**
1. Failed attempt logged
2. Note behaves normally (no indication)
3. After X attempts → emergency wipe option
4. No visible error messages

---

## 🎨 **UI/UX Philosophy**

### **Boring is Beautiful**
- No flashy animations
- Standard Material Design
- Looks like every other notes app
- No "vault" or "secure" branding
- No suspicious permissions

### **Natural Behavior**
- Gateway note appears empty
- Can be edited like normal note
- No special indicators
- Gesture detection is invisible
- Wrong gesture = normal note behavior

---

## 🔒 **Security Best Practices**

1. ✅ **No hardcoded keys** - All keys derived from user gesture
2. ✅ **Salt & IV** - Random for each encryption
3. ✅ **PBKDF2** - 10,000 iterations for key derivation
4. ✅ **AES-256** - Industry standard encryption
5. ✅ **SQLCipher** - Encrypted database at rest
6. ✅ **No logs** - No sensitive data in logs
7. ✅ **Memory cleanup** - Keys cleared after use
8. ✅ **No internet** - Completely offline

---

## 📝 **Current Status**

### ✅ **Completed**
- [x] Project setup with Gradle
- [x] Jetpack Compose + Material 3 theme
- [x] Data models (Note, Document, VaultItem)
- [x] Room DAOs for all entities
- [x] Public database (AppDatabase)
- [x] Encrypted vault database (VaultDatabase with SQLCipher)
- [x] Encryption utilities (AES-256)
- [x] Gesture detection system
- [x] Secure preferences (DataStore)
- [x] Repositories with auto encryption
- [x] Basic MainActivity with Compose
- [x] Build successful ✅

### 🚧 **In Progress**
- [ ] Notes list UI
- [ ] Note editor UI
- [ ] Gesture detection UI
- [ ] Vault setup flow
- [ ] Vault access UI
- [ ] Document viewer
- [ ] Settings screen

### 📋 **Planned**
- [ ] PDF reader integration
- [ ] Image viewer
- [ ] Biometric authentication
- [ ] Decoy content system
- [ ] Emergency wipe
- [ ] Export/Import (encrypted)
- [ ] Dark mode refinement

---

## 🎯 **Next Steps**

1. **Build Notes List Screen** - Show all public notes
2. **Build Note Editor** - Create/edit notes
3. **Implement Gesture Detection** - Swipe pattern recognition
4. **Build Vault Setup** - First-time vault initialization
5. **Build Vault UI** - Encrypted notes interface
6. **Add Decoy System** - Fake content for plausible deniability

---

## ⚠️ **Important Notes**

### **This is a STEALTH app**
- Never advertise the vault feature
- No "secure" or "encrypted" in app name
- No suspicious permissions
- Looks completely normal

### **Security Disclaimer**
- This provides strong encryption
- But security depends on:
  - Strong gesture pattern
  - Device security (lock screen)
  - Physical device security
  - No root/jailbreak

### **Legal**
- Use responsibly
- Comply with local laws
- Not for illegal activities
- Privacy is a right, not a crime

---

## 🏁 **Build & Run**

```bash
# Build debug APK
./gradlew assembleDebug

# Install on device
./gradlew installDebug

# Build release APK (requires signing)
./gradlew assembleRelease
```

---

## 📄 **License**

This project is for educational purposes. Use responsibly.

---

**Remember**: The best security is the one nobody knows exists. 🤫
