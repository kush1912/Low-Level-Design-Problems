# Inventory Management System - REST API

A Spring Boot demo of a layered REST API (controller -> middleware ->
service -> data layer) built around a single `InventoryItem` resource, using
an in-memory store. Exercises the full HTTP verb set, including the newly
standardized **HTTP `QUERY`** method (RFC 10008).

## Running

The Gradle build is pinned to run this app:

```
./gradlew bootRun
```

Server starts on `http://localhost:8087` and seeds 6 sample items on boot
(`config/DataSeeder`).

## Architecture

```
controller/   InventoryController        - REST endpoints (HTTP <-> DTOs)
middleware/   RequestLoggingFilter        - cross-cutting request/response logging (Servlet Filter)
exception/    GlobalExceptionHandler      - centralized error -> ApiError translation (@RestControllerAdvice)
service/      InventoryService(Impl)      - business rules, filtering/sorting/pagination
repository/   InventoryRepository (+Impl) - data layer, in-memory ConcurrentHashMap
model/        InventoryItem, Category, ItemStatus
dto/          Create/Update/StockAdjustment requests, ItemResponse, PagedResponse, ItemQueryRequest
config/       DataSeeder                  - sample data on startup
```

`status` (`IN_STOCK` / `LOW_STOCK` / `OUT_OF_STOCK`) is always derived from
`quantity` vs `reorderThreshold`; clients never set it directly.

## Endpoints

| Method  | Path                                   | Purpose                                   |
|---------|-----------------------------------------|--------------------------------------------|
| POST    | `/api/v1/inventory-items`               | Create an item                            |
| GET     | `/api/v1/inventory-items/{id}`          | Fetch one item                            |
| GET     | `/api/v1/inventory-items`                | List, with filter/sort/paginate params    |
| PUT     | `/api/v1/inventory-items/{id}`          | Full replace of an item                   |
| PATCH   | `/api/v1/inventory-items/{id}/stock`    | Partial update: adjust stock by a delta   |
| DELETE  | `/api/v1/inventory-items/{id}`          | Delete an item                            |
| QUERY   | `/api/v1/inventory-items/query`          | Complex search via JSON body (see below)  |

### `GET` list - filtering, sorting, pagination

```
GET /api/v1/inventory-items?category=ELECTRONICS&status=IN_STOCK&minPrice=10&maxPrice=200&search=mouse&page=0&size=10&sortBy=price&sortDir=desc
```

All params are optional. `sortBy` accepts `name` (default), `price`,
`quantity`, `createdAt`, `status`. `sortDir` is `asc` (default) or `desc`.
Response is a `PagedResponse`: `{ content, page, size, totalElements, totalPages, sortBy, sortDir }`.

### `QUERY` - the new HTTP method (RFC 10008)

`QUERY` sits between `GET` and `POST`: safe/read-only/cacheable like `GET`,
but carries a JSON body like `POST`, so it can express filters a query
string can't cleanly hold (e.g. several categories/statuses at once):

```
QUERY /api/v1/inventory-items/query
Content-Type: application/json

{
  "categories": ["ELECTRONICS", "TOYS"],
  "statuses": ["IN_STOCK", "LOW_STOCK"],
  "minPrice": 10,
  "maxPrice": 200,
  "page": 0,
  "size": 10,
  "sortBy": "quantity",
  "sortDir": "asc"
}
```

Spring Framework 6.2.x (used here, via Boot 3.4.4) doesn't yet expose
`RequestMethod.QUERY` on `@RequestMapping` (tracked upstream in
[spring-framework#36988](https://github.com/spring-projects/spring-framework/issues/36988)),
but the servlet dispatcher still forwards unrecognized verbs into the normal
handler-mapping flow. `InventoryController.queryItems` maps the path without
restricting `method` and manually verifies `request.getMethod() == "QUERY"`,
returning `405 Method Not Allowed` for anything else - documented in the
Javadoc on that handler.

`curl` doesn't need special flags beyond `-X QUERY`:

```
curl -X QUERY http://localhost:8087/api/v1/inventory-items/query \
  -H "Content-Type: application/json" \
  -d '{"categories":["ELECTRONICS"],"page":0,"size":5}'
```

PowerShell's `Invoke-RestMethod` needs `-CustomMethod QUERY` (the `-Method`
parameter only accepts a fixed enum).

## Error responses

Every failure returns a uniform `ApiError` body via `GlobalExceptionHandler`:

```json
{
  "timestamp": "...",
  "status": 404,
  "error": "Not Found",
  "message": "Inventory item not found with id: ...",
  "path": "/api/v1/inventory-items/..."
}
```

| Situation                         | Status |
|------------------------------------|--------|
| Item id not found                  | 404    |
| Duplicate SKU on create             | 409    |
| Bean validation failure             | 400    |
| Stock delta would go negative       | 400    |
| Wrong verb on `/query`              | 405    |
| Unhandled exception                 | 500    |

## Sample end-to-end curl flow

```bash
# Create
curl -X POST http://localhost:8087/api/v1/inventory-items \
  -H "Content-Type: application/json" \
  -d '{"sku":"SKU-2001","name":"Test Widget","category":"ELECTRONICS","unitPrice":25.5,"quantity":5,"reorderThreshold":2}'

# Read
curl http://localhost:8087/api/v1/inventory-items/{id}

# Full update
curl -X PUT http://localhost:8087/api/v1/inventory-items/{id} \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Widget v2","category":"ELECTRONICS","unitPrice":30,"quantity":5,"reorderThreshold":2}'

# Partial stock update
curl -X PATCH http://localhost:8087/api/v1/inventory-items/{id}/stock \
  -H "Content-Type: application/json" \
  -d '{"delta":-3,"reason":"sold"}'

# Delete
curl -X DELETE http://localhost:8087/api/v1/inventory-items/{id}
```
