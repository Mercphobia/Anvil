---
name: general-debugging
description: Universal debugging skill — always load for any project. Covers stack trace reading, bisection, logging strategy, and systematic root cause analysis.
applies_to: [NODE_JS, PYTHON, RUST, GO, C_CPP, ANDROID, GENERIC]
---

# General Debugging Conventions

You are debugging code. These rules apply regardless of language or platform.

## 1. Read the Stack Trace First — Thoroughly

- The stack trace is not decoration — it tells you the exact file, line, and call chain of the failure.
- Read from the bottom (where the error originated) upward through the call chain.
- Focus on YOUR code first, not framework internals. If the top frame is in `node_modules` or `site-packages`, trace downward until you hit your own code.
- Every stack trace has these elements: error type, error message, file path, line number, function name. Extract all four before hypothesizing.

```
# Python example:
Traceback (most recent call last):
  File "app/handler.py", line 42, in process    ← YOUR CODE: start here
    result = validate(input)                     ← the failing call
  File "app/validator.py", line 15, in validate ← deeper: the bug lives here
    return int(raw_value)
ValueError: invalid literal for int() with base 10: 'abc'

Interpretation:
- Error type: ValueError
- Message: cannot convert string 'abc' to int
- Root cause: validator.py line 15 — raw_value is 'abc', not a number
- Why: handler.py line 42 passed unvalidated input to validate()
```

## 2. Reproduce Before Fixing

- If you cannot reproduce the bug, you cannot verify the fix.
- Write a minimal reproduction case: the smallest input/code that triggers the same error.
- If the bug is intermittent (race condition, timing), add logging and run the repro in a loop until it triggers.
- Never fix a bug you haven't seen happen — you are guessing, not debugging.

```
Steps:
1. Capture the exact input that caused the failure (query params, request body, file content)
2. Write a test case with that input that FAILS (RED)
3. Fix the code
4. Confirm the test now PASSES (GREEN)
5. Add the test to the permanent test suite so it never regresses
```

## 3. Bisect the Problem Space

- When you don't know where the bug is, halve the search space repeatedly.
- Binary search on code: comment out half the logic. Does the bug persist? If yes, bug is in the active half. If no, bug is in the commented half. Repeat.
- Binary search on commits: `git bisect start` → `git bisect bad` (current) → `git bisect good <known-working-commit>`. Let git binary-search the commit history.
- Binary search on data: process half the input. Does it fail? Narrow the input until you find the single record/line/value that triggers the bug.

```bash
# Git bisect workflow
git bisect start
git bisect bad HEAD                    # current commit is broken
git bisect good a1b2c3d                # last known working commit
# Git checks out a midpoint commit — test it
git bisect good                        # if it works
git bisect bad                         # if it's broken
# Repeat until git identifies the exact commit
git bisect reset                       # return to HEAD when done
```

## 4. Logging Strategy

- Add logging at the INPUT boundary (what data entered the function) and the OUTPUT boundary (what it returned).
- Use different log levels correctly:
  - `ERROR`: something failed, user-impacting, needs attention now.
  - `WARN`: something unexpected but recovered; might indicate a deeper issue.
  - `INFO`: key state transitions (startup, shutdown, request received, request completed).
  - `DEBUG`: internal state useful during investigation (variable values, branch decisions).
- Log messages must be self-contained: include the operation name and the key values. "Failed" is useless; "GetUser(42): connection refused on db-host:5432" is actionable.
- Temporary debug logging is fine — just remove it before committing after the fix.

```python
# Bad: ambiguous
logger.error("Failed")

# Good: self-contained and actionable
logger.error("GetUser(user_id=%d): database connection refused (host=%s, port=%d, timeout=%ds)",
             user_id, db_host, db_port, timeout)

# Bad: no input context
logger.debug("Processing...")

# Good: input and branch context
logger.debug("ProcessOrder(order_id=%s): amount=%d, discount=%s, eligible=%s",
             order_id, amount, discount_code, eligible)
```

## 5. One Change at a Time

- Change ONE thing, then test. Never change multiple variables between tests.
- If you changed three things and the bug went away, you don't know which change fixed it — and you may have introduced a new bug.
- Use `git diff` before every test to know exactly what changed.
- This applies to configuration too: change one environment variable, one DB setting, one timeout at a time.

## 6. Rubber Duck and Explain It

- Explain the bug out loud (or in writing) as if teaching someone else.
- Start from the input, walk through every step of the code, and predict the output.
- When what you predict diverges from what actually happens, that gap IS the bug.
- If you cannot explain the code flow clearly, you do not understand it yet — go read it again.

## 7. Check Assumptions

- Most bugs are assumptions that are no longer true. List yours and verify each one:
  - "This function always receives a non-null value" → add a null check and log when it happens.
  - "This API always returns JSON" → check the Content-Type header and the response body.
  - "This file always exists" → handle FileNotFoundError/ENOENT.
  - "This value is always positive" → add an assertion; it will fire when the assumption fails.
  - "This operation completes in under 1 second" → add a timeout; log when it takes longer.
- Never assume the environment matches your mental model — verify with actual tool output.

## 8. Use the Right Tool for the Question

| Question | Tool |
|---|---|
| What value does this variable have? | Print/debug-log it at the point of interest |
| What path did execution take? | Add log statements at each branch |
| Is this code even reached? | Add a log or `panic`/`throw` at that point |
| What called this function? | Stack trace or `caller` inspection |
| When did this break? | `git bisect` |
| Is this slower than expected? | Profiler (pprof, py-spy, perf, flamegraph) |
| Is memory growing unboundedly? | Memory profiler (heaptrack, Valgrind, pprof heap) |
| What network traffic is happening? | `tcpdump`, Wireshark, or browser DevTools Network tab |
| What SQL queries are running? | Database query log or ORM query logging |

## 9. Defensive Debugging for Intermittent Bugs

- Intermittent bugs are usually: race conditions, network timeouts, garbage collection pauses, or resource exhaustion.
- Add timestamps to every log line (millisecond precision) to reconstruct order of events.
- For concurrency bugs: log thread/goroutine IDs with every message to track which entity did what.
- Run the failing operation in a tight loop with progressive logging until you catch it in the act.
- Use `rr` (record-replay) for deterministic debugging of non-deterministic bugs.

```python
# Thread-aware debug logging
import threading, time, logging
logger = logging.getLogger(__name__)

def debug(msg, *args):
    logger.debug("[t=%d][thread=%s] " + msg,
                 int(time.monotonic() * 1000),
                 threading.current_thread().name,
                 *args)
```

## 10. When to Stop and Ask for Help

- You've been stuck on the same bug for 30+ minutes without a new insight.
- You've verified all your assumptions and they hold — the bug is in code you don't control.
- The error is in a dependency/library — check its issue tracker before patching locally.
- You need domain knowledge you don't have (cryptography, hardware, protocol internals).
- Write up: (a) what you expected, (b) what actually happened, (c) what you've tried, (d) a minimal reproduction. This write-up often surfaces the bug on its own.