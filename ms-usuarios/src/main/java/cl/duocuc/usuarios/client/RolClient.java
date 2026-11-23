
package cl.duocuc.usuarios.client;

import cl.duocuc.usuarios.dto.RolDto;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class RolClient {

    private static final List<RolDto> ROLES = List.of(
        new RolDto(1L, "TECNICO"),
        new RolDto(2L, "INGENIERO"),
        new RolDto(3L, "JEFE_TECNICO"),
        new RolDto(4L, "CLIENTE")
    );

    public List<RolDto> listar() {
        return ROLES;
    }

    public RolDto obtener(Long id) {
        return ROLES.stream()
                .filter(rol -> rol.id().equals(id))
                .findFirst()
                .orElse(null);
    }
}
