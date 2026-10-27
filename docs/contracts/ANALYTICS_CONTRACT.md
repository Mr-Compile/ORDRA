# Local Analytics Contract

All analytics are computed locally from Room.

Canonical metrics:
- order count by period
- total spending by period
- spending by platform
- spending by category
- delivered value
- pending/active value
- outstanding COD
- average order value

## Rules
- Use minor currency units.
- Do not mix currencies into one monetary total without an explicit conversion system. V1 should normally filter/group by currency instead.
- Define whether cancelled/refunded orders contribute to each metric.
- Date range boundaries must be deterministic.
- UI receives already-defined domain analytics models rather than implementing financial aggregation itself.
