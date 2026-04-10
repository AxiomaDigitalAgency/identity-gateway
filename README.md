# Identity Gateway

Módulo de identidad y autenticación desacoplado para AION.

## Objetivo
Resolver autenticación mediante estrategias intercambiables por configuración.

## Estrategias iniciales
- CHANNEL
- OAUTH

## Endpoint interno
POST /internal/identity/authenticate

## Configuración
```yaml
aion:
  identity:
    enabled: true
    mode: CHANNEL