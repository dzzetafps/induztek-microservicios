
package cl.duocuc.usuarios.config;

import cl.duocuc.usuarios.client.RolClient;
import cl.duocuc.usuarios.dto.RolDto;
import cl.duocuc.usuarios.model.Usuario;
import cl.duocuc.usuarios.repository.UsuarioRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public UserDetailsService userDetailsService(
            UsuarioRepository repositorio,
            RolClient roles) {

        return correo -> {
            Usuario usuario = repositorio.findByCorreo(correo)
                    .orElseThrow(() ->
                            new UsernameNotFoundException("Usuario no encontrado"));

            RolDto rol = roles.obtener(usuario.getRolId());

            if (rol == null) {
                throw new UsernameNotFoundException("Rol no válido");
            }

            return User.withUsername(usuario.getCorreo())
                    .password(usuario.getContrasena())
                    .roles(rol.nombre())
                    .build();
        };
    }
@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
        throws Exception {

        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                    session.sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            HttpMethod.POST,
                            "/api/usuarios/login").permitAll()
                    .requestMatchers(
                            HttpMethod.POST,
                            "/api/usuarios").hasRole("JEFE_TECNICO")
                    .requestMatchers(
                            HttpMethod.DELETE,
                            "/api/usuarios/*").hasRole("JEFE_TECNICO")
                    .anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
