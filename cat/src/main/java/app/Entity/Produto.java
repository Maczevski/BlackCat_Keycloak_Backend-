package app.Entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Produto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	 @NotEmpty(message = "O nome do produto é obrigatório")
	private String nome;
	
	private String descricao;
	
	private boolean ativo;
	
	@NotNull(message = "O preço do produto é obrigatório")
	@Positive(message = "O preço deve ser um valor positivo")
	private double preco;
	
	@OneToMany(mappedBy = "produto")
	@JsonIgnoreProperties("produto")
	private List<ProdutoVenda> produtosVenda;
	
	 public Produto(Long id, String nome, String descricao, double preco, boolean ativo) {
	        this.id = id;
	        this.nome = nome;
	        this.descricao = descricao;
	        this.preco = preco;
	        this.ativo = ativo;
	    }
	 
	 
	
}
