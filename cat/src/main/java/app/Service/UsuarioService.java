package app.Service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication; // Corrigido para a importação correta do Spring
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import app.Repository.UsuarioRepository;
import app.auth.KeycloakService;
import app.auth.UserRequest;
import app.auth.Usuario;
import app.reponse.UserUpdateResponse;

@Service
public class UsuarioService {
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Autowired
	private KeycloakService keycloakService;
	
    private static final Logger log = LoggerFactory.getLogger(KeycloakService.class);


	public String save(UserRequest user) {
		
		String keyclokID = keycloakService.criarUsuarioNoKeycloak(user);
		
		Usuario usuario = new Usuario();
		usuario.setNome(user.getNome());
		usuario.setLogin(user.getLogin());
		usuario.setRole(user.getRole().toString());
		usuario.setKeycloakID(keyclokID);
		

		if (conferirUser(usuario)) {
			throw new RuntimeException("Login já está em uso");
		}

		usuario.setAtivo(true);
		this.usuarioRepository.save(usuario);
		return "Usuário salvo com sucesso";
	}

	public UserUpdateResponse update(UserRequest usuario, long id) {
		log.info("Atualizando Usuário");
		// Define o ID no objeto usuário antes de verificar a existência de login duplicado
		usuario.setId(id);

		String token = "";
		String msg = "";

		Optional<Usuario> optional = this.usuarioRepository.findById(id);
		log.info("Recuperndo o Usuário do DB");


		if (optional.isPresent()) {
			Usuario userInDB = optional.get();
			
			if (!userInDB.getLogin().equals(usuario.getLogin())) {
			    throw new RuntimeException("Não é possível mudar o nome de usuário");
			}
			
			userInDB.setLogin(usuario.getLogin());
			userInDB.setNome(usuario.getNome());
			userInDB.setRole(usuario.getRole());
			userInDB.setAtivo(usuario.isAtivo());
			if (conferirUser(userInDB)) {
				throw new RuntimeException("Login já está em uso");
			}
			log.info("Antes do keycloak");

			keycloakService.atualizarUsuarioNoKeycloak(userInDB.getKeycloakID(), usuario);
			
			userInDB = this.usuarioRepository.save(userInDB);

			msg = "Atualizado com sucesso";
			return new UserUpdateResponse(msg, token);
		} else {
			throw new RuntimeException("Usúario não encontrado");
		}

	}

	public boolean conferirUser(Usuario usuario) {
		Optional<Usuario> existingUser = usuarioRepository.findByLoginIgnoreCase(usuario.getLogin());
		// Verifica se o usuário existe e se o ID é diferente do ID do usuário que está
		// sendo atualizado

		return existingUser.isPresent() && existingUser.get().getId() != usuario.getId();
	}

	public Usuario findById(long id) {
		Optional<Usuario> optional = this.usuarioRepository.findById(id);
		if (optional.isPresent()) {
			return optional.get();
		} else
			throw new RuntimeException("Usúario não encontrado");
	}

	public List<Usuario> findAll(boolean ativo) {

		List<Usuario> users = this.usuarioRepository.findByAtivo(ativo);

		return this.usuarioRepository.findByAtivo(ativo);
	}

	public String delete(Long id) {
		this.usuarioRepository.deleteById(id);
		return "Usuario deletado com sucesso";
	}

	public String disable(Long id) {
		

		Optional<Usuario> optional = this.usuarioRepository.findById(id);
		if (optional.isPresent()) {
			Usuario usuarioInDB = optional.get();
			if (!getUsuarioLogado().getKeycloakID().equals(usuarioInDB.getKeycloakID())) {
				
				UserRequest usuario = new UserRequest();
				
				usuario.setLogin(usuarioInDB.getLogin());
				usuario.setNome(usuarioInDB.getNome());
				usuario.setRole(usuarioInDB.getRole());
				usuario.setAtivo(false);
				
				usuarioInDB.setAtivo(false);
				log.info(usuarioInDB.getKeycloakID());
				this.usuarioRepository.save(usuarioInDB);
				return "Usuário desativado com sucesso!";
			} else {
				throw new RuntimeException("Não é possível se autodesativar");
			}
		} else {
			throw new RuntimeException("Usúario não encontrado");
		}
	}

	public String enable(Long id) {
		Optional<Usuario> optional = this.usuarioRepository.findById(id);
		if (optional.isPresent()) {
			Usuario usuarioInDB = optional.get();
			usuarioInDB.setAtivo(true);
			
			UserRequest usuario = new UserRequest();
			
			usuario.setLogin(usuarioInDB.getLogin());
			usuario.setNome(usuarioInDB.getNome());
			usuario.setRole(usuarioInDB.getRole());
			usuario.setAtivo(true);
			
			this.usuarioRepository.save(usuarioInDB);
			return "Usuário ativado com sucesso!";
		} else {
			throw new RuntimeException("Usúario não encontrado");
		}
	}

	public Usuario getUsuarioLogado() {
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

	    if (authentication instanceof JwtAuthenticationToken jwtToken) {
	        String username = jwtToken.getToken().getClaimAsString("preferred_username");
	        Optional<Usuario> optional = usuarioRepository.findByLoginIgnoreCase(username);
	        return optional.orElse(null);
	    }

	    return null;
	}

	
	public Usuario findByLogin(String login) {
		
		Optional<Usuario> optional = this.usuarioRepository.findByLoginIgnoreCase(login);
		if (optional.isPresent()) {
			return optional.get();
		} else
			throw new RuntimeException("Usúario não encontrado");
	}
	
	
}
