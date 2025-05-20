package app.auth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


@Service
public class KeycloakService {

    private final String serverUrl = "https://backend.blackcat.local:9443";
    private final String realm = "projeto-mensal";
    private final String adminUsername = "admin";
    private final String adminPassword = "admin";
    private final String clientId = "black-cat";
    private final String clientSecret = "ppPkIAuudwAQvctJnTtw5CULHRj1bB6s";
    

    public String criarUsuarioNoKeycloak(UserRequest request) {
    	
    	String token = getAdminToken();
    	
    	 try {
    	        String existingUserId = buscarUserIdPorUsername(request.getLogin(), token);
    	        
    	        // Se já existe, atualiza as informações (caso senha ou role tenham mudado)
    	        atualizarUsuarioNoKeycloak(existingUserId, request); 
    	        return existingUserId;
    	    } catch (RuntimeException e) {
    	        // Usuário não existe, prosseguir com a criação
    	    }


        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> user = new HashMap<>();
        user.put("enabled", true);
        user.put("username", request.getLogin());
        user.put("firstName", request.getNome());

        Map<String, String> credential = new HashMap<>();
        credential.put("type", "password");
        credential.put("value", request.getSenha());
        credential.put("temporary", "false");

        user.put("credentials", List.of(credential));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(user, headers);

        try {
            ResponseEntity<Void> response = new RestTemplate().postForEntity(
                serverUrl + "/admin/realms/" + realm + "/users", entity, Void.class);

            if (response.getStatusCode().is2xxSuccessful()) {
            	String userId = buscarUserIdPorUsername(request.getLogin(), token);
                atribuirRoleAoUsuario(userId, request.getRole(), token);
                return userId;
            } else {
                throw new RuntimeException("Erro ao criar usuário no Keycloak: " + response.getStatusCode());
            }
        } catch (RestClientException ex) {
            throw new RuntimeException("Erro ao criar usuário no Keycloak", ex);
        }
    }
    
    public void atualizarUsuarioNoKeycloak(String userId, UserRequest request) {

        String token = getAdminToken();
        String url = serverUrl + "/admin/realms/" + realm + "/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put("firstName", request.getNome());
        userUpdate.put("username", request.getLogin());
        
        Map<String, String> credential = new HashMap<>();
        credential.put("type", "password");
        credential.put("value", request.getSenha());
        credential.put("temporary", "false");

        userUpdate.put("credentials", List.of(credential));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(userUpdate, headers);

        try {
            new RestTemplate().exchange(url, HttpMethod.PUT, entity, Void.class);
            atribuirRoleAoUsuario(userId, request.getRole(), token);
        } catch (RestClientException ex) {
            throw new RuntimeException("Erro ao atualizar usuário no Keycloak", ex);
        }
    }



    private String getAdminToken() {
        String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);
        form.add("username", adminUsername);
        form.add("password", adminPassword);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        try {
            ResponseEntity<Map> response = new RestTemplate().postForEntity(tokenUrl, request, Map.class);
            return (String) response.getBody().get("access_token");
        } catch (RestClientException ex) {
            throw new RuntimeException("Erro ao obter token do Keycloak", ex);
        }
    }

    private String buscarUserIdPorUsername(String username, String token) {
        String url = serverUrl + "/admin/realms/" + realm + "/users?username=" + username;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<UserRepresentation[]> response = new RestTemplate().exchange(
            url, HttpMethod.GET, entity, UserRepresentation[].class);

        if (response.getBody() != null && response.getBody().length > 0) {
            return response.getBody()[0].getId();
        }
        throw new RuntimeException("Usuário criado, mas não encontrado via busca.");
    }
    
    public void atribuirRoleAoUsuario(String userId, String roleName, String token) {
        
        String keycloakRole = "";
        
        
        if(roleName.equals("GESTOR")) {
        	keycloakRole = "admin";
        }else if (roleName.equals("FUNCIONARIO")) {
        	keycloakRole = "user-default";
        }

        String roleUrl = serverUrl + "/admin/realms/" + realm + "/roles/" + keycloakRole;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> roleRequest = new HttpEntity<>(headers);
        ResponseEntity<Map> roleResponse = new RestTemplate().exchange(roleUrl, HttpMethod.GET, roleRequest, Map.class);

        Map<String, Object> roleRepresentation = roleResponse.getBody();

        if (roleRepresentation == null) {
            throw new RuntimeException("Role '" + roleName + "' não encontrada.");
        }

        String assignUrl = serverUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";

        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<Map<String, Object>>> assignRequest = new HttpEntity<>(List.of(roleRepresentation), headers);

        new RestTemplate().postForEntity(assignUrl, assignRequest, Void.class);
    }
    




    static class UserRepresentation {
        private String id;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }
}
