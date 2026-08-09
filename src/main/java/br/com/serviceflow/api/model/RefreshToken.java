package br.com.serviceflow.api.model;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="tb_refresh_tokens") public class RefreshToken {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="usuario_id",nullable=false) private Usuario usuario; @Column(name="token_hash",nullable=false,unique=true,length=64) private String tokenHash; @Column(name="expira_em",nullable=false) private LocalDateTime expiraEm; @Column(nullable=false) private Boolean revogado; @Column(name="criado_em",nullable=false) private LocalDateTime criadoEm;
 public RefreshToken(){} public Usuario getUsuario(){return usuario;} public void setUsuario(Usuario v){usuario=v;} public String getTokenHash(){return tokenHash;} public void setTokenHash(String v){tokenHash=v;} public LocalDateTime getExpiraEm(){return expiraEm;} public void setExpiraEm(LocalDateTime v){expiraEm=v;} public Boolean getRevogado(){return revogado;} public void setRevogado(Boolean v){revogado=v;} public void setCriadoEm(LocalDateTime v){criadoEm=v;}
}
