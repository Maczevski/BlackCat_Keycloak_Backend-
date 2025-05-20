package app.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import app.Entity.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
	
	public Optional<Produto> findByNomeAndDescricao(String nome, String descricao);
	
	public List<Produto> findByAtivo(boolean ativo);
	
	public List<Produto> findByNomeContainingIgnoreCaseAndAtivoTrue(String nome);
	
	
}
