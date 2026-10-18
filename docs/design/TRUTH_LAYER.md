# 01 - SPRINT 1.5: TRUTH LAYER & EPISTEMIC CLASSIFICATION
## Neighborhood Lore Machine - Foundation Freeze

---

## 1. The Core Principle

> **"THE APP MUST NEVER CLAIM MORE CERTAINTY THAN THE DATA ACTUALLY SUPPORTS."**

In historical expedition software, uncorroborated oral tradition, regional ghost stories, and public county deeds frequently become conflated into a single monolithic "story". Sprint 1.5 established an **Epistemic Truth Layer** that treats historical certainty as a first-class, strictly enforced data property.

Every single site in the Neighborhood Lore Machine—whether loaded from an authoritative regional pack or logged in the field by an explorer—is classified into an explicit epistemic confidence tier and presented with unambiguous provenance.

---

## 2. The 6-Tier Epistemic Confidence Hierarchy

The domain model `ConfidenceLevel` defines 6 mutually exclusive tiers of historical reliability:

```kotlin
enum class ConfidenceLevel(
    val label: String,
    val description: String,
    val iconName: String,
    val badgeColorHex: Long
) {
    OFFICIAL_SOURCE(
        label = "Official Source",
        description = "Formally documented by government, landmark register, or deed archive",
        iconName = "Verified",
        badgeColorHex = 0xFF22C55E // Emerald Green
    ),
    VERIFIED(
        label = "Verified",
        description = "Corroborated by independent historical records or archival deed",
        iconName = "MenuBook",
        badgeColorHex = 0xFF38BDF8 // Sky Blue
    ),
    PROBABLE(
        label = "Probable",
        description = "Supported by physical artifacts, local records, or multiple accounts",
        iconName = "AutoAwesome",
        badgeColorHex = 0xFF2DD4BF // Teal
    ),
    ORAL_HISTORY(
        label = "Oral History",
        description = "Passed down through community memory or first-hand testimony",
        iconName = "Mic",
        badgeColorHex = 0xFFF59E0B // Amber
    ),
    LEGEND(
        label = "Legend",
        description = "Folklore, supernatural claims, or traditional regional stories",
        iconName = "AutoAwesome",
        badgeColorHex = 0xFFA855F7 // Purple
    ),
    UNVERIFIED(
        label = "Unverified",
        description = "Field report or anecdote without independent corroboration",
        iconName = "Close",
        badgeColorHex = 0xFF94A3B8 // Slate Gray
    );
}
```

---

## 3. The Strict Fail-Closed Rule

To prevent data corruption, missing attributes, or rogue values from masquerading as verified facts, `ConfidenceLevel.fromString` implements a strict **fail-closed contract**:

```kotlin
fun fromString(value: String?): ConfidenceLevel {
    if (value.isNullOrBlank()) return UNVERIFIED
    val normalized = value.trim().uppercase().replace(" ", "_")
    return try {
        valueOf(normalized)
    } catch (_: IllegalArgumentException) {
        when {
            normalized.contains("OFFICIAL") -> OFFICIAL_SOURCE
            normalized.contains("VERIF") && !normalized.contains("UNVERIF") -> VERIFIED
            normalized.contains("PROBAB") -> PROBABLE
            normalized.contains("ORAL") -> ORAL_HISTORY
            normalized.contains("LEGEND") || normalized.contains("FOLK") -> LEGEND
            else -> UNVERIFIED // Strict fail-closed fallback
        }
    }
}
```

### Key Guarantees:
1. **Null or Blank** inputs immediately evaluate to `UNVERIFIED`.
2. **Unrecognized strings** (e.g., typos or corrupted values) fail closed to `UNVERIFIED`.
3. **Database default**: The `LoreSite` domain object and `LoreSiteEntity` table default `confidenceLevel` to `"UNVERIFIED"`.

---

## 4. Separation of Narrative Dimensions in UI

Previously, a single `fullStory` text block mixed verifiable facts (construction dates, architects, casualty counts) with supernatural legends (ghostly train whistles, glowing lights, headless specters). 

In Sprint 1.5, `SiteDetailDialog` divides site details into three distinct architectural panels:

### Panel A: Epistemic Status Card
Highlights the confidence level with its dedicated palette, description, and an origin disclosure. If the site is user-generated (`isCustom = true`), it explicitly displays:
> *"Origin: User Field Discovery (Not independently corroborated by county deed or landmark archive)"*

### Panel B: Documented Archival History (`site.verifiedHistory`)
Displays solely corroborated facts from deed archives, library records, and census data. If no archival facts exist (common for pure folklore sites or ghost trestles), it displays:
> *"No independent archival record or public deed established for this site. Historical claims remain unverified."*

### Panel C: Folklore & Oral Tradition (`site.folkloreTradition`)
Framed in regional folklore purple (`#A855F7`), this section presents cultural heritage, ghost sightings, and oral legends, with the clear advisory:
> *"Preserved as regional cultural heritage, community memory, or folklore. Not established as archival fact."*

### Panel D: Primary Sources & Citations (`site.sources` & `site.sourceNotes`)
Cites specific library holdings, historical society papers, or field interview notes.

---

## 5. Truthful User-Generated Lore

When users log custom lore in the field via `AddCustomLoreDialog`:
- The confidence level is locked to `ConfidenceLevel.UNVERIFIED`.
- The verification grade is locked to `"Field Report (Unverified)"`.
- The UI displays a warning:
  > *"FIELD LOG TRUTH LAYER: Custom sites are recorded as Unverified Field Reports. They will remain marked as 'Not Independently Verified' until corroborated with historical deed records or public archives."*
- On list cards (`LoreCard`), user sites receive the prominent amber tag **`USER REPORT`** alongside the `UNVERIFIED` badge.
