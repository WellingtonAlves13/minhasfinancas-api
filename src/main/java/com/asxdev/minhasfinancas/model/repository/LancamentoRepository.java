package com.asxdev.minhasfinancas.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asxdev.minhasfinancas.model.entity.Lancamento;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long>{
	

}
