# 🔬 Plugin DNA Verifier (ASM + MinHash LSH)

A tool for **fingerprinting plugin JARs/ZIPs** using [ASM](https://asm.ow2.io/) for bytecode parsing and **MinHash LSH** for efficient similarity detection.

It supports:
- **One-to-One comparison** → Compare two plugin builds and measure similarity.
- **One-to-Many comparison** → Detect potential matches in a larger collection efficiently, simulating real-world plugin verification.

---

## 📂 Project Structure

- **`Main.kt`** → Entry point. Dispatches commands (`tokenize`, `compare`, `compare-many`).
- **`DNA.kt`** → Abstract representation of a plugin’s "DNA" (classes, methods, fields, packages, etc.).
- **`ZipDNA`** → Tokenizes a plugin ZIP/JAR:
    - Extracts entries
    - Parses `.class` files with ASM
    - Collects classes, fields, and methods
    - Runs **MinHash** and stores signatures into LSH buckets
    - Outputs a DNA JSON fingerprint
- **`CompareOneToOneDNA`** → Compares two DNA JSON files and produces similarity metrics.
- **`CompareOneToManyDNA`** → Compares one DNA JSON against many using LSH buckets. Optimizes comparison from **O(n)** to **O(k)**, where *k* is the number of candidates in the same buckets.
- **`Global`** → Stores IDs of all DNA and a global bucket → DNA index (naive in-memory "database").
- **`Utils`** → Shared helper functions.

---

## 🚀 Usage

Download the standalone **shadow JAR** (`dna`) from [Releases](./releases).

Run with:

```bash
# Tokenize a plugin JAR/ZIP into a DNA JSON
dna tokenize <zipPath> <outJson>

# Compare two DNA JSONs (one-to-one)
dna compare <json1> <json2>

# Compare a DNA JSON against all others in Global.json (one-to-many)
dna compare-many <jsonPath>
