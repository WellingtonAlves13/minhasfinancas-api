package com.asxdev.minhasfinancas.service;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.asxdev.minhasfinancas.exception.ErroAutenticacao;
import com.asxdev.minhasfinancas.exception.RegraNegocioException;
import com.asxdev.minhasfinancas.model.entity.Usuario;
import com.asxdev.minhasfinancas.model.repository.UsuarioRepository;
import com.asxdev.minhasfinancas.service.impl.UsuarioServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class UsuarioServiceTest {

	@SpyBean
	UsuarioServiceImpl service;

	@MockBean
	UsuarioRepository repository;

	@Test
	public void deveSalvarUmUsuario() {
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
			// cenario
			Mockito.doNothing().when(service).validarEmail(Mockito.anyString());
			Usuario usuario = Usuario.builder().nome("nome").email("email@email.com").senha("senha").id(1l).build();
			Mockito.when(repository.save(Mockito.any(Usuario.class))).thenReturn(usuario);

			// acao
			Usuario usuarioSalvo = service.salvarUsuario(new Usuario());

			// verificacao
			org.assertj.core.api.Assertions.assertThat(usuarioSalvo).isNotNull();
			org.assertj.core.api.Assertions.assertThat(usuarioSalvo.getId().equals(1l));
			org.assertj.core.api.Assertions.assertThat(usuarioSalvo.getNome().equals("nome"));
			org.assertj.core.api.Assertions.assertThat(usuarioSalvo.getEmail().equals("email@email.com"));
			org.assertj.core.api.Assertions.assertThat(usuarioSalvo.getSenha().equals("senha"));

		});
	}

	@Test
	public void naoDeveSalvaUmUsuarioComOEmailJaCadastrado() {
		Assertions.assertThrows(RegraNegocioException.class, () -> {
			// cenario
			String email = "email@email.com";
			Usuario usuario = Usuario.builder().email(email).build();
			Mockito.doThrow(RegraNegocioException.class).when(service).validarEmail(email);

			// acao
			service.salvarUsuario(usuario);
			
			//verificacao
			Mockito.verify( repository, Mockito.never()).save(usuario);

		});

	}

	@Test
	public void deveAutenticarUmUsuarioComSucesso() {
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
			// cenario
			String email = "email@email.com";
			String senha = "senha";

			Usuario usuario = Usuario.builder().email(email).senha(senha).id(1l).build();
			Mockito.when(repository.findByEmail(email)).thenReturn(Optional.of(usuario));

			// acao
			Usuario result = service.autenticar(email, senha);

			// verificacao
			org.assertj.core.api.Assertions.assertThat(result).isNotNull();
		});
	}

	@Test
	public void deveLancarErroQuandoNaoEncontrarUsuarioCadastradoComOEmailInformado() {

		// cenario
		Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());

		// acao
		Throwable exception = org.assertj.core.api.Assertions
				.catchThrowable(() -> service.autenticar("email@email.com", "senha"));

		// verificacao
		org.assertj.core.api.Assertions.assertThat(exception).isInstanceOf(ErroAutenticacao.class)
				.hasMessage("Usuario não encontrado para o email informado");

	}

	@Test
	public void deveLancarErroQuandoSenhaNaoBater() {
		// cenario
		String senha = "senha";
		Usuario usuario = Usuario.builder().email("email@email.com").senha(senha).build();
		Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(usuario));

		// acao
		Throwable exception = org.assertj.core.api.Assertions
				.catchThrowable(() -> service.autenticar("email@email.com", "123"));
		org.assertj.core.api.Assertions.assertThat(exception).isInstanceOf(ErroAutenticacao.class)
				.hasMessage("Senha invalida");

	}

	@Test
	public void deveValidarEmail() {
		Assertions.assertDoesNotThrow(() -> {
			// cenario
			Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(false);

			// acao
			service.validarEmail("email@email.com");
		});

	}

	@Test
	public void deveLancarErrorAoValidarEmailQuandoExistirEmailCadastrado() {
		Assertions.assertThrows(RegraNegocioException.class, () -> {
			// cenario
			Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(true);

			// acao
			service.validarEmail("email@email.com");
		});

	}

}
