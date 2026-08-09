package br.com.serviceflow.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_empresas")
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 100)
    private String segmento;

    @Column(name = "nome_proprietario", nullable = false, length = 150)
    private String nomeProprietario;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(nullable = false, length = 80)
    private String plano;

    @Column(nullable = false)
    private Boolean ativo;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    public Empresa() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getSegmento() { return segmento; }
    public void setSegmento(String segmento) { this.segmento = segmento; }
    public String getNomeProprietario() { return nomeProprietario; }
    public void setNomeProprietario(String nomeProprietario) { this.nomeProprietario = nomeProprietario; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPlano() { return plano; }
    public void setPlano(String plano) { this.plano = plano; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
