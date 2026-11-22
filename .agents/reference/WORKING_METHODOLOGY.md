# Working methodology

Rules every AI agent (and contributor) follows when adding a feature or fixing a bug. A change
is not ready to commit until every item in the checklist below holds.

## Before every commit

1. **The project compiles.** `mvn -B compile` succeeds.
2. **The feature has meaningful unit tests, and they pass.** `mvn -B test` succeeds.
3. **PMD reports 0 warnings.** `CI=true mvn -B verify` succeeds with no PMD violation.

`CI=true mvn -B verify` is the command CI runs. It compiles, runs the tests, and runs Checkstyle,
PMD and SpotBugs, so a green run covers all three rules at once. The `CI=true` part is required:
the PMD and SpotBugs profiles only activate when it is set, and without it a local build can pass
while CI fails.

Never commit, or say a change is done, on the strength of a build that skipped one of these steps.
If a step fails, fix the cause and run the full command again.

## Design

Follow [CLEAN_CODE.md](CLEAN_CODE.md). When a design problem keeps coming back (branching on
type or state, repeated object setup, many objects talking to each other), check
[DESIGN_PATTERNS.md](DESIGN_PATTERNS.md) for a pattern that fits. Use one only when it solves a
problem the code has today.

For engine code, follow [JMONKEYENGINE.md](JMONKEYENGINE.md). When it is not precise enough,
read the official jMonkeyEngine documentation or JavaDoc it links to rather than guessing.

## What counts as a meaningful unit test

- It checks behaviour the feature promises, with concrete inputs and expected outputs.
- It would fail if the logic it covers were broken. A test that cannot fail, or that only calls
  code without asserting a result, does not count.
- It covers the edge cases of the logic: boundaries, zero, negative values, wrap-around angles,
  empty input, and so on.
- It is fast, independent and repeatable, as described in [CLEAN_CODE.md](CLEAN_CODE.md).

Code that needs a display or a running jME application (scene setup, rendering, input wiring)
cannot run in CI tests. Pull the decision-making logic out of it into small methods that take
plain values and return a result, and test those. `GamebaseTest` testing `behindAngle` and
`turnToward` is the example to follow.

Tests use JUnit 3.8.1: extend `junit.framework.TestCase` and name methods `test...`.

## Getting to 0 PMD warnings

- Fix the code rather than silencing the rule.
- Suppress a rule only when the fix would make the code worse, scope the `@SuppressWarnings`
  as narrowly as possible, and add a comment explaining why. Existing suppressions in
  `Gamebase.java` show the expected form.
- Do not edit `config/pmd/pmd-ruleset.xml` to make a change pass. Changing the ruleset is a
  separate decision for the repository owner.
