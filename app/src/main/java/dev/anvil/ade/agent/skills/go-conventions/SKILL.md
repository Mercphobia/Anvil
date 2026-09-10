---
name: go-conventions
description: Go conventions — always load for Go projects. Covers error handling idiom, go.mod, package convention, defer, and idiomatic Go patterns.
applies_to: [GO]
---

# Go Conventions

You are writing Go. These rules apply to every `.go` file.

## 1. Error Handling — The Go Way

- Errors are values: check them, don't throw them.
- Every function that can fail returns `(T, error)`; the caller must handle the error immediately.
- Never ignore errors with `_` — if you genuinely don't care, write `_ = ...` with a comment explaining why.
- Wrap errors with `fmt.Errorf("context: %w", err)` to preserve the chain.
- Use `errors.Is()` and `errors.As()` for error inspection; never compare error strings.

```go
import (
    "errors"
    "fmt"
)

var ErrNotFound = errors.New("not found")

func GetUser(id int) (*User, error) {
    row := db.QueryRow("SELECT * FROM users WHERE id = ?", id)
    var u User
    err := row.Scan(&u.ID, &u.Name)
    if errors.Is(err, sql.ErrNoRows) {
        return nil, fmt.Errorf("GetUser(%d): %w", id, ErrNotFound)
    }
    if err != nil {
        return nil, fmt.Errorf("GetUser(%d): %w", id, err)
    }
    return &u, nil
}
```

## 2. go.mod and Dependency Management

- `go.mod` is the single source of truth for module identity and dependencies.
- Module path follows the repository: `module github.com/user/project`.
- Run `go mod tidy` after adding/removing imports to sync `go.mod` and `go.sum`.
- `go.sum` is committed to version control — it locks dependency checksums.
- Use `go get <package>@<version>` for explicit version pinning; avoid `@latest` in production.

```
module github.com/anvil/worker

go 1.22

require (
    github.com/gin-gonic/gin v1.9.1
    github.com/rs/zerolog v1.31.0
)
```

## 3. Package Convention

- One package per directory; package name matches directory name in all lowercase, no underscores or mixed case.
- Package name should be short (single word if possible): `user`, `handler`, `store` — not `user_service` or `UserHandler`.
- `package main` only in the binary entrypoint; that package must have `func main()`.
- Exported identifiers (public API) start with uppercase; unexported (internal) start with lowercase.
- Avoid `util`, `common`, `helper` packages — name packages by what they provide, not by what they contain.

```
project/
├── cmd/
│   └── server/
│       └── main.go          // package main
├── internal/
│   ├── user/                 // package user
│   │   ├── service.go
│   │   └── repository.go
│   └── log/                  // package log
│       └── log.go
├── go.mod
└── go.sum
```

## 4. Defer — Resource Cleanup

- `defer` runs in LIFO order — the last defer registered runs first.
- Always `defer file.Close()` or `defer resp.Body.Close()` immediately after opening.
- Deferred functions arguments are evaluated at the `defer` statement, not at execution — wrap in a closure if you need the latest value.
- Recover from panics with `defer` + `recover()` only at goroutine boundaries; never use it for normal control flow.
- Never defer in a loop — the defers pile up until the function returns; use an inline function to scope them.

```go
// Good: immediate defer after open
f, err := os.Open("data.json")
if err != nil {
    return err
}
defer f.Close()

// Good: scoped defer in a loop
for _, path := range paths {
    func() {
        f, err := os.Open(path)
        if err != nil {
            return
        }
        defer f.Close()
        // process f
    }()
}
```

## 5. Interfaces

- Define interfaces where they are consumed, not where they are implemented.
- Interfaces should be small (1-3 methods); large interfaces are a code smell.
- Accept interfaces, return concrete types.
- Use the `interface{}` or `any` sparingly — prefer generics for type-safe containers.
- Compile-time interface satisfaction check: `var _ io.Reader = (*MyType)(nil)`.

```go
// Good: small interface defined at the consumer
type UserStore interface {
    GetByID(id int) (*User, error)
    Save(u *User) error
}

type Service struct {
    store UserStore  // accept interface
}

func NewService(store UserStore) *Service {
    return &Service{store: store}
}
```

## 6. Concurrency

- Share memory by communicating, not communicate by sharing memory — use channels.
- `go func()` launches a goroutine; always know how it exits.
- Use `sync.WaitGroup` to wait for goroutine completion; don't use `time.Sleep` as synchronization.
- `context.Context` is the standard cancellation mechanism: pass it as the first parameter.
- Never start a goroutine without a way to stop it — use `context.Context` or a done channel.

```go
func ProcessItems(ctx context.Context, items []Item) error {
    var wg sync.WaitGroup
    errCh := make(chan error, len(items))

    for _, item := range items {
        wg.Add(1)
        go func(item Item) {
            defer wg.Done()
            if err := process(ctx, item); err != nil {
                errCh <- err
            }
        }(item)
    }

    wg.Wait()
    close(errCh)

    // Collect first error if any
    if err := <-errCh; err != nil {
        return err
    }
    return nil
}
```

## 7. Testing

- Test files: `*_test.go` in the same package (white-box) or `_test` package (black-box).
- Use table-driven tests for multiple cases: define a slice of test cases and loop.
- Subtests with `t.Run()` for isolating individual cases with names.
- Use `testify` for assertions only when stdlib `testing` is too verbose; prefer `if got != want { t.Errorf(...) }`.
- Mock at the interface boundary; use `gomock` or hand-rolled mocks implementing the same interface.

```go
func TestDivide(t *testing.T) {
    tests := []struct {
        name    string
        a, b    float64
        want    float64
        wantErr bool
    }{
        {"positive", 10, 2, 5, false},
        {"by zero", 1, 0, 0, true},
        {"negative", -10, 2, -5, false},
    }

    for _, tt := range tests {
        t.Run(tt.name, func(t *testing.T) {
            got, err := Divide(tt.a, tt.b)
            if (err != nil) != tt.wantErr {
                t.Fatalf("error = %v, wantErr = %v", err, tt.wantErr)
            }
            if got != tt.want {
                t.Errorf("got %v, want %v", got, tt.want)
            }
        })
    }
}
```

## 8. Formatting and Linting

- `gofmt` or `goimports` is non-negotiable — run it on every save, in CI, always.
- `go vet` catches suspicious constructs; run it before every commit.
- `golangci-lint` aggregates multiple linters; configure with `.golangci.yml`.
- CI must run `gofmt -d .`, `go vet ./...`, and `golangci-lint run ./...`.
- Follow Effective Go and the Code Review Comments document for style.

## 9. Zero Values and Initialization

- Go zero-initializes all variables — use this, don't fight it.
- `nil` slices, maps, and channels are valid but read-only/blocking — initialize with `make()` before use.
- Use composite literals for struct initialization: `s := Server{Port: 8080}`.
- Avoid constructors just for zero-value initialization; use constructors when validation or defaults are non-trivial.
- `var buf bytes.Buffer` is ready to use — no constructor needed.

```go
// Good: zero-value ready
var buf bytes.Buffer
buf.WriteString("hello")

// Good: composite literal with named fields
cfg := Config{
    Port:    8080,
    Timeout: 30 * time.Second,
}
```

## 10. Build Tags and Conditional Compilation

- Use build tags for platform-specific code: `//go:build linux` and `_linux.go` suffix.
- Use build tags for optional features: `//go:build integration`.
- The `internal/` package is enforced by the compiler — code inside is only importable by the parent module.
- Use `go generate` for code generation; document generate directives at the top of the file with `//go:generate`.
- Embed static files with `//go:embed` (Go 1.16+) instead of external file loading.