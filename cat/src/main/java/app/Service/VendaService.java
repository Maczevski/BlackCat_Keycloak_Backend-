package app.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import app.Entity.Produto;
import app.Entity.ProdutoVenda;
import app.Entity.Venda;
import app.Repository.UsuarioRepository;
import app.Repository.VendaRepository;
import app.auth.Usuario;

@Service
public class VendaService {
	@Autowired
	private VendaRepository vendaRepository;

	@Autowired
	private ProdutoService produtoService;
	@Autowired
	private UsuarioService usuarioService;
	
	@Autowired 
	private UsuarioRepository usuarioRepository;

	public String save(Venda venda) {

		venda = registrarVenda(venda);

		validarVenda(venda);
		// toda vez que tiver um relacionamento @onetomany e que vc salva em cascata,
		// precisa fazer isso pra não ficar nula a chave estrangeira da venda no produto
		// venda
		if (venda.getProdutosVenda() != null) {
			for (int i = 0; i < venda.getProdutosVenda().size(); i++) {
				venda.getProdutosVenda().get(i).setVenda(venda);
			}
		}
		this.vendaRepository.save(venda);
		return "Venda salva com sucesso";
	}
//unitario
	public void validarVenda(Venda venda) {
		// Verificar se o usuário está ativo
		if (venda.getUsuario() == null || !venda.getUsuario().isAtivo()) {
			String nomeUsuario = (venda.getUsuario() != null) ? venda.getUsuario().getNome() : "null";
			throw new RuntimeException("Erro: " + nomeUsuario+ " foi desativado");
		}

		List<String> formasPagamentoValidas = Arrays.asList("Cartão de Débito", "Cartão de Crédito", "Dinheiro", "Pix");
		if (!formasPagamentoValidas.contains(venda.getFormaPagamento())) {
			throw new RuntimeException("Forma de pagamento inválida");
		}
	}

	public String update(Venda venda, long id) {

		venda = this.atualizarVenda(venda, id);
		this.vendaRepository.save(venda);
		return "Atualizada com sucesso";
	}

	public Venda findById(long id) {
		Optional<Venda> optional = this.vendaRepository.findById(id);
		if (optional.isPresent()) {
			return optional.get();
		} else {
			throw new RuntimeException("Venda não encontrada");
		}
	}

	public List<Venda> findAll() {
		return this.vendaRepository.findAll();
	}

	public String delete(Long id) {
		this.vendaRepository.deleteById(id);
		return "Venda deletada com sucesso";
	}

	private Venda registrarVenda(Venda venda) {

		// Verificar se o usuário está ativo no banco de dados
		Usuario usuario = usuarioRepository.findById(venda.getUsuario().getId()).get();
		

		if (usuario == null) {
			throw new RuntimeException("Usuario não encontrado");
		}
		// Verificar se a lista de produtos não está vazia
		if (venda.getProdutosVenda() == null || venda.getProdutosVenda().isEmpty()) {
			throw new RuntimeException("A lista de produtos não pode estar vazia");
		}

		if (!usuario.isAtivo()) {
			throw new RuntimeException("Erro: " + usuario.getNome() + " foi desativado");
		}

		venda.setProdutosVenda(this.verificarProdutos(venda.getProdutosVenda()));
		double valorTotal = calcularTotal(venda);
		venda.setTotal(valorTotal);
		venda.setUsuario(usuario);
		venda.setData(LocalDateTime.now());

		return venda;
	}
	

	public Venda atualizarVenda(Venda venda, long id) {

		if (!venda.getUsuario().isAtivo()) {
			throw new RuntimeException("Erro: " + venda.getUsuario().getNome() + " foi desativado");
		}

		Venda vendaInDb = this.findById(id);
		venda.setId(id);
		venda.setProdutosVenda(this.verificarProdutos(venda.getProdutosVenda()));
		venda.setData(vendaInDb.getData());
		double valorTotal = calcularTotal(venda);
		venda.setTotal(valorTotal);

		return venda;
	}

	public List<ProdutoVenda> verificarProdutos(List<ProdutoVenda> produtosVenda) {
		
		List<ProdutoVenda> listTemp = new ArrayList<>();
		
		for(ProdutoVenda produtoVenda : produtosVenda) {
			
			Produto produto = produtoService.findById(produtoVenda.getProduto().getId());
			if (produto == null) {
				throw new RuntimeException("Produto não encontrado");
			}
			// Verificar se o produto está ativo
			if (!produto.isAtivo()) {
				throw new RuntimeException("Erro: o produto " + produto.getNome() + " foi desativado.");
			} else {
				listTemp.add(produtoVenda);
			}

		}
		
		return juntarProdutosVendaIguais(produtosVenda);
		
	}
	
	public List<ProdutoVenda> juntarProdutosVendaIguais(List<ProdutoVenda> produtosVenda){
		List<ProdutoVenda> listTemp = new ArrayList<>();


		for (ProdutoVenda produtoVenda : produtosVenda) {
			// Verificar se o produto está ativo 
			Produto produto = produtoVenda.getProduto();

			boolean encontrou = false;
			for (ProdutoVenda tempProdutoVenda : listTemp) {
				if (produto.getId() == tempProdutoVenda.getProduto().getId()) {
					encontrou = true;
					tempProdutoVenda.setQuantidade(tempProdutoVenda.getQuantidade() + produtoVenda.getQuantidade());
				}
			}

			if (!encontrou) {
				produtoVenda.setProduto(produto); // Garantir que o produto atualizado está no ProdutoVenda
				listTemp.add(produtoVenda);
			}
		}
		return listTemp;
	}

	public double calcularTotal(Venda venda) {

		double valorTotal = 0;

		for (ProdutoVenda p : venda.getProdutosVenda()) {
			if (p.getProduto().getId() == 0) {
				this.produtoService.save(p.getProduto());
				valorTotal += p.getProduto().getPreco() * p.getQuantidade();
			} else {
				Produto produto = this.produtoService.findById(p.getProduto().getId());
				valorTotal += produto.getPreco() * p.getQuantidade();
			}
		}

		if (venda.getDesconto() == 0) {
			return valorTotal;
		} else {
			return valorTotal * (1 - (venda.getDesconto() / 100.0));
		}
	}


	public List<Venda> findByData(LocalDateTime startDate, LocalDateTime endDate) {
		
		if (startDate.isAfter(endDate)) {
			throw new RuntimeException("A data inicial não pode ser posterior a data final");// Garante que startDate não seja depois de endDate.
		}
		
		return this.vendaRepository.findByDataBetween(startDate, endDate);
	}

	public List<Venda> findByMonthAndYear(int mes, int ano) {
	    // Valida o mês
	    if (mes < 1 || mes > 12) {
	        throw new RuntimeException("O mês inserido não é válido.");
	    }

	    // Valida o ano
	    if (ano < 1900 || ano > LocalDateTime.now().getYear()) {
	        throw new RuntimeException("O ano inserido não é válido.");
	    }

	    // Valida se o mês e ano são maiores que a data atual
	    if (ano == LocalDateTime.now().getYear() && mes > LocalDateTime.now().getMonth().getValue()) {
	        throw new RuntimeException("O mês selecionado é posterior à data atual.");
	    }

	    // Define as datas de início e fim do mês
	    YearMonth yearMonth = YearMonth.of(ano, mes);
	    LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
	    LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);

	    return vendaRepository.findByDataBetween(startDate, endDate);
	}


	public List<Venda> findByUsuarioId(long usuarioId) {
		
		return vendaRepository.findByUsuarioId(usuarioId);
	}
	

    // Método para vendas mensais
    public double getVendasMensais(int ano, int mes) {
        LocalDate startDate = LocalDate.of(ano, mes, 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
        List<Venda> vendas = vendaRepository.findByDataBetween(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));

        return vendas.stream().mapToDouble(Venda::getTotal).sum();
    }

    // Método para vendas semanais
    public double getVendasSemanais() {
        LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        List<Venda> vendas = vendaRepository.findByDataBetween(startOfWeek.atStartOfDay(), endOfWeek.atTime(23, 59, 59));

        return vendas.stream().mapToDouble(Venda::getTotal).sum();
    }

    // Método para vendas anuais
    public double getVendasAnuais(int ano) {
        LocalDate startOfYear = LocalDate.of(ano, 1, 1);
        LocalDate endOfYear = startOfYear.with(TemporalAdjusters.lastDayOfYear());
        List<Venda> vendas = vendaRepository.findByDataBetween(startOfYear.atStartOfDay(), endOfYear.atTime(23, 59, 59));

        return vendas.stream().mapToDouble(Venda::getTotal).sum();
    }

    public List<Venda> getVendasDoUsuarioLogado() {
        Usuario usuarioLogado = usuarioService.getUsuarioLogado();  
        return vendaRepository.findByUsuarioId(usuarioLogado.getId());  
    }
    
    public long getNumeroVendasDiaPorUsuario(long usuarioId) {
        // Obtém a data atual (hoje)
        LocalDate hoje = LocalDate.now();

        // Define o início e o final do dia
        LocalDateTime inicioDoDia = hoje.atStartOfDay();
        LocalDateTime fimDoDia = hoje.atTime(23, 59, 59);

        // Busca as vendas do usuário no intervalo de tempo do dia
        List<Venda> vendasDoDia = vendaRepository.findByDataBetweenAndUsuarioId(inicioDoDia, fimDoDia, usuarioId);

        // Retorna o número de vendas (tamanho da lista)
        return vendasDoDia.size();
    }
}
