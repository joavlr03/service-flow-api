package br.com.serviceflow.api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_tipos_servicos")
public class TipoServico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;
    @Column(nullable = false, length = 120)
    private String nome;
    @Column(name = "preco_padrao", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoPadrao;
    @Column(name = "duracao_minutos", nullable = false)
    private Integer duracaoMinutos;
    @Column(nullable = false)
    private Boolean ativo;
    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    public TipoServico() {
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

    public String getNome() {
        return nome;
    }

    public void setNome(String v) {
        nome = v;
    }

    public BigDecimal getPrecoPadrao() {
        return precoPadrao;
    }

    public void setPrecoPadrao(BigDecimal v) {
        precoPadrao = v;
    }

    public Integer getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(Integer v) {
        duracaoMinutos = v;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean v) {
        ativo = v;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime v) {
        criadoEm = v;
    }
}
