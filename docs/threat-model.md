# Threat Model

Secure_Buying began as a Solidity smart contract, where the blockchain made
records hard to alter. After I rebuilt it on AWS (S3 and DynamoDB), I asked
what could now be attacked. This document lists those threats using the STRIDE
categories and says which part of this toolkit or of Secure_Buying defends
against each one.

## What We Are Protecting

- **User accounts**: buyers, sellers and couriers must be who they say they are.
- **Transaction records**: the history of each deal (paid, shipped, delivered,
  refunded) must be accurate and trustworthy.
- **Item photos**: the photo the buyer approved must not be swapped later.
- **Passwords**: they must never be exposed, even if the database leaks.

## Threats

| # | STRIDE category | Threat | Defence |
|---|-----------------|--------|---------|
| 1 | Spoofing | A weak or reused password lets an attacker log in as someone else | Password strength checker and BCrypt hashing (this toolkit) |
| 2 | Spoofing | A fake ID is used at registration | The ID check is simulated in Secure_Buying, and ID numbers are never stored |
| 3 | Tampering | The item photo is swapped after the buyer approved it | S3 versioning keeps every version, and `PhotoUploader` hashes photos. The hash check is not yet linked to the order flow |
| 4 | Tampering | An order status is edited directly in the database | **Known gap.** The original blockchain design prevented this and DynamoDB does not. A tamper-evident log is future work |
| 5 | Repudiation | A buyer says "I never received it" | The courier's ID scan records delivery (simulated in this proof of concept) |
| 6 | Information disclosure | The user database leaks | Only BCrypt hashes are stored, ID numbers are never stored, and S3 public access is blocked |
| 7 | Fraud (business logic) | A buyer files a false damage claim | One-day damage window, and a refund only after the seller confirms the return |

## Out of Scope

- Real payment processing (payment is simulated in Secure_Buying).
- Network-level attacks and cloud infrastructure security.
- Verifying that an ID document is genuine.
- Login and role checks (not built yet).
