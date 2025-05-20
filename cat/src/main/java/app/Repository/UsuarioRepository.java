package app.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import app.auth.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>{
	
	public Optional<Usuario> findByLoginIgnoreCase(String login);
	
	public List<Usuario> findByAtivo(boolean ativo);
		
}
