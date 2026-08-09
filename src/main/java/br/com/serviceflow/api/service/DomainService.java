package br.com.serviceflow.api.service;

import static br.com.serviceflow.api.dto.domain.DomainDtos.*;
import br.com.serviceflow.api.model.*;
import br.com.serviceflow.api.repository.*;
import jakarta.persistence.EntityNotFoundException;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DomainService {
    private final EmpresaRepository er;
    private final ClienteRepository cr;
    private final VeiculoRepository vr;
    private final TipoServicoRepository sr;
    private final OrdemServicoRepository or;
    private final DespesaRepository dr;

    public DomainService(EmpresaRepository er, ClienteRepository cr, VeiculoRepository vr, TipoServicoRepository sr,
            OrdemServicoRepository or, DespesaRepository dr) {
        this.er = er;
        this.cr = cr;
        this.vr = vr;
        this.sr = sr;
        this.or = or;
        this.dr = dr;
    }

    private Empresa emp(Long e) {
        return er.findById(e).orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada"));
    }

    private Cliente cli(Long i, Long e) {
        return cr.findByIdAndEmpresaId(i, e).orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
    }

    private Veiculo vei(Long i, Long e) {
        return vr.findByIdAndEmpresaId(i, e).orElseThrow(() -> new EntityNotFoundException("Veículo não encontrado"));
    }

    private TipoServico ser(Long i, Long e) {
        return sr.findByIdAndEmpresaId(i, e).orElseThrow(() -> new EntityNotFoundException("Serviço não encontrado"));
    }

    public ClienteOut addCliente(Long e, ClienteIn x) {
        Cliente c = new Cliente();
        c.setEmpresa(emp(e));
        c.setNome(x.name());
        c.setTelefone(x.phone());
        c.setWhatsapp(x.whatsapp() == null ? x.phone() : x.whatsapp());
        c.setObservacoes(x.notes());
        c.setCriadoEm(LocalDateTime.now());
        return co(cr.save(c));
    }

    public List<ClienteOut> clientes(Long e, String q) {
        return cr.findByEmpresaIdAndNomeContainingIgnoreCaseOrderByNome(e, q == null ? "" : q).stream().map(this::co)
                .toList();
    }

    public ClienteOut cliente(Long e, Long i) {
        return co(cli(i, e));
    }

    private ClienteOut co(Cliente c) {
        return new ClienteOut(c.getId(), c.getNome(), c.getTelefone(), c.getWhatsapp(), c.getObservacoes(),
                c.getCriadoEm());
    }

    public VeiculoOut addVeiculo(Long e, VeiculoIn x) {
        Cliente c = cli(x.clientId(), e);
        Veiculo v = new Veiculo();
        v.setEmpresa(c.getEmpresa());
        v.setCliente(c);
        v.setMarca(x.brand());
        v.setModelo(x.model());
        v.setPlaca(x.plate() == null ? null : x.plate().toUpperCase());
        v.setCor(x.color());
        v.setCriadoEm(LocalDateTime.now());
        return vo(vr.save(v));
    }

    public List<VeiculoOut> veiculos(Long e, Long c) {
        cli(c, e);
        return vr.findByEmpresaIdAndClienteIdOrderByMarca(e, c).stream().map(this::vo).toList();
    }

    private VeiculoOut vo(Veiculo v) {
        return new VeiculoOut(v.getId(), v.getCliente().getId(), v.getMarca(), v.getModelo(), v.getPlaca(), v.getCor());
    }

    public ServicoOut addServico(Long e, ServicoIn x) {
        TipoServico s = new TipoServico();
        s.setEmpresa(emp(e));
        s.setNome(x.name());
        s.setPrecoPadrao(x.price());
        s.setDuracaoMinutos(x.durationMin());
        s.setAtivo(x.active() == null || x.active());
        s.setCriadoEm(LocalDateTime.now());
        return so(sr.save(s));
    }

    public List<ServicoOut> servicos(Long e) {
        return sr.findByEmpresaIdOrderByNome(e).stream().map(this::so).toList();
    }

    private ServicoOut so(TipoServico s) {
        return new ServicoOut(s.getId(), s.getNome(), s.getPrecoPadrao(), s.getDuracaoMinutos(), s.getAtivo());
    }

    public OrdemOut addOrdem(Long e, OrdemIn x) {
        Cliente c = cli(x.clientId(), e);
        Veiculo v = vei(x.vehicleId(), e);
        if (!v.getCliente().getId().equals(c.getId()))
            throw new IllegalArgumentException("Veículo não pertence ao cliente");
        OrdemServico o = new OrdemServico();
        o.setEmpresa(emp(e));
        o.setCliente(c);
        o.setVeiculo(v);
        o.setTipoServico(ser(x.serviceId(), e));
        o.setCodigo("OS-" + (1000 + or.countByEmpresaId(e) + 1));
        o.setDescricao(x.description());
        o.setData(x.date());
        o.setHorario(x.time());
        o.setValor(x.price());
        o.setObservacoes(x.notes());
        o.setStatus("AGENDADO");
        o.setCriadoEm(LocalDateTime.now());
        o.setAtualizadoEm(LocalDateTime.now());
        return oo(or.save(o));
    }

    public OrdemOut ordem(Long e, Long i) {
        return oo(or.findByIdAndEmpresaId(i, e).orElseThrow(() -> new EntityNotFoundException("OS não encontrada")));
    }

    public List<OrdemOut> ordens(Long e, LocalDate a, LocalDate b) {
        return or.findByEmpresaIdAndDataBetweenOrderByDataAscHorarioAsc(e, a, b).stream().map(this::oo).toList();
    }

    public OrdemOut status(Long e, Long i, String s) {
        if (!Set.of("AGENDADO", "FINALIZADO", "CANCELADO").contains(s))
            throw new IllegalArgumentException("Status inválido");
        OrdemServico o = or.findByIdAndEmpresaId(i, e).orElseThrow();
        o.setStatus(s);
        o.setAtualizadoEm(LocalDateTime.now());
        return oo(o);
    }

    private OrdemOut oo(OrdemServico o) {
        return new OrdemOut(o.getId(), o.getCodigo(), o.getCliente().getId(), o.getVeiculo().getId(),
                o.getTipoServico().getId(), o.getDescricao(), o.getData(), o.getHorario(), o.getValor(),
                o.getObservacoes(), o.getStatus());
    }

    public DespesaOut addDespesa(Long e, DespesaIn x) {
        Despesa d = new Despesa();
        d.setEmpresa(emp(e));
        d.setDescricao(x.description());
        d.setCategoria(x.category());
        d.setValor(x.amount());
        d.setData(x.date());
        d.setObservacoes(x.notes());
        d.setCriadoEm(LocalDateTime.now());
        return dd(dr.save(d));
    }

    public List<DespesaOut> despesas(Long e, LocalDate a, LocalDate b) {
        return dr.findByEmpresaIdAndDataBetweenOrderByDataDesc(e, a, b).stream().map(this::dd).toList();
    }

    private DespesaOut dd(Despesa d) {
        return new DespesaOut(d.getId(), d.getDescricao(), d.getCategoria(), d.getValor(), d.getData(),
                d.getObservacoes());
    }

    public ClienteOut updateCliente(Long e, Long id, ClienteIn x) {
        Cliente c = cli(id, e);
        c.setNome(x.name());
        c.setTelefone(x.phone());
        c.setWhatsapp(x.whatsapp() == null ? x.phone() : x.whatsapp());
        c.setObservacoes(x.notes());
        return co(c);
    }

    public void deleteCliente(Long e, Long id) {
        Cliente c = cli(id, e);
        if (vr.existsByEmpresaIdAndClienteId(e, id) || or.existsByEmpresaIdAndClienteId(e, id))
            throw new IllegalArgumentException("Cliente possui histórico e não pode ser excluído");
        cr.delete(c);
    }

    public VeiculoOut veiculo(Long e, Long id) {
        return vo(vei(id, e));
    }

    public VeiculoOut updateVeiculo(Long e, Long id, VeiculoIn x) {
        Veiculo v = vei(id, e);
        Cliente c = cli(x.clientId(), e);
        v.setCliente(c);
        v.setMarca(x.brand());
        v.setModelo(x.model());
        v.setPlaca(x.plate() == null ? null : x.plate().toUpperCase());
        v.setCor(x.color());
        return vo(v);
    }

    public void deleteVeiculo(Long e, Long id) {
        Veiculo v = vei(id, e);
        if (or.existsByEmpresaIdAndVeiculoId(e, id))
            throw new IllegalArgumentException("Veículo possui histórico e não pode ser excluído");
        vr.delete(v);
    }

    public ServicoOut servico(Long e, Long id) {
        return so(ser(id, e));
    }

    public ServicoOut updateServico(Long e, Long id, ServicoIn x) {
        TipoServico s = ser(id, e);
        s.setNome(x.name());
        s.setPrecoPadrao(x.price());
        s.setDuracaoMinutos(x.durationMin());
        s.setAtivo(x.active() == null || x.active());
        return so(s);
    }

    public void deleteServico(Long e, Long id) {
        TipoServico s = ser(id, e);
        if (or.existsByEmpresaIdAndTipoServicoId(e, id))
            throw new IllegalArgumentException("Serviço possui histórico; desative-o em vez de excluir");
        sr.delete(s);
    }

    public OrdemOut updateOrdem(Long e, Long id, OrdemIn x) {
        OrdemServico o = or.findByIdAndEmpresaId(id, e)
                .orElseThrow(() -> new EntityNotFoundException("OS não encontrada"));
        Cliente c = cli(x.clientId(), e);
        Veiculo v = vei(x.vehicleId(), e);
        if (!v.getCliente().getId().equals(c.getId()))
            throw new IllegalArgumentException("Veículo não pertence ao cliente");
        o.setCliente(c);
        o.setVeiculo(v);
        o.setTipoServico(ser(x.serviceId(), e));
        o.setDescricao(x.description());
        o.setData(x.date());
        o.setHorario(x.time());
        o.setValor(x.price());
        o.setObservacoes(x.notes());
        o.setAtualizadoEm(LocalDateTime.now());
        return oo(o);
    }

    public void deleteOrdem(Long e, Long id) {
        or.delete(or.findByIdAndEmpresaId(id, e).orElseThrow(() -> new EntityNotFoundException("OS não encontrada")));
    }

    public DespesaOut despesa(Long e, Long id) {
        return dd(dr.findByIdAndEmpresaId(id, e)
                .orElseThrow(() -> new EntityNotFoundException("Despesa não encontrada")));
    }

    public DespesaOut updateDespesa(Long e, Long id, DespesaIn x) {
        Despesa d = dr.findByIdAndEmpresaId(id, e)
                .orElseThrow(() -> new EntityNotFoundException("Despesa não encontrada"));
        d.setDescricao(x.description());
        d.setCategoria(x.category());
        d.setValor(x.amount());
        d.setData(x.date());
        d.setObservacoes(x.notes());
        return dd(d);
    }

    public void deleteDespesa(Long e, Long id) {
        dr.delete(dr.findByIdAndEmpresaId(id, e)
                .orElseThrow(() -> new EntityNotFoundException("Despesa não encontrada")));
    }

    public FinanceiroOut financeiro(Long e, LocalDate a, LocalDate b) {
        var os = or.findByEmpresaIdAndDataBetweenOrderByDataAscHorarioAsc(e, a, b);
        BigDecimal r = os.stream().filter(o -> o.getStatus().equals("FINALIZADO")).map(OrdemServico::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal f = os.stream().filter(o -> !o.getStatus().equals("CANCELADO")).map(OrdemServico::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal d = dr.findByEmpresaIdAndDataBetweenOrderByDataDesc(e, a, b).stream().map(Despesa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long n = os.stream().filter(o -> o.getStatus().equals("FINALIZADO")).count(),
                c = os.stream().filter(o -> o.getStatus().equals("CANCELADO")).count();
        return new FinanceiroOut(r, f, d, r.subtract(d), n, c,
                n == 0 ? BigDecimal.ZERO : r.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP));
    }
}
