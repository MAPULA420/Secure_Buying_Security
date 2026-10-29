# Threat Model

Secure_Buying began as a Solidity smart contract, where the blockchain made records hard to alter. After the rebuild on AWS (S3 and DynamoDB), the account layer and records needed their own protection. This document lists what could now be attacked, using the STRIDE categories, and says which part of this toolkit or of Secure_Buying defends against each threat.

## What We Are Protecting

- **User accounts**: buyers, sellers and couriers must be who they say they are.
- **Transaction records**: the history of each deal (paid, shipped, delivered, refunded) must be accurate and trustworthy.
- **Item photos**: the photo the buyer approved must not be swapped later.
- **Passwords**: they must never be exposed, even if the database leaks.

## Threats

| # | STRIDE category | Threat | Defence |
|---|---|---|---|
| 1 | Spoofing | A weak or reused password lets an attacker log in as someone else | **This toolkit:** password strength checker rejects weak passwords at registration, and BCrypt hashing is used for storage |
| 2 | Spoofing | An attacker guesses passwords again and again at login | **This toolkit:** the account locks for 60 seconds after 3 failed attempts, and BCrypt (cost 12) makes each guess slow |
| 3 | Spoofing | A fake ID is used at registration | **Secure_Buying:** the ID check is simulated, and ID numbers are never stored |
| 4 | Tampering | The item photo is swapped after the buyer approved it | **Secure_Buying:** S3 versioning keeps every version, and `PhotoUploader` hashes photos. The hash check is not yet linked to the order flow |
| 5 | Tampering | A record or status is edited, for example "damaged" changed to "delivered" | **This toolkit:** the hash-chained audit log detects an edited entry and reports the line. It is not yet connected to Secure_Buying's DynamoDB orders |
| 6 | Repudiation | "I never received it" or "I never did that" | **Secure_Buying:** the courier's ID scan records delivery (simulated). **This toolkit:** registrations, logins, lockouts and every allowed or denied action are written to the audit log with a timestamp |
| 7 | Information disclosure | The user database leaks | **This toolkit:** only salted BCrypt hashes are stored. **Secure_Buying:** ID numbers are never stored, and S3 public access is blocked |
| 8 | Denial of service | An attacker fails logins on someone else's account to lock them out | **This toolkit:** the lock is temporary and expires by itself after 60 seconds |
| 9 | Elevation of privilege | A buyer performs a seller-only or courier-only action | **This toolkit:** role-based access control gives each action to one role, and denied attempts are logged |
| 10 | Fraud (business logic) | A buyer files a false damage claim | **Secure_Buying:** one-day damage window, and a refund only after the seller confirms the return |

## Known Gaps

These are not defended yet. They are the next steps.

- **Audit log limits (row 5):** editing an entry is detected, but deleting the newest entries, or rewriting the whole file, is not. Keeping the latest hash in a separate place (for example an S3 object) would close this.
- **Not connected to AWS (rows 5 and 9):** the audit log, users and roles are plain files in this toolkit and are not yet wired into Secure_Buying's DynamoDB orders.
- **Roles are stored in a file (row 9):** someone with access to `users.txt` could change a role. A database with access control would fix this.
- **Account enumeration (rows 2 and 8):** the "account is locked" message only appears for real usernames, so it tells an attacker that the username exists.
- **Photo hash not linked to the order flow (row 4).**

## Out of Scope

- Real payment processing (payment is simulated in Secure_Buying).
- Network-level attacks and cloud infrastructure security.
- Verifying that an ID document is genuine.