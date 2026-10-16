package br.com.serviceflow.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_categorias", uniqueConstraints =
        @UniqueConstraint(name = "uk_categoria_empresa_tipo_nome", columnNames = {"empresa_id", "tipo", "nome"}))
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;
    // DESPESA ou ENTRADA
    @Column(nullable = false, length = 10)
    private String tipo;
    @Column(nullable = false, length = 40)
    private String nome;
    @Column(nullable = false)
    private Boolean ativo = true;

    public Categoria() {}

    public Categoria(Empresa empresa, String tipo, String nome) {
        this.empresa = empresa;
        this.tipo = tipo;
        this.nome = nome;
    }

    public Long getId() { return id; }
    public Empresa getEmpresa() { return empresa; }
    public String getTipo() { return tipo; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
