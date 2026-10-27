# ORDRA Navigation UX

Primary bottom destinations: Home, Orders, Calendar, Insights. Central Add/Scan is an action, not a persistent destination with its own back stack.

Add/Scan opens a bottom sheet:
- Add Order Manually
- Scan Screenshot
- Import Image

Settings opens from top-level app bars. Order Detail is pushed from Home/Orders/Calendar/History. Shipment Timeline pushes from Order Detail's Shipment section and returns to it. Status Suggestion review opens from detected-update surfaces/notifications and resolves back to the source context. Edit Order pushes from Detail and returns to the same order after save. Smart Import Review returns to the created Order Detail after successful save.

Back behavior must be predictable: no duplicate Home destinations, no loops, no lost form state from accidental recomposition. Deep links/share intents should enter a safe import flow and return to Ordra normally.
