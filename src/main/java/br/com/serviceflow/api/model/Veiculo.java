package br.com.serviceflow.api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_veiculos")
public class Veiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
    @Column(nullable = false, length = 80)
    private String marca;
    @Column(nullable = false, length = 80)
    private String modelo;
    @Column(length = 10)
    private String placa;
    @Column(length = 40)
    private String cor;
    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    public Veiculo() {
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

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente v) {
        cliente = v;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String v) {
        marca = v;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String v) {
        modelo = v;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String v) {
        placa = v;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String v) {
        cor = v;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime v) {
        criadoEm = v;
    }
}
