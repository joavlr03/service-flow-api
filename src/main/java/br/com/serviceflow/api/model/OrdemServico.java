package br.com.serviceflow.api.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "tb_ordens_servicos")
public class OrdemServico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 30)
    private String codigo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_servico_id", nullable = false)
    private TipoServico tipoServico;
    private String descricao;
    @Column(nullable = false)
    private LocalDate data;
    @Column(nullable = false)
    private LocalTime horario;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;
    @Column(columnDefinition = "TEXT")
    private String observacoes;
    @Column(nullable = false, length = 20)
    private String status;
    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    public OrdemServico() {
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String v) {
        codigo = v;
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

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(Veiculo v) {
        veiculo = v;
    }

    public TipoServico getTipoServico() {
        return tipoServico;
    }

    public void setTipoServico(TipoServico v) {
        tipoServico = v;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String v) {
        descricao = v;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate v) {
        data = v;
    }

    public LocalTime getHorario() {
        return horario;
    }

    public void setHorario(LocalTime v) {
        horario = v;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal v) {
        valor = v;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String v) {
        observacoes = v;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        status = v;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime v) {
        criadoEm = v;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime v) {
        atualizadoEm = v;
    }
}
