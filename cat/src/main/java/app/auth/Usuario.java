package app.auth;

import java.util.List;

import app.Entity.Venda;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	private String keycloakID;

	@NotBlank(message = "O nome é obrigatório")
	private String nome;

	@NotBlank(message = "Login é obrigatório")
	@Size(min = 3, max = 20, message = "Login deve ter entre 3 e 20 caracteres")
	private String login;

	@NotNull(message = "O tipo de usuario é obrigatorio")
	private String role;

	private boolean ativo;
	
	
    public Usuario(long id, String nome, String login, String senha, String role, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.login = login;
        this.role = role;
        this.ativo = ativo;
    }


	@OneToMany(mappedBy = "usuario")
	@JsonIgnoreProperties("usuario")
	private List<Venda> vendas;


}