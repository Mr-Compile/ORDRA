# ORDRA Navigation UX

Primary bottom destinations: **Home, Orders, Calendar, Insights** with a visually emphasized
central **Add/Scan** action. The center slot is an action, not a persistent destination with
its own back stack; a floating/elevated bar with restrained `GlassSurface` treatment is the
approved treatment. Every destination supports active/inactive states, an optional
notification indicator, accessibility labels, and both themes.

Spec-vs-canonical decision (2026-10-27): the design direction suggested
Home/Orders/Track-Add/Activity/Profile. Ordra keeps Calendar and Insights as committed
destinations (delivery calendar and local insights are functional requirements), maps
"Track/Add" to the central Add/Scan affordance, maps "Activity" to the detection/activity
feed inside Smart Parcel Status (UI-7), and maps "Profile" to Settings reached from top app
bars. If the owner later prefers an Activity tab, swapping a bottom destination does not
change the route graph.

Add/Scan opens a modal bottom sheet:
- Add Order Manually
- Scan Screenshot
- Import Image

Settings opens from top-level app bars. Order Detail is pushed from
Home/Orders/Calendar/History. Tracking Detail (Shipment Timeline) pushes from Order
Detail's Shipment section and parcel cards, and returns to the source context. Status
Suggestion review opens from detected-update surfaces/notifications and resolves back to the
source context. Edit Order pushes from Detail and returns to the same order after save.
Smart Import Review returns to the created Order Detail after successful save.

Adaptive navigation: compact widths keep the bottom bar; medium/expanded window size classes
use a `OrdraNavRail`/sidebar with the same destinations. Usability wins over visual
experimentation — the rail keeps identical ordering and labels.

Back behavior must be predictable: no duplicate Home destinations, no loops, no lost form
state from accidental recomposition. Deep links/share intents enter a safe import flow and
return to Ordra normally.
