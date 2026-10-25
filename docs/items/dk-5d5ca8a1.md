---
id: dk-5d5ca8a1
type: task
created: 2026-10-25
status: todo
since: 2026-10-25
area: structure
priority: P1
rank: zzzzzn
parent:
fixes: []
blocked_by: []
relates: []
---
# ShopSessionService: extract ShopRequestRouter; move unbound-register flow to ShopBindingService

Baseline review 2026-10-25 (componentization). apothecary/shop/session/ShopSessionService.java combines session registry, menu opening, validation, rate limiting, history paging, a 25-case dispatcher calling five services directly, and the unbound-register create/reconnect flow with linkThenClose.
