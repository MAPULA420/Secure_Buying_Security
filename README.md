# Secure Buying Security

A command-line security toolkit written in Java, built to protect an escrow-style online marketplace such as [Secure_Buying](https://github.com/MAPULA420/Secure_Buying).

## The Problem

Buyers and sellers on social media platforms face scams every day: fake proof of payment, false "item not received" claims, wrong items sent, and stolen accounts. An escrow app holds the money until the deal is done, but it also needs to protect accounts and keep records that nobody can quietly change.

Secure_Buying started as a Solidity smart contract, where the blockchain protected the records. After moving it to AWS, the account layer and the records needed their own protection. This toolkit adds that protection.

## What This Toolkit Does

1. **Password strength checker**: rates a password, explains what is weak about it, and flags common passwords.
2. **Secure registration**: rejects weak passwords, assigns a role (buyer, seller or courier), and stores only a BCrypt hash, never the password.
3. **Login with temporary lockout**: verifies the password against the stored hash. After 3 failed attempts the account is locked for 60 seconds, then unlocks by itself.
4. **Role-based access control**: every marketplace action belongs to one role, so a buyer cannot perform seller-only actions. Denied attempts are logged.
5. **Hash-chained audit log**: each log entry contains the hash of the entry before it, so editing any entry is detected and the broken line is reported.
6. **Threat model**: a STRIDE analysis of how an escrow marketplace can be attacked and which part of this toolkit or of Secure_Buying defends against each threat, including known gaps (see `docs/threat-model.md`).

## Built With

- Java 25
- Maven
- BCrypt (password hashing)
- SHA-256 (audit log chaining, from the Java standard library)

