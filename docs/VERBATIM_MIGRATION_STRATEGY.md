# HyperCell Verbatim Migration Strategy

**STATUS:** APPROVED
**DATE:** November 21, 2025
**OBJECTIVE:** Separate rigorously tested code from Scoop into HyperCell with **ZERO LOGIC CHANGES**.

---

## ⚠️ CRITICAL DIRECTIVES (DO NOT DEVIATE)

1.  **NO LOGIC CHANGES:** Do not modify the internal logic, method signatures, or variable names of any copied Scoop file.
2.  **NO NEW LIBRARIES:** Do not add external dependencies (like Guava, Commons, etc.) that were not present in the original scope.
3.  **MINIMAL INTERVENTION:** Changes are strictly limited to **structural scaffolding** required to make the code compile in the new project.
4.  **PACKAGE PRESERVATION:** Prefer keeping the original `scoop.*` package structure for copied files to avoid breaking internal `import` statements.

---

## 🏗️ STRATEGY: Verbatim Copy & Bridge Aliasing

The approach treats the Scoop code as a "Black Box" to be hosted within HyperCell. We do not refactor the black box; we build the environment it expects.

### Phase 1: Establish Clean Baseline
*   **Action:** `git restore .` and `git clean -fd`.
*   **Goal:** Ensure `hypercell` compiles cleanly in its current state before adding Scoop functions.
*   **Dependency:** Re-create `hypercell-formula/.../HyperCellDate.g4` (required for existing `DateAnalyzer`).

### Phase 2: Verbatim Source Copy
*   **Source:** `../scoop/app/src/main/java/scoop/...`
*   **Destination:** `hypercell-core/src/main/java/scoop/...`
*   **Action:** Copy identified function files (e.g., `FilterFunction.java`, `LookupFunction.java`) and their direct dependencies (e.g., `Criteria.java`) **EXACTLY AS THEY ARE**.
*   **Rule:** Do **NOT** change `package scoop.expression;` to `package io.hypercell...` inside these files. Let them live in their original package namespace within the `hypercell-core` source tree.

### Phase 3: Conflict Resolution (The "Bridge" Pattern)
*   **Scenario:** Copied Scoop code imports `scoop.worksheet.memsheet.MemCell`, but the active system object in HyperCell is `io.hypercell.core.grid.MemCell`.
*   **Solution:** Create a **Bridge Alias** class.
*   **Location:** `hypercell-core/src/main/java/scoop/worksheet/memsheet/MemCell.java`
*   **Content:**
    ```java
    package scoop.worksheet.memsheet;

    import io.hypercell.core.grid.MemCell; // The "Real" class

    // The Bridge
    public class MemCell extends io.hypercell.core.grid.MemCell {
        // Minimal Constructors required by legacy code to compile
        public MemCell(Object value) { super(value); }
        // ... direct delegation only. NO NEW LOGIC.
    }
    ```
*   **Result:** The copied Scoop code finds the class it expects (`scoop...MemCell`), but at runtime, it is operating on the valid HyperCell object.

### Phase 4: Compilation & Iteration
1.  **Build:** Run `./gradlew clean build`.
2.  **Analyze Errors:**
    *   **"Package scoop.x.y does not exist":** -> **COPY** the missing file verbatim from `../scoop`.
    *   **"Symbol not found" (on a specific method):** -> Check if the Bridge Alias needs a missing constructor or helper method that was present in the old class but not the new one. Add *only* a delegate method/constructor.
3.  **Repeat** until compilation succeeds.

### Phase 5: Wiring
*   **Action:** Minimal edit to `io.hypercell.core.expression.Compile.java`.
*   **Detail:** Add `case` statements to instantiate the `scoop.expression.*` function classes where the parser identifies them.

---

## 🛑 "OFF THE RAILS" CHECKLIST

If you find yourself doing any of the following, **STOP**. You are off course.

- [ ] Changing a method body inside a copied Scoop file.
- [ ] Renaming variables.
- [ ] "Cleaning up" imports (unless absolutely necessary for compilation).
- [ ] Adding `implementation 'com.google.guava...'` or similar to build.gradle.
- [ ] Deleting "unused" methods from copied files.
- [ ] Changing the package declaration of a file (e.g. `package scoop` -> `package io.hypercell`).

**When in doubt: COPY VERBATIM.**
