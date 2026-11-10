# Releasing

A release is a git tag `vX.Y.Z`. Pushing the tag runs [`release.yml`](../.github/workflows/release.yml), which builds the signed APK and AAB and publishes a GitHub Release.

## Requirements

- **JDK 25** (Temurin). The Gradle daemon is pinned to 25 in `gradle/gradle-daemon-jvm.properties`, and both workflows install it with `actions/setup-java`.
- The Android SDK with API 37, which Android Studio installs.

## Versioning

Tags are `vMAJOR.MINOR.PATCH` for a release and `vMAJOR.MINOR.PATCH-rcN` for a release candidate. Any other tag makes the workflow fail.

- `versionName` is the tag without the `v` (`v1.2.3` gives `1.2.3`). The release workflow passes it explicitly with `-PappVersionName`. For local builds, Gradle uses that property if given, else the latest `v*` tag, else `0.0.0-dev`.
- `versionCode` is computed from the name in `buildSrc/src/main/kotlin/AppVersion.kt`:

  ```
  major * 10,000,000 + minor * 100,000 + patch * 100 + (N for -rcN, 99 for a final release)
  ```

  So `1.2.3-rc1` is `10200301` and `1.2.3` is `10200399`: a release candidate always sorts below its final release, and a tag always gives the same code. Limits: major up to 214, minor up to 99, patch up to 999, N from 1 to 98. Other suffixes (`-beta`, `+build`) and out-of-range numbers fail the build.
- A `-rcN` tag marks the GitHub Release as a pre-release. Do not upload pre-releases to the Play Console.
- The rules are covered by unit tests: `./gradlew -p buildSrc test`.

## Signing key

Create the keystore once and keep it out of the repository (`*.jks` and `*.keystore` are ignored):

```bash
keytool -genkeypair -v \
  -keystore pokedex-release.jks \
  -alias pokedex \
  -keyalg RSA -keysize 4096 -validity 10000
```

Back it up somewhere safe, with its passwords. If the key is lost, installed copies can no longer be updated.

Encode it for GitHub:

```bash
# Linux, macOS, Git Bash
base64 -w0 pokedex-release.jks > pokedex-release.jks.base64
```

```powershell
# Windows PowerShell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("pokedex-release.jks")) | Set-Content -NoNewline pokedex-release.jks.base64
```

Add four repository secrets (Settings, Secrets and variables, Actions). The release workflow fails if any of them is missing; a release is never built unsigned:

| Secret | Value |
|---|---|
| `KEYSTORE_BASE64` | The content of `pokedex-release.jks.base64` |
| `KEYSTORE_PASSWORD` | The keystore password |
| `KEY_ALIAS` | `pokedex` (the alias above) |
| `KEY_PASSWORD` | The key password |

Delete the `.base64` file afterwards.

## Cutting a release

1. Move the entries under `## [Unreleased]` in `CHANGELOG.md` to a new `## [X.Y.Z] - YYYY-MM-DD` section, and update the links at the bottom.
2. Commit the change on `main` and wait for CI to pass.
3. Tag and push:

   ```bash
   git tag -a vX.Y.Z -m "vX.Y.Z"
   git push origin vX.Y.Z
   ```

4. The workflow fails early if the tag is malformed, if `CHANGELOG.md` has no section for it, or if a signing secret is missing. After the build it checks that the APK's `versionName` equals the tag and that `apksigner verify` passes with a non-debug certificate. Then it attaches `pokedex-vX.Y.Z.apk`, `pokedex-vX.Y.Z.aab` and `pokedex-vX.Y.Z.sha256` to the release, with the changelog section as the notes.
5. The R8 `mapping.txt` of the release is kept as a workflow artifact (`r8-mapping-vX.Y.Z`, 90 days), not as a release asset. Download it from the workflow run if you need to read a release stack trace; after 90 days it is gone, so save it if the release matters.

To verify a download:

```bash
sha256sum -c pokedex-vX.Y.Z.sha256
```

To publish to the Play internal track as well, see the commented step in `release.yml`.

## Building locally

Without the signing variables, `./gradlew assembleRelease` produces an unsigned, minified APK. To sign it, set `KEYSTORE_FILE` (a path), `KEYSTORE_PASSWORD`, `KEY_ALIAS` and `KEY_PASSWORD` as environment variables or Gradle properties.

The release build uses R8. Its keep rules are in `app/src/main/keepRules/rules.keep`; after changing dependencies, install a release build on a device and check that the list, a detail page, catching and the guess game still work.
