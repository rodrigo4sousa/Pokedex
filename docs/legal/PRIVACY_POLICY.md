---
layout: default
title: Privacy Policy
---

# Privacy Policy

Last updated: 2026-11-10

This policy describes what the Pokédex Android app does with data. The app is a personal portfolio project with no backend, no accounts, no advertising and no analytics.

## Who is responsible

Rodrigo Sousa, the author of the app. Contact: **rcsousa05@gmail.com**

The author runs no server for the app and receives no data from it.

## What the app stores on your device

The app keeps this data in a local database in its private storage:

| Data | Purpose |
|---|---|
| The Pokémon you caught (name, Pokédex number, time you caught it) | Show your "My Pokédex" list and the caught markers |
| The daily "Who's that Pokémon?" round (date, hidden Pokémon, your wrong guesses, hearts left, result) | Resume the day's round and prevent replaying it |
| Your guess streak (count, date of the last win) | Show your streak |

The app also caches downloaded Pokémon data (up to 20 MB) and images on the device. They are public content, not linked to you.

The app asks for no personal information and requests a single Android permission, `INTERNET`. None of the stored data is sent anywhere by the app.

### Android backup

The app allows Android's Auto Backup and device-to-device transfer for the database file only. If backup is enabled on your device, Google may store a copy of your caught list and game progress in your Google account and restore it on a new device. This is handled by Android and Google, not by the author; you can turn it off in your device's backup settings or delete the copy from your Google account.

## Which services the app contacts

To work, the app downloads public content from these services:

| Service | What is requested | Why |
|---|---|---|
| `pokeapi.co` (PokeAPI) | Pokémon, species, evolution chain and type data | Show Pokémon information |
| `raw.githubusercontent.com` (GitHub, from the PokeAPI/sprites and PokeAPI/cries repositories) | Sprites, official artwork and cries | Show images and play cries |

Like any internet request, these requests reveal to the service, and to the network operators in between, your **IP address**, the address requested (for example, which Pokémon you opened), the time, and standard technical headers such as the user-agent of the HTTP library or media player. The app sends no identifier of its own, no cookies and no API key. The author does not see these requests.

These services have their own privacy practices, which the author does not control:

- PokeAPI: <https://pokeapi.co/>
- GitHub (`raw.githubusercontent.com`): <https://docs.github.com/en/site-policy/privacy-policies/github-general-privacy-statement>

## How to delete your data

- **On your device:** open Android Settings, then Apps, then Pokédex, then Storage, and choose "Clear storage" (removes the database and caches) or "Clear cache" (removes only the caches). Uninstalling the app removes everything it stored.
- **Cloud backup copy:** if Android backup was on, delete the backup in your Google account's backup settings.

## Your rights

The author holds no personal data about you, so there is nothing to access, correct or delete on your behalf: the data is on your device and you control it as described above. Under the GDPR you also have the rights of access, rectification, erasure, restriction, objection and portability, and the right to complain to your national data protection authority. Questions or requests: **rcsousa05@gmail.com**.

## Children

The app is not directed at children under 13 and collects no personal data from anyone.
