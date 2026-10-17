# Secure Buying Security

A command-line security toolkit written in Java, built to protect an
escrow-style online marketplace such as
[Secure_Buying](https://github.com/MAPULA420/Secure_Buying).

## The Problem

Buyers and sellers on social media platforms face scams every day: fake proof
of payment, false "item not received" claims, wrong items sent, and stolen
accounts. An escrow app holds the money until the deal is done, but it also
needs to protect accounts and keep records that nobody can quietly change.

## What This Toolkit Does

1. **Secure accounts**: password strength checking, BCrypt hashing, and
   account lockout after repeated failed logins.
2. **Tamper-evident audit log**: every transaction event is chained to the
   previous one with SHA-256, so any edit to an old entry is detected.
3. **Threat model**: a STRIDE analysis of how an escrow marketplace can be
   attacked and which part of the toolkit defends against each threat.
4. **Fraud pattern detector** (stretch goal): flags suspicious transaction
   patterns from sample data.

## Built With

- Java 25
- Maven
- BCrypt (password hashing)
- SHA-256 (built into Java)

## How to Run

    mvn compile
    mvn exec:java