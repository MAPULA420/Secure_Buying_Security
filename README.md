# Secure Buying Security

A command-line security toolkit written in Java, built to protect an
escrow-style online marketplace such as
[Secure_Buying](https://github.com/MAPULA420/Secure_Buying).

## The Problem

Buyers and sellers on social media platforms face scams every day: fake proof
of payment, false "item not received" claims, wrong items sent, and stolen
accounts. An escrow app holds the money until the deal is done, but it also
needs to protect accounts and keep records that nobody can quietly change.

Secure_Buying started as a Solidity smart contract, where the blockchain
protected the records. After moving it to AWS, the account layer needed its
own protection. This toolkit secures that layer.

## What This Toolkit Does

1. **Password strength checker**: rates a password, explains what is weak
   about it, and flags common passwords.
2. **Secure registration**: rejects weak passwords and stores only a BCrypt
   hash, never the password itself.
3. **Threat model**: a STRIDE analysis of how an escrow marketplace can be
   attacked and which part of this toolkit or of Secure_Buying defends
   against each threat, including one known gap (see `docs/threat-model.md`).

## Built With

- Java 25
- Maven
- BCrypt (password hashing)
