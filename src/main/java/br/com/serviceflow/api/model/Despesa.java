package br.com.serviceflow.api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "tb_despesas")
public class Despesa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;
    @Column(nullable = false, length = 180)
    private String descricao;
    @Column(nullable = false, length = 40)
    private String categoria;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;
    @Column(nullable = false)
    private LocalDate data;
    @Column(columnDefinition = "TEXT")
    private String observacoes;
    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    public Despesa() {
    }

    public Long getId() {
        return id;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa v) {
        empresa = v;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String v) {
        descricao = v;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String v) {
        categoria = v;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal v) {
        valor = v;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate v) {
        data = v;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String v) {
        observacoes = v;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime v) {
        criadoEm = v;
    }
}
