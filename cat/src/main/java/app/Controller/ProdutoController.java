package app.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import app.Entity.Produto;
import app.Service.ProdutoService;
import jakarta.validation.Valid;

@Validated
@RestController
@CrossOrigin("*")
@RequestMapping("api/produto")
@PreAuthorize("hasRole('admin') or hasRole('user-default') or hasRole('black-cat-role')")
public class ProdutoController {
	@Autowired
	private ProdutoService produtoService  ;
	
	
	  @PostMapping("/save")
	    public ResponseEntity<String> save(@RequestBody @Valid Produto produto) {
	        try {
	            String message = this.produtoService.save(produto);
	            if (message.contains("salvo")) {
	                return new ResponseEntity<>(message, HttpStatus.OK);
	            } else {
	                return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
	            }
	        } catch (Exception e) {
	            return new ResponseEntity<>("Erro: " + e.getMessage(), HttpStatus.BAD_REQUEST);
	        }
	    }
	
	 @PutMapping("/update/{id}")
	    public ResponseEntity<String> update(@Valid @RequestBody Produto produto, @PathVariable Long id) {
	        try {
	            String msn = this.produtoService.update(produto, id);
	            return new ResponseEntity<>(msn, HttpStatus.OK);
	        } catch (Exception e) {
	            return new ResponseEntity<>("Deu erro! " + e.getMessage(), HttpStatus.BAD_REQUEST);
	        }
	    }
	 
	 @GetMapping("/findById/{id}")
	 public ResponseEntity<Produto> findById(@PathVariable Long id) {
	     try {
	    	 Produto  funcionario = this.produtoService.findById(id);
	         return new ResponseEntity<>(funcionario, HttpStatus.OK);
	     } catch (Exception e) {
	         return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
	     }
	 }

		@GetMapping("/findAll")
		public ResponseEntity<List<Produto>> findAll(@RequestParam boolean ativo) {
			try {
				List<Produto> lista = this.produtoService.findAll(ativo);
				return new ResponseEntity<>(lista, HttpStatus.OK);
			} catch (Exception e) {
				return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
			}
		}
		
		@GetMapping("/findByNome")
		public ResponseEntity<List<Produto>> findByNome(@RequestParam String nome) {
			try {
				List<Produto> lista = this.produtoService.findByNome(nome);
				return new ResponseEntity<>(lista, HttpStatus.OK);
			} catch (Exception e) {
				return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
			}
		}

		@DeleteMapping("/delete/{id}")
		public ResponseEntity<String> delete(@PathVariable Long id) {
		    try {
		        String msn = this.produtoService.delete(id);
		        return new ResponseEntity<>(msn, HttpStatus.OK);
		    } catch (Exception e) {
		        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		    }
		}
		
		@PutMapping("/disable/{id}")
	    public ResponseEntity<String> disable(@PathVariable Long id) {
	        try {
	            String msn = this.produtoService.disable(id);
	            return new ResponseEntity<>(msn, HttpStatus.OK);
	        } catch (Exception e) {
	            return new ResponseEntity<>("Deu erro! " + e.getMessage(), HttpStatus.BAD_REQUEST);
	        }
	    }
	 
		@PutMapping("/enable/{id}")
		public ResponseEntity<String> enable(@PathVariable Long id) {
			try {
				String msn = this.produtoService.enable(id);
				return new ResponseEntity<>(msn, HttpStatus.OK);
			} catch (Exception e) {
				return new ResponseEntity<>("Deu erro! " + e.getMessage(), HttpStatus.BAD_REQUEST);
			}
		}
		

}
