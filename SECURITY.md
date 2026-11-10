# Security Policy

## Supported versions

Only the latest release receives fixes. This is a personal project, so fixes are made on a best-effort basis.

## Reporting a vulnerability

Please do not open a public issue for a security problem.

Report it privately, in one of these ways:

- Use GitHub's [private vulnerability reporting](https://github.com/rodrigo4sousa/Pokedex/security/advisories/new) on this repository.
- Email **rcsousa05@gmail.com**.

Include the app version (About screen), your Android version, and the steps to reproduce the problem. You will get a reply as soon as possible, and the report will be handled privately until a fix is released.

## Scope

The app has no backend and no accounts. It stores only the caught Pokémon and the guess game's progress on the device, and it downloads public data over HTTPS from PokeAPI and GitHub (see the [Privacy Policy](docs/legal/PRIVACY_POLICY.md)). Reports about those third-party services themselves should go to their owners.

Release files are signed, and each release has a SHA-256 checksum file. If a download does not match its checksum, do not install it, and tell me.
