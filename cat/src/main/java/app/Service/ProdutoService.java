package app.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.Entity.Produto;
import app.Repository.ProdutoRepository;

@Service
public class ProdutoService {

	@Autowired
	private ProdutoRepository produtoRepository;

	public String save(Produto produto) {

		if (conferirProd(produto)) {
			throw new RuntimeException("Produto com o mesmo nome e descrição já existe");
		}

		produto.setAtivo(true);
		this.produtoRepository.save(produto);
		return "Produto salvo com sucesso";
	}

	public String update(Produto produto, long id) {
		produto.setId(id);

		if (conferirProd(produto)) {
			throw new RuntimeException("Produto com o mesmo nome e descrição já existe");
		}

		this.produtoRepository.save(produto);
		return "Produto atualizado com sucesso";
	}

	public boolean conferirProd(Produto produto) {
		Optional<Produto> existingProduct = produtoRepository.findByNomeAndDescricao(produto.getNome(),
				produto.getDescricao());

		// Verifica se o produto existe e se o ID é diferente do ID do produto que está
		// sendo atualizado
		return existingProduct.isPresent() && existingProduct.get().getId() != produto.getId();
	}

	public Produto findById(long id) {
		Optional<Produto> optional = this.produtoRepository.findById(id);
		if (optional.isPresent()) {
			return optional.get();
		} else throw new RuntimeException("Produto não encontrado");
	}

	public List<Produto> findAll(boolean ativo) {
		return this.produtoRepository.findByAtivo(ativo);
	}
	
	public List<Produto> findByNome(String nome) {
		return this.produtoRepository.findByNomeContainingIgnoreCaseAndAtivoTrue(nome);
	}

	public String delete(Long id) {
		this.produtoRepository.deleteById(id);
		return "Produto deletado com sucesso!";
	}

	public String disable(Long id) {
	    Produto produtoInDB = this.findById(id);
	    if (produtoInDB == null) {
	        throw new RuntimeException("Produto não encontrado");
	    }
	    produtoInDB.setAtivo(false);
	    this.produtoRepository.save(produtoInDB);
	    return "Produto desativado com sucesso!";
	}


	public String enable(Long id) {
		Produto produtoInDB = this.findById(id);
		produtoInDB.setAtivo(true);
		this.produtoRepository.save(produtoInDB);
		return "Produto ativado com sucesso!";
	}

}
