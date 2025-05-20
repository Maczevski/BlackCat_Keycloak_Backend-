package app.auth;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.Service.UsuarioService;
import lombok.extern.java.Log;

@Service
public class KeycloakAuthService {

	@Autowired
	private UsuarioService usuarioService;

	private final String keycloakUrl = "https://backend.blackcat.local:9443/realms/projeto-mensal/protocol/openid-connect/token";
	private final String clientId = "black-cat";
	private final String clientSecret = "ppPkIAuudwAQvctJnTtw5CULHRj1bB6s";
	private Logger log = LoggerFactory.getLogger(UsuarioService.class);

	public String authenticate(Login login) {
		

		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("grant_type", "password");
		form.add("client_id", clientId);
		form.add("client_secret", clientSecret);
		form.add("username", login.getUsername());
		form.add("password", login.getPassword());

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

		try {

			ResponseEntity<Map> response = restTemplate.exchange(keycloakUrl, HttpMethod.POST, request, Map.class);

			Map<String, Object> body = response.getBody();
			if (body == null || !body.containsKey("access_token")) {
				throw new RuntimeException("Token não retornado");
			}

			String accessToken = (String) body.get("access_token");

			List<String> roles = getRolesFromToken(accessToken);

			List<String> rolesPermitidas = List.of("admin", "blackcat-user", "user-default");

			boolean autorizado = roles.stream().anyMatch(rolesPermitidas::contains);

			if (!autorizado) {
				throw new RuntimeException("Usuário não tem permissão para acessar o sistema");
			}
			
			syncKeycloakUser(accessToken, login.getPassword());

			return accessToken;

		} catch (HttpClientErrorException e) {
			log.error("Bateu aqui");
			throw new RuntimeException("Erro na autenticação: " + e.getMessage());
		} catch (Exception e) {
			throw new RuntimeException("Erro inesperado na autenticação: " + e.getMessage());
		}
	}

	public void syncKeycloakUser(String token, String password) {

		String username = this.getUserName(token);

		List<String> roles = getRolesFromToken(token);

		List<String> rolesPermitidas = List.of("admin", "blackcat-user", "user-default");

		boolean autorizado = roles.stream().anyMatch(rolesPermitidas::contains);
		try {
			Usuario user = usuarioService.findByLogin(username);
			log.info("Buscando Usuario por Login");

		} catch (RuntimeException e) {
			if (e.getMessage().equals("Usúario não encontrado") && autorizado) {
				UserRequest newUser = new UserRequest();
				newUser.setLogin(username);
				newUser.setSenha(password);
				newUser.setAtivo(true);
				newUser.setNome(getFirstName(token));
				if (rolesPermitidas.contains("admin") || rolesPermitidas.contains("black-cat-role")) {
					newUser.setRole("GESTOR");
				}else {
					newUser.setRole("FUNCIONARIO");
				}
				
				usuarioService.save(newUser);
			}
		} catch (Exception e) {
			throw new RuntimeException("Não foi possível realizar a autenticação" + e.getMessage());
		}

	}

	public List<String> getRolesFromToken(String token) {
		try {
			String[] chunks = token.split("\\.");
			if (chunks.length != 3) {
				throw new RuntimeException("Formato de token inválido");
			}

			String payload = new String(Base64.getUrlDecoder().decode(chunks[1]));

			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> claims = mapper.readValue(payload, Map.class);

			Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");
			if (realmAccess == null || !realmAccess.containsKey("roles")) {
				return List.of();
			}

			List<String> roles = (List<String>) realmAccess.get("roles");

			return roles;

		} catch (Exception e) {
			throw new RuntimeException("Erro ao decodificar o token: " + e.getMessage());
		}
	}

	public String getUserName(String token) {
		try {
			String[] chunks = token.split("\\.");
			if (chunks.length != 3) {
				throw new RuntimeException("Formato de token inválido");
			}

			String payload = new String(Base64.getUrlDecoder().decode(chunks[1]));

			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> claims = mapper.readValue(payload, Map.class);

			String preferredUsername = (String) claims.get("preferred_username");
			if (preferredUsername.isEmpty() || preferredUsername.isBlank()) {
				return "";
			}
			return preferredUsername;

		} catch (Exception e) {
			throw new RuntimeException("Erro ao decodificar o token: " + e.getMessage());
		}
	}

	public String getFirstName(String token) {
		try {
			String[] chunks = token.split("\\.");
			if (chunks.length != 3) {
				throw new RuntimeException("Formato de token inválido");
			}

			String payload = new String(Base64.getUrlDecoder().decode(chunks[1]));

			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> claims = mapper.readValue(payload, Map.class);

			String firstName = (String) claims.get("given_name");
			if(firstName == null) {
				return getUserName(token);
			}else if (firstName.isEmpty() || firstName.isBlank()) {
				return getUserName(token);
			}
			return firstName;

		} catch (Exception e) {
			throw new RuntimeException("Erro ao decodificar o token: " + e.getMessage());
		}
	}

	Boolean verifyUserInDb() {

		return false;
	}
}
