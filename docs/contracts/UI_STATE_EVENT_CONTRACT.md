# Compose UI State & Event Contract

## Rule
Composable screens render immutable UiState and emit UiEvent/UserAction. They do not call DAOs/services directly.

## Common screen state
Each major screen should account for:
- loading/initializing where actually necessary
- content
- empty
- recoverable error
- offline/AI unavailable when relevant
- permission required when relevant

## Example OrderListUiState
- query
- selectedFilters
- sort
- orders
- isEmpty
- transientMessage?
- isRefreshing only if a real refresh operation exists

## Example AddEditOrderUiState
- form fields
- field errors
- isDirty
- isSaving
- canSave
- saveError?

## Events
Examples:
- SearchChanged
- FilterChanged
- AddOrderClicked
- SaveClicked
- ArchiveClicked
- StatusSelected
- RetryClicked
- ScreenshotSelected
- ConfirmImportClicked

## One-off effects
Navigation, snackbar messages, permission launch requests, and external intents should use a deliberate effect mechanism and must not be accidentally replayed after configuration/state restoration.

## Navigation data
Pass stable IDs between destinations rather than large mutable domain objects whenever possible.

## State restoration
Preserve user-entered form state appropriately across rotation/process recreation where feasible.
