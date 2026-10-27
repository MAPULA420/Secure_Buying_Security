# Threat Model

This document lists how an escrow-style marketplace such as Secure_Buying
could be attacked, using the STRIDE categories, and which part of this
toolkit defends against each threat.

## What We Are Protecting

- **User accounts**: buyers, sellers and couriers must be who they say they are.
- **Transaction records**: the history of each deal (paid, shipped, delivered,
  refunded) must be accurate and trustworthy.
- **Item photos**: the photo the buyer approved must not be swapped later.
- **Passwords**: they must never be exposed, even if the database leaks.

## Threats

| # | STRIDE category | Threat | Defence in this toolkit |
|---|-----------------|--------|-------------------------|
| 1 | Spoofing | An attacker guesses or reuses a weak password and logs in as someone else | Password strength checker, BCrypt hashing, account lockout (planned) |
| 2 | Spoofing | A fake or borrowed ID, or a photo of a person, is used to pass the ID and face check | Real ID and face checks are future work in Secure_Buying; failed checks should be logged |
| 3 | Spoofing | One person registers many fake accounts to run scams | Fraud detector (stretch goal) |
| 4 | Tampering | An order status is edited, for example from "damaged" to "delivered" | Hash-chained audit log and a verifier that detects edited entries (planned) |
| 5 | Tampering | The item photo is swapped after the buyer approved it | Compare the stored SHA-256 photo hash with a fresh hash (stretch goal) |
| 6 | Repudiation | A seller says "I never shipped" or a buyer says "I never received it" | Every scan and status change is recorded in the audit log (planned) |
| 7 | Information disclosure | The user database leaks and passwords are exposed | Only BCrypt hashes are stored, never plain-text passwords |
| 8 | Denial of service | An attacker fails logins on someone else's account to lock them out | Lockout is temporary, not permanent (planned) |
| 9 | Elevation of privilege | A buyer performs seller-only or courier-only actions | Out of scope here; the full app needs role checks |
| 10 | Fraud (business logic) | A buyer repeatedly reports false damage inside the one-day window | Fraud detector (stretch goal) |

## Out of Scope

- Real payment processing (payment is simulated in Secure_Buying).
- Network-level attacks and cloud infrastructure security.
- Verifying that an ID document is genuine.
