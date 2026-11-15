package com.example.Studio21.security;

import com.example.Studio21.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
/**Crea y válida los JWT. Unica clase que conoce la clave secreta**/
public class JwtService {
    public static final String CLAIM_ROL = "rol";

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(@Value("${jwt.secret}") String secreto,
                      @Value("${jwt.expiration-ms}") long expiracionMs){
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32){
            throw new IllegalStateException("jwt.secret debe tener al menos 32 caracteres  (HS256 exige 256 bits)");

        }
        this.clave = Keys.hmacShaKeyFor(bytes); //Checar porque da errror
        this.expiracionMs = expiracionMs;
    }
    /**
     * sub = id del usuario (el correo no, el correo se puede cambiar en PUT /api/cuenta
     * y el token debe seguir sirviendo). el rol viaja como claim para armar los permisos
     */
    public String generarToken(Usuario usuario){
        Date ahora  = new Date();
        return Jwts.builder() //Hacer el import
                .subject(String.valueOf(usuario.getId()))
                .claim(CLAIM_ROL, usuario.getRol().name())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave)
                .compact();
    }

    /**
     * Verifica firma y vigencia, devuelve el contenido del token.
     * @throws JwtException si el token está alterado,  mal formado o vencid
     */

    public Claims validar(String token) throws JwtException {
        return Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
    }
    public long getExpiracionMs() {
        return expiracionMs;
    }
}
