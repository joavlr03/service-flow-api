package br.com.serviceflow.api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_produtos")
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;
    @Column(nullable = false, length = 120)
    private String nome;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;
    @Column(nullable = false)
    private Integer estoque = 0;
    @Column(nullable = false)
    private Boolean ativo = true;
    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    public Produto() {}

    public Long getId() { return id; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa v) { empresa = v; }
    public String getNome() { return nome; }
    public void setNome(String v) { nome = v; }
    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal v) { preco = v; }
    public Integer getEstoque() { return estoque; }
    public void setEstoque(Integer v) { estoque = v; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean v) { ativo = v; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime v) { criadoEm = v; }
}
