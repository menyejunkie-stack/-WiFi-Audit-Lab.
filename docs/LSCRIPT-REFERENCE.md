# LAZY Script (lscript) — Security Reference

Upstream: https://github.com/arismelachroinos/lscript  
Version described by upstream README: v2.1.4  
License: GNU GPL-3.0  
Platform: Kali Linux shell toolkit  
Repository status: archived/read-only

## Why this reference is here

LAZY Script is a legacy Kali Linux automation menu that groups many third-party penetration-testing utilities. Its upstream README describes wireless assessment features (including WPA/WPA2 handshake and WPS-related workflows) alongside unrelated tools for exploitation, payload generation, phishing, spoofing, and other offensive activities.

This project links to LAZY Script for **documentation and security-learning context only**. It is not embedded, installed, or executed by WiFi Audit Lab.

## Compatibility

LAZY Script is a Bash/Shell toolkit intended for Kali Linux with root privileges and external system packages. It is not an Android APK library and cannot be run as-is inside this Android app or as a normal GitHub Actions build step.

## Safe integration policy for WiFi Audit Lab

The Android project may implement independent, defensive features inspired by general wireless-security concepts:

- Display nearby network metadata exposed by Android's Wi-Fi APIs.
- Explain SSID, BSSID, channels, bands, signal strength, and advertised security modes.
- Analyze password strength locally without transmitting candidate passwords.
- Provide an offline educational lab using synthetic test data and locally generated test hashes.
- Produce a local security report with hardening recommendations.

Do not import or execute upstream scripts, install their third-party tools, generate payloads, conduct phishing/deauthentication, or attempt password guessing against live Wi-Fi authentication. Any future wireless testing should remain within an isolated lab and use documented, authorized defensive procedures.

## Upstream source

See the upstream README and source before relying on any behavior:  
https://github.com/arismelachroinos/lscript

The upstream project is archived; dependencies and scripts may be outdated or unsafe to run on a current system. Review code and dependencies before any use.
