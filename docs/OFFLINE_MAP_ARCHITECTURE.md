# Southern Indiana Lore: Offline Map Architecture

## 1. Current State
`InteractiveLoreMap` makes no network requests, and the app declares no `INTERNET` permission.

- **Tile protocol**: Standard Web Mercator (EPSG:3857) 256x256 px tiles, generated on demand.
- **Base layer**: `OfflineBasemapRenderer` draws a minimal schematic from a few hand-placed polylines: the Ohio River, Silver Creek, a Knobstone ridgeline, and one historic road/rail corridor (Monon / Paoli Pike), on a dark background with a tile grid and a `z/x/y [OFFLINE]` label on each tile. It has no place names, street data, or hill shading, and it is **not** surveyed data. Do not use it for navigation; the "Navigate" buttons hand off to Google Maps.
- **Historical overlays**: `UsgsHistoricalTopoRasterGenerator` (1955 topo) and `SanbornRasterTileGenerator` (1886/1905 Sanborn conventions) render on-device as well.
- **Caching**: in-memory `LruCache<String, Bitmap>(128)`.

An earlier prototype streamed CARTO "Dark Matter" raster tiles with OkHttp when a connection was available. That was removed so that the "fully offline" and "no network access" claims are true, and to avoid depending on a third-party tile service that has its own usage terms. The trade-off is that the base map is now the minimal schematic above everywhere, not just when offline. A real offline basemap is the next step (section 2).

---

## 2. Target Architecture (Roadmap)
To show real roads, water, and place names without any network access, a later release can embed a vector tile renderer.

### Packaged asset format
- **Container**: SQLite-based `.mbtiles` or `.pmtiles` archive bundled in `assets/lorepacks/southern_indiana/map/`, or imported from device storage.
- **Vector format**: Mapbox Vector Tiles (MVT) with OpenStreetMap highway geometries, water bodies, county borders, and contour intervals. (OpenStreetMap data is ODbL: attribution is required in the app when this is added.)
- **Historical overlays**: georeferenced rasters of historic survey maps.

### Rendering stack options
1. **MapLibre Native Android SDK**: hardware-accelerated vector rendering with native support for local `.mbtiles` sources and custom styles.
2. **Embedded Canvas vector engine**: a lightweight Compose Canvas renderer reading GeoJSON/MVT geometry unpacked from `LorePackManager`, with no native dependencies.

### Principles
- Keep the fail-closed truth layer: the map must visually separate verified landmarks from folklore.
- Keep pack authority: tile packages should ship with hash manifests matching the lore pack.
- Keep user-added markers independent of the base map archive.

---

## 3. Dependency Inventory (Current)
- `androidx.collection:collection-ktx` - `LruCache` for memory caching.
- `androidx.compose.foundation:foundation` - gesture detection (`detectDragGestures`, `detectTapGestures`) and Canvas drawing.
- No HTTP client is a direct dependency. (Coil, used for local images, brings OkHttp transitively; the app never calls it for network tiles.)
