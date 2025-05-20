package app.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRequest {

	private long id;

	@NotBlank(message = "O nome é obrigatório")
	private String nome;

	@NotBlank(message = "Login é obrigatório")
	@Size(min = 3, max = 20, message = "Login deve ter entre 3 e 20 caracteres")
	private String login;

	@NotBlank(message = "A senha é obrigatória")
	@Size(min = 8, message = "A senha deve ter no minimo 8 caracteres")
	private String senha;

	@NotNull(message = "O tipo de usuario é obrigatorio")
	private String role;

	private boolean ativo;

}