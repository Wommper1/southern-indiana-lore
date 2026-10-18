# Lore Pack Verification Log

Last updated: October 2026. Pack version 1.10.0.

The lore pack in `app/src/main/assets/lorepacks/southern_indiana/` has 52 sites. The citations that came with
the original data were never independently checked, and when we started checking, many entries turned out to
contain details that the sources do not support. This file records what was checked, what was changed, and what
is still unverified, so the in-app labels are honest.

## How the labels work

| Label (in app) | Meaning in this pack | Entries |
| :--- | :--- | ---: |
| Official Source | An official designation (for example National Historic Landmark) was confirmed against an independent source | 2 |
| Plausible | Checked against independent sources and corrected where they disagreed; some other details are still unconfirmed | 22 |
| Unverified | Not yet checked. The cited sources have not been independently confirmed | 22 |
| Folk Legend / Oral History / Probable | The entry's own nature (folklore, oral tradition) or an older label that already admitted incomplete verification. Not re-labeled | 6 |

Unverified entries still appear in the app and trigger alerts, but they show an "Unverified" badge and rank slightly
lower than official ones when several sites are nearby. The detail screen heading reads "History (not fully
verified)" for anything that is not Official Source, Archivally Verified or Well Supported.

## Method and limits

- Each checked claim was compared with at least one public source: mostly Wikipedia, state historical marker
  pages (in.gov), local library and newspaper material, and a few reference sites. Wikipedia is not authoritative and
  sometimes cites nothing; where it was the only source, the log says so.
- **No primary archival records (deed books, census, newspapers of the period) were consulted.**
- Where sources disagree, the entry now says so and gives the range instead of picking one figure.
- Citations that could not be confirmed to exist were removed from the entries that were rewritten (see Duncan
  Tunnel), rather than left looking authoritative.
- A damaged-data finding: three dollar amounts had lost leading characters in the original data (New Harmony's
  "$125,000" became "25,000", Medora's cost became "8,100", Marshfield's "$96,000" became "6,000").

## Corrections made

| Entry | Previously | Now | Basis |
| :--- | :--- | :--- | :--- |
| Saint Meinrad Archabbey | Archabbey "dedicated 1969 by Pope Paul VI"; named founders; "12-bell carillon" | Elevated to archabbey by the Holy See in **1954**; founders and carillon removed (unconfirmed) | Wikipedia |
| New Harmony | Sold to Owen and Maclure for "25,000"; Harmonists 1814-1824 | $125,000 to $150,000 depending on the source (about 20,000 acres); Harmonists left in 1825 | Wikipedia (cites two figures) |
| Medora Covered Bridge | Cost "8,100"; 434 ft span; "National Historic Landmark" | Cost **$18,142**; clear span 431 ft 10 in to 434 ft by source; National Register 2007 (no landmark status found) | Wikipedia |
| Beck's Mill | National Register 2007, "#07001278"; rebuilt by "George Beck II" | National Register **1990** (#90001789); the 2007 listing is the separate Beck's Mill Bridge; rebuilder name removed | Wikipedia, NRHP listing |
| Corydon Capitol / Constitution Elm | "132-foot" elm | About **50 ft tall**, branches spreading about 130 ft | Corydon historical markers, Indiana Public Media |
| Howard Steamboat Museum | Over 3,000 vessels; mansion by Arthur Loomis, finished 1894; NRHP #73000028 | More than 1,100 river vessels (totals vary); mansion begun 1890, designed by Drach and Thomas with Loomis supervising; NRHP #73000031 | Wikipedia |
| Big Four Bridge | Built 1890-1895; Pratt spans; "designed by George S. Morison" | Construction began **1888**, opened 1895, rebuilt 1929 on the old piers; current spans are Parker and Pennsylvania trusses; Morison attribution removed (unconfirmed) | Wikipedia |
| K&I Bridge | "Rebuilt 1912 by the Phoenix Bridge Company into a steel cantilever span" | Replaced **1910-1912** by a double-track swing-span bridge; contractor unconfirmed | Wikipedia |
| Tulip Trestle | "Second longest viaduct in the world", "third longest steel viaduct on Earth"; 2,307 ft; 2,895 tons; engineer A.S. Baldwin; builder Strobel | Longest rail trestle in the US when completed and third longest of its kind in the world; length 2,295 to 2,307 ft by source; about 2,700 short tons; engineer and builder unconfirmed | Wikipedia |
| Hindostan Falls | Platted 1818; "totally abandoned" by 1828; epidemic "late summer 1820"; cited to Ruby Rogers, Indiana Magazine of History, 1957 | Settled about 1816, platted by 1818; epidemic timing differs by source; county seat moved 1828, post office closed 1830, residents moved to Loogootee 1853; citation replaced with the sources actually found | Wikipedia, Indiana Historical Bureau marker |
| Rose Island (2 entries) | "135,000 visitors in the 1926 season"; flood "reached 85.4 feet" | About 135,000 visitors a year at peak (state marker); flood height removed (unsupported) | State marker, Wikipedia, RCDB |
| USS Argosy No. 3 | "Destroyed" in 1865; all 10 dead from the 70th Ohio | Exploded and was later rebuilt; at least 9 of the 10 were 70th Ohio; wreck site differs by source | State marker, Waterways Journal |
| Steamboat Eclipse | Built by James Howard; 365 ft; 36-ft wheels; "over 300 steamboats" at New Albany | Built at New Albany in 1852, 1,117 tons, 140 staterooms; yards named in sources are Dowerman and others; dimensions and the 300 figure unconfirmed | Indiana state history site |
| Squire Boone Caverns | Discovered 1790; mill built 1804; entombed in the cavern | Found in the late 1700s; mill date 1804 or 1808 by source; buried near the entrance, remains later moved deeper | Wikipedia |
| Wyandotte caves (2 entries) | Saltpeter miner "Dr. Samuel Adams"; flint mining "1000 BCE" | **Dr. Benjamin Adams**; Native American use roughly 4,000 years by one account; state bought the property in 1966; Monument Mountain is 135 ft (the Archivist said 185) | Wikipedia |
| Marshfield Train Robbery | "6,000" taken; messenger "Thomas Harkins" severely injured | About **$96,000**; the messenger's fate and name are not settled in sources | True West, Wikipedia-derived sources |
| Monon corridor | Founded by "Dr. Nathaniel Scribner and James Brooks"; carried escaping slaves | Organized 1847 with merchant James Brooks as president; the Underground Railroad claim is shown as undocumented local tradition | Indiana archives pages |
| Culbertson Mansion | Staff "have documented" piano music and cold spots; "donated to the state" | Presented as ghost lore (the haunted-house tradition began in 1985); state museum system 1976; National Register 1974 | Wikipedia |
| Madison Incline | "Operated by cog mechanism until 1868"; "cog engines like the Reuben Wells" | Horses 1841-1848, cog rail from about 1848, then the Reuben Wells (1868) without cog assistance | Railroad history pages |
| Duncan Tunnel | 4,295 ft as fact; completed by the Air Line; "chief surveyor K.C. Henderson" | Length shown as a range (4,295 / 4,311 / 4,689 ft by source); begun about 1870, finished 1881 by a successor company, first used 1882; Henderson removed; chief engineer in 1881 was Marshal Morris | 1960 New Albany Valley News article, Indiana Bicentennial Commission, Wikipedia |

## Confirmed without changes

- **Cannelton Cotton Mill & Industrial Fortress**: National Register (1975) and National Historic Landmark (1991) status confirmed
- **Eleutherian College Abolitionist Sanctuary**: National Historic Landmark (1997) status confirmed
- **Tunnelton Big Tunnel & Spectral Watchman**: tunnel length and 1857 completion confirmed (one source); legend unverified

## Not yet checked (27 entries)

These are labeled **Unverified** (or keep an older label such as Folk Legend or Probable) until they are checked:

- The Town Clock Church (Floyd County)
- The Scribner House (Floyd County)
- Georgetown Historic District & Tunnel Hill (Floyd County)
- Falling Run Phantom Trestle (Floyd County)
- Floyds Knobs & The Ridge Witches Tree (Floyd County)
- Floyd County Asylum & Potter's Field (Floyd County)
- Silver Hills Hermit & Ghost Light (Floyd County)
- Greenville Mineral Springs & Lost Resort (Floyd County)
- Falls of the Ohio & Clark's Cabin Site (Clark County)
- Twelve Mile Island Steamboat Wrecks (Clark County)
- Battle of Corydon Civil War Site (Harrison County)
- Clarks Point & Lewis and Clark Launch Overlook (Clark County)
- Lake Iola & Interurban Electric Traction Site (Scott County)
- Freeman Field & Tuskegee Airmen Civil Rights Protest (Jackson County)
- Republic Aviation P-47 Thunderbolt Factory (Vanderburgh County)
- Lincoln Gardens & Baptisttown Heritage (Vanderburgh County)
- Fort Sackville & The Wabash Territorial Borderland (Knox County)
- Marengo Cave National Landmark (Crawford County)
- Bluespring Caverns Underground River (Lawrence County)
- Spring Mill Twin Caves & Subterranean Karst River (Lawrence County)
- Milltown Blue River Pratt Truss Bridge (Crawford County)
- The Buffalo Trace (Vincennes Trace) (Floyd County)
- The Lost River Karst System (Orange County)
- Salem Underground Railroad & Morgan's Raid Depot (Washington County)
- Jasper Historic Train Depot & Patoka River Span (Dubois County)
- Historic Rome Courthouse (Perry County)
- Tell City Swiss Colonization Enclave (Perry County)

## Known issues not fixed

- **Duplicate entries:** the two Rose Island entries overlap each other, and three entries cover Wyandotte and
  Marengo Caves. They were kept so site IDs, geofences, and trails keep working.
- **Scribner House:** the story text names three Scribner brothers; the history text names two.
- **Underground Railroad claims** (Town Clock Church sub-floor spaces, Monon boxcars, Salem) need
  skeptical review; Indiana DNR notes the network had no single fixed route and warns against tunnel and hidden-room
  claims.
- **Relics and "oral history" recordings** in the Relic Lab and the Oral History archive are dramatized fiction.
  They are not historical documents and have not been audited.
- **Archivist answers** (`LocalArchivistService`) are hand-written text; they no longer say "verified", but most of
  their details are unchecked.
- **Emergency fallback data** (`LoreSeedData.kt`) carries the same corrections for the fields that were changed. It
  is only used if the pack fails validation on an empty database, and every entry in it is labeled Unverified.

## How to help check an entry

Pick an Unverified entry, find a source that confirms or contradicts each specific claim (dates, numbers, names), and
send the correction with the source. Prefer state historical markers, National Register records, and period
newspapers over summaries.

## Sources used

- Wikipedia: [Duncan Tunnel](https://en.wikipedia.org/wiki/Duncan_Tunnel), [Tulip Viaduct](https://en.wikipedia.org/wiki/Tulip_Viaduct), [Hindostan Falls](https://en.wikipedia.org/wiki/Hindostan_Falls,_Indiana), [Saint Meinrad Archabbey](https://en.wikipedia.org/wiki/Saint_Meinrad_Archabbey), [Eleutherian College](https://en.wikipedia.org/wiki/Eleutherian_College), [New Harmony](https://en.wikipedia.org/wiki/New_Harmony,_Indiana), [Cannelton Cotton Mill](https://en.wikipedia.org/wiki/Cannelton_Cotton_Mill), [Medora Covered Bridge](https://en.wikipedia.org/wiki/Medora_Covered_Bridge), [Culbertson Mansion](https://en.wikipedia.org/wiki/Culbertson_Mansion), [Howard Steamboat Museum](https://en.wikipedia.org/wiki/Howard_Steamboat_Museum), [Squire Boone Caverns](https://en.wikipedia.org/wiki/Squire_Boone_Caverns), [Big Four Bridge](https://en.wikipedia.org/wiki/Big_Four_Bridge_(Louisville)), [K&I Terminal Bridge](https://en.wikipedia.org/wiki/Kentucky_%26_Indiana_Terminal_Bridge), [Beck's Mill](https://en.wikipedia.org/wiki/Beck%27s_Mill), [Tunnelton](https://en.wikipedia.org/wiki/Tunnelton,_Indiana), [Rose Island](https://en.wikipedia.org/wiki/Rose_Island_(amusement_park)), [Wyandotte Caves](https://en.wikipedia.org/wiki/Wyandotte_Caves)
- Indiana state pages: [Hindostan marker](https://www.in.gov/history/markers/231.htm), [Argosy No. 3 marker](https://www.in.gov/history/markers/263.htm), [Duncan Tunnel (Bicentennial Commission)](https://www.in.gov/ibc/2451.htm), [New Albany shipyards](https://discoverindianahistory.org/items/show/154), [New Albany and Salem Railroad](https://discoverindianahistory.org/items/show/99), [Constitution Elm](https://indianapublicmedia.org/momentofindianahistory/tree-grew-corydon)
- Other: [Floyd County Public Library, Edwardsville Tunnel (1960 article)](https://floydlibrary.org/wp-content/uploads/2016/07/edwardsvilletunnel.pdf), [Madison and Indianapolis Railroad history](https://www.frrandp.com/2020/06/indianas-first-railroad-madison.html), [Reno Gang and the Marshfield robbery](https://truewestmagazine.com/?p=38542)
