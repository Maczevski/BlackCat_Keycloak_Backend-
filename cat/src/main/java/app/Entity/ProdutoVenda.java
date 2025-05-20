package app.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoVenda {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	private int quantidade;
	
	@ManyToOne
	@JsonIgnoreProperties("produtosVenda")
	private Venda venda;
	
	@ManyToOne
	@JsonIgnoreProperties("produtosVenda")
	private Produto produto;
	
	 public ProdutoVenda(Produto produto, int quantidade) {
	        this.produto = produto;
	        this.quantidade = quantidade;
	    }
}
