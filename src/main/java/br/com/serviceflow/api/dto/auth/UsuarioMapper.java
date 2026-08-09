package br.com.serviceflow.api.dto.auth;

import br.com.serviceflow.api.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {
    public UsuarioResponse toDto(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(), usuario.getEmpresa().getId(), usuario.getNome(),
                usuario.getEmail(), usuario.getPerfil(), usuario.getTrocaSenhaObrigatoria());
    }
}
