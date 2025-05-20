package app.Controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.reponse.UserUpdateResponse;
import app.Service.UsuarioService;
import app.auth.UserRequest;
import app.auth.Usuario;

@Validated
@RestController
@RequestMapping("api/usuario")
@CrossOrigin("*")
@PreAuthorize("hasRole('admin') or hasRole('black-cat-role')")
public class UsuarioController {

	@Autowired
	private UsuarioService usuarioService;

	@PostMapping("/save")
	    public ResponseEntity<String> save(@RequestBody @Valid UserRequest usuario) {
	        try {
	            String message = this.usuarioService.save(usuario);
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
	public ResponseEntity<UserUpdateResponse> update(@RequestBody UserRequest usuario, @PathVariable Long id) {
		try {
			UserUpdateResponse response = this.usuarioService.update(usuario, id);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
		    return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/findById/{id}")
	public ResponseEntity<Usuario> findById(@PathVariable long id) {
		try {
			Usuario funcionario = this.usuarioService.findById(id);
			return new ResponseEntity<>(funcionario, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}@GetMapping("/findByLogin/{login}")
	public ResponseEntity<Usuario> findByLogin(@PathVariable String login) {
		try {
			Usuario funcionario = this.usuarioService.findByLogin(login);
			return new ResponseEntity<>(funcionario, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/findAll")
	public ResponseEntity<List<Usuario>> findAll(@RequestParam boolean ativo) {
		try {
			List<Usuario> lista = this.usuarioService.findAll(ativo);
			return new ResponseEntity<>(lista, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<String> delete(@PathVariable Long id) {
		try {
			String msn = this.usuarioService.delete(id);
			return new ResponseEntity<>(msn, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>("Erro ao deletar Usuario!", HttpStatus.BAD_REQUEST);
		}
	}
	
	@PutMapping("/disable/{id}")
    public ResponseEntity<String> disable(@PathVariable Long id) {
        try {
            String msn = this.usuarioService.disable(id);
            return new ResponseEntity<>(msn, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Deu erro! " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
 
	@PutMapping("/enable/{id}")
	public ResponseEntity<String> enable(@PathVariable Long id) {
		try {
			String msn = this.usuarioService.enable(id);
			return new ResponseEntity<>(msn, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>("Deu erro! " + e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	

}
