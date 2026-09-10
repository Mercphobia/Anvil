---
name: python-conventions
description: Python engineering conventions — always load for Python projects. Covers PEP8, type hints, virtual environments, dependency management, and project structure.
applies_to: [PYTHON]
---

# Python Conventions

You are writing Python. These rules apply to every Python file you generate or edit.

## 1. PEP8 Compliance

- 4-space indentation, no tabs.
- 79-character line limit for code; 72 for docstrings and comments.
- Imports ordered in three groups separated by blank lines: standard library, third-party, local.
- Two blank lines before top-level functions and classes; one blank line between methods.
- snake_case for functions, variables, modules; PascalCase for classes; UPPER_CASE for constants.
- Spaces around operators and after commas, no extra spaces inside brackets.

```python
import os
import sys

import requests

from .utils import helper

MAX_RETRIES = 3

class DataProcessor:
    def process_item(self, item: dict) -> str:
        return item.get("name", "").strip()
```

## 2. Type Hints Are Mandatory

- Every function signature must have parameter and return type annotations.
- Use `from __future__ import annotations` for forward references (Python 3.10+ style).
- Prefer `list[dict]` over `List[Dict]` (Python 3.9+ built-in generics).
- Use `Optional[X]` / `X | None` for nullable types; never assume None is fine without declaring it.
- mypy-compatible: if you annotate, the code must pass a type checker.

```python
from __future__ import annotations

def fetch_user(user_id: int) -> dict[str, str] | None:
    response = requests.get(f"/api/users/{user_id}")
    return response.json() if response.ok else None
```

## 3. Virtual Environments

- Always use a virtual environment — never install packages globally.
- Create: `python -m venv .venv` (prefer `.venv` as the directory name).
- Activate before any pip/npm equivalent: `.venv/bin/activate` (Linux/macOS) or `.venv\Scripts\activate` (Windows).
- `.venv/` must be in `.gitignore`.
- Check `sys.prefix` to confirm you are running inside a venv before installing anything.

## 4. Dependency Management

- `requirements.txt` for production dependencies, pinned with `==`.
- `requirements-dev.txt` for linting/testing tools (pytest, ruff, mypy).
- When adding a dependency: install it, then run `pip freeze > requirements.txt` and prune unrelated entries.
- Never commit a `requirements.txt` with unpinned versions or dozens of transitive dependencies.
- Prefer `pyproject.toml` for packages; `requirements.txt` for applications and scripts.

```requirements.txt
# requirements.txt
requests==2.31.0
pydantic==2.5.0
```

## 5. Project Structure

```
project/
├── .venv/              # virtual environment (gitignored)
├── src/
│   └── package_name/
│       ├── __init__.py
│       ├── core.py
│       └── utils.py
├── tests/
│   ├── __init__.py
│   └── test_core.py
├── requirements.txt
├── requirements-dev.txt
├── pyproject.toml       # or setup.cfg for older projects
└── README.md
```

- Source code lives under `src/` (avoids accidental imports of the package root).
- Tests mirror the source structure under `tests/`.
- Every directory with Python modules must have `__init__.py`.
- Script entry points go in `__main__.py` or a top-level `main.py`.

## 6. Error Handling

- Catch specific exceptions, never bare `except:` or `except Exception:`.
- Use `try/finally` or context managers (`with`) for resource cleanup.
- Re-raise with `raise ... from e` to preserve the exception chain.
- Avoid swallowing errors silently — at minimum, log the traceback.

```python
try:
    data = json.loads(raw)
except json.JSONDecodeError as e:
    raise ValueError(f"Invalid JSON from {source}") from e
```

## 7. Docstrings

- Every public module, class, and function has a docstring.
- Use triple-quote `"""` Google-style or NumPy-style docstrings.
- First line is a concise summary; blank line; then details/args/returns/raises.

```python
def divide(a: float, b: float) -> float:
    """Return a divided by b.

    Args:
        a: Numerator.
        b: Denominator.

    Returns:
        The quotient a / b.

    Raises:
        ZeroDivisionError: If b is zero.
    """
    if b == 0:
        raise ZeroDivisionError("Denominator must be non-zero")
    return a / b
```

## 8. Linting and Formatting

- Ruff for linting and formatting (replaces flake8, isort, black).
- Run `ruff check . && ruff format .` before committing.
- CI must block merges with unresolved lint errors.
- pyproject.toml configuration:

```toml
[tool.ruff]
line-length = 100
target-version = "py311"

[tool.ruff.lint]
select = ["E", "F", "I", "N", "W"]
```

## 9. Testing

- pytest is the default test runner.
- Test files named `test_*.py`; test functions named `test_*`.
- One assert per test; descriptive failure messages when the assert is not self-documenting.
- Use fixtures for shared setup; avoid test interdependence.
- Aim for tests that run in under 2 seconds; mock network and filesystem.

```python
import pytest
from src.package_name.core import divide

def test_divide_positive_numbers():
    assert divide(10.0, 2.0) == 5.0

def test_divide_by_zero_raises():
    with pytest.raises(ZeroDivisionError):
        divide(1.0, 0.0)
```

## 10. Async Python

- Prefer `asyncio` for I/O-bound work; use `async def` / `await`.
- Never mix `asyncio.run()` inside an existing event loop.
- Use `httpx.AsyncClient` for async HTTP; `aiofiles` for async file I/O.
- Run blocking code in a thread pool: `await asyncio.to_thread(blocking_func)`.