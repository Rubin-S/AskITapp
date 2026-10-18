#!/usr/bin/env python3
"""Fail when Kotlin UI code passes an English literal where users will read it.

Android lint's HardcodedText only checks XML layouts, so Compose literals slip through.
Put user-visible text in res/values/strings.xml and res/values-ta/strings.xml.

A literal that is genuinely not translatable (a glyph sample, a place name in fixture data)
can stay if its line ends with `// i18n-ignore`. Sample copy inside @Preview functions is
ignored. String templates are checked only for their literal words, not their ${...} parts.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SOURCES = [ROOT / "app/src/main/java", ROOT / "designsystem/src/main/java"]
CALL = re.compile(r'\b(?:Text\(\s*|(?:text|title|contentDescription|placeholder|message|supportingText)\s*=\s*)"((?:\\.|[^"\\])*)"')
TEMPLATE = re.compile(r"\$\{[^}]*\}|\$[A-Za-z_][A-Za-z0-9_]*")
WORD = re.compile(r"[A-Za-z]{2,}")
ALLOWED = {"Aa"}  # font-style preview glyph in the story text tools


def in_preview(lines, index):
    """True when the enclosing top-level function is annotated with @Preview."""
    for back in range(index, -1, -1):
        if re.match(r"^(private |internal )?fun ", lines[back]):
            return any("@Preview" in line for line in lines[max(0, back - 10):back])
    return False


def main():
    hits = []
    for source in SOURCES:
        for path in sorted(source.rglob("*.kt")):
            lines = path.read_text(encoding="utf-8").splitlines()
            for i, line in enumerate(lines):
                if "i18n-ignore" in line:
                    continue
                for match in CALL.finditer(line):
                    literal = match.group(1)
                    if literal in ALLOWED:
                        continue
                    words = re.sub(r"\$\{.*", "", TEMPLATE.sub("", literal))
                    if not WORD.search(words):
                        continue
                    if in_preview(lines, i):
                        continue
                    hits.append(f"{path.relative_to(ROOT)}:{i + 1}: {line.strip()}")
    if hits:
        print("Hardcoded user-visible strings (move them to string resources, en + ta):")
        print("\n".join(hits))
        return 1
    print("No hardcoded user-visible strings.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
