package fly.be.flyflix.auth.controller.dto;



public record LoginResponse(String accessToken, long expiresIn, String refreshToken) { }

