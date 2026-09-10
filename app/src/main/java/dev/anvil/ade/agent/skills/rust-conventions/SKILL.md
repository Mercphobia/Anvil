---
name: rust-conventions
description: Rust conventions — always load for Rust projects. Covers ownership, borrow checker, Cargo, module system, error handling, and idiomatic patterns.
applies_to: [RUST]
---

# Rust Conventions

You are writing Rust. These rules apply to every `.rs` file.

## 1. Ownership and Borrowing

- Prefer borrowing (`&T`, `&mut T`) over moving ownership unless you genuinely need to consume the value.
- Clone deliberately: `clone()` is explicit — if you reach for it, ask whether a borrow would work instead.
- Use `&str` for string parameters the function only reads; use `String` when the function needs to own the data.
- Avoid `Rc<RefCell<T>>` unless single-threaded shared mutability is the only option — prefer `Arc<Mutex<T>>` for multi-threaded or restructure to avoid shared state.
- Lifetime annotations go on the function, not the caller — let the compiler guide you; add `'a` only when the borrow checker demands it.

```rust
// Good: borrow when you only need to read
fn process(name: &str, items: &[Item]) -> Vec<String> {
    items.iter().map(|i| format!("{name}: {i}")).collect()
}

// Good: take ownership only when you must
fn store(name: String, items: Vec<Item>) -> Database {
    // ...
}
```

## 2. Cargo and Project Structure

- Use `Cargo.toml` for all dependencies; never manually download crates.
- Pin dependencies with exact versions for binaries (`=X.Y.Z`); use caret (`^X.Y.Z`) for libraries.
- `Cargo.lock` is committed for binaries, gitignored for libraries.
- Standard project layout:

```
project/
├── Cargo.toml
├── Cargo.lock
├── src/
│   ├── main.rs          # binary entrypoint
│   ├── lib.rs           # library root (re-exports public API)
│   ├── error.rs         # error types
│   └── modules/         # feature modules
├── tests/
│   └── integration_test.rs
├── examples/
│   └── demo.rs
└── benches/
    └── benchmark.rs
```

- Feature flags in `Cargo.toml` for optional functionality; gate with `#[cfg(feature = "x")]`.

## 3. Module System

- `mod` declares a module; `use` brings items into scope.
- `src/lib.rs` is the crate root for libraries; `src/main.rs` for binaries.
- Prefer `pub mod x;` in `lib.rs` and `pub use x::Y;` for re-exports of the public API.
- Avoid `mod` inside functions or blocks — keep module declarations at the file/module top.
- Use `use` paths thoughtfully: `use std::collections::HashMap` is better than `use std::collections::*`.

```rust
// lib.rs — public API re-exports
mod error;
mod handlers;
mod models;

pub use error::{AppError, Result};
pub use handlers::router;
pub use models::User;
```

## 4. Error Handling

- Use `Result<T, E>` for recoverable errors; `panic!` only for unrecoverable bugs (invariants violated).
- Prefer `thiserror` for library error types and `anyhow` for binary/application error handling.
- Every error variant should carry context: include the operation and input that failed.
- Use the `?` operator to propagate errors; never silently discard them with `let _ = ...`.
- Implement `From` for error conversions when wrapping lower-level errors.

```rust
// Library error with thiserror
#[derive(Debug, thiserror::Error)]
pub enum AppError {
    #[error("config not found at {path}: {source}")]
    ConfigNotFound { path: String, source: std::io::Error },

    #[error("invalid input: {0}")]
    Validation(String),
}

// Binary error with anyhow
fn main() -> anyhow::Result<()> {
    let config = std::fs::read_to_string("config.toml")
        .context("Failed to read config.toml")?;
    Ok(())
}
```

## 5. Pattern Matching

- Use `match` exhaustively — the compiler enforces it; embrace it.
- Use `if let` for single-pattern matches where the else branch is empty.
- Use `let ... else` for early returns on pattern failure (Rust 1.65+).
- Destructure structs and enums inline instead of accessing fields with dots.

```rust
// Good: exhaustive match
match result {
    Ok(data) => process(data),
    Err(AppError::Timeout) => retry(),
    Err(e) => return Err(e),
}

// Good: let-else for early return
let Some(config) = load_config() else {
    return Err(AppError::ConfigNotFound);
};
```

## 6. Clippy and Formatting

- `rustfmt` is the one true formatter — configure via `rustfmt.toml`; never fight it.
- `clippy` catches idiomatic issues: `cargo clippy -- -D warnings` in CI.
- Address every clippy warning — they exist because the pattern is a known footgun.
- Run `cargo fmt --check` and `cargo clippy` in CI; block merges on failures.

```toml
# rustfmt.toml
max_width = 100
tab_spaces = 4
edition = "2021"
```

## 7. Testing

- Unit tests go in the same file inside `#[cfg(test)] mod tests { ... }`.
- Integration tests go in `tests/` directory; each file is a separate crate.
- Use `assert_eq!` and `assert!` with descriptive panic messages for failures.
- Prefer `#[test]` over doc-tests for complex examples; doc-tests are for API documentation snippets.
- Test error cases, not just the happy path.

```rust
#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_parse_valid_input() {
        let result = parse("42").unwrap();
        assert_eq!(result, 42);
    }

    #[test]
    fn test_parse_empty_string_returns_err() {
        let err = parse("").unwrap_err();
        assert!(matches!(err, ParseError::EmptyInput));
    }
}
```

## 8. Traits and Generics

- Define traits for shared behavior; implement them for your types.
- Use `impl Trait` in return position for simple cases; use generics with trait bounds for parameters.
- Derive common traits: `#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]`.
- Prefer `&dyn Trait` for runtime polymorphism; `impl Trait` for compile-time dispatch.
- Implement `Display` and `Error` for error types; `FromStr` for types parsed from strings.

```rust
// Good: trait with blanket implementation
pub trait Validatable {
    fn validate(&self) -> Result<(), ValidationError>;
}

impl<T: Validatable> Validatable for Vec<T> {
    fn validate(&self) -> Result<(), ValidationError> {
        for item in self {
            item.validate()?;
        }
        Ok(())
    }
}
```

## 9. Concurrency

- Prefer message passing (`std::sync::mpsc` or `tokio::sync::mpsc`) over shared state.
- When shared state is necessary, use `Arc<Mutex<T>>` or `Arc<RwLock<T>>`.
- Use `tokio` for async I/O; `rayon` for CPU-bound parallelism.
- Never hold a lock across an `.await` point — use `tokio::sync::Mutex` if you must.
- Mark thread-safe types with `Send + Sync` bounds where the compiler demands it.

```rust
// Good: spawn tasks with tokio
let handles: Vec<_> = urls.into_iter().map(|url| {
    tokio::spawn(async move {
        fetch(&url).await
    })
}).collect();

let results: Vec<_> = futures::future::join_all(handles).await;
```

## 10. Performance Mindset

- Zero-cost abstractions are real — trust the compiler; measure before optimizing.
- Use `Vec::with_capacity` when you know the approximate size upfront.
- Prefer iterators over loops — they compose better and are often equally fast.
- Use `Cow<str>` when you sometimes need to own and sometimes can borrow.
- Profile with `cargo flamegraph` before optimizing; never guess the bottleneck.