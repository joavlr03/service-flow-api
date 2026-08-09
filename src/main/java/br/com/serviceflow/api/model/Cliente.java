package br.com.serviceflow.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;
    @Column(nullable = false, length = 150)
    private String nome;
    @Column(nullable = false, length = 30)
    private String telefone;
    @Column(nullable = false, length = 30)
    private String whatsapp;
    @Column(columnDefinition = "TEXT")
    private String observacoes;
    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    public Cliente() {
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

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String v) {
        telefone = v;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String v) {
        whatsapp = v;
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
