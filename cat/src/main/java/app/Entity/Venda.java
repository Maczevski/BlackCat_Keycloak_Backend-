package app.Entity;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import app.auth.Usuario;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Venda {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	private double total;
	
	private LocalDateTime data;
	
	@Min(value = 0, message = "O valor do desconto não pode ser negativo")
	@Max(value = 100, message = "O valor do desconto não pode ser maior que 100%")
	private int desconto;

	@NotBlank(message = "A forma de pagamento é obrigatória") 
	private String formaPagamento;
	
	@ManyToOne
	@JsonIgnoreProperties("vendas")
	@NotNull(message = "O nome não pode ser nulo, vazio ou apenas espaços em branco") 
	private Usuario usuario;

	@OneToMany(mappedBy = "venda", cascade = CascadeType.ALL)
	@JsonIgnoreProperties("venda")
	@NotEmpty(message = "O nome não pode ser nulo, vazio ou apenas espaços em branco") 
	private List<ProdutoVenda> produtosVenda;
 
}

