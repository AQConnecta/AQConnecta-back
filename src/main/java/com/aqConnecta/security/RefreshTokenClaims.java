package com.aqConnecta.security;

import java.util.UUID;

public record RefreshTokenClaims(UUID id, String email) {
}
