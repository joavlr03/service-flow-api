package br.com.serviceflow.api.model;
import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="tb_password_reset_tokens") public class PasswordResetToken {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="usuario_id",nullable=false) private Usuario usuario; @Column(name="token_hash",nullable=false,unique=true,length=64) private String tokenHash; @Column(name="expira_em",nullable=false) private LocalDateTime expiraEm; @Column(name="utilizado_em") private LocalDateTime utilizadoEm; @Column(name="criado_em",nullable=false) private LocalDateTime criadoEm;
 public PasswordResetToken(){} public Usuario getUsuario(){return usuario;} public void setUsuario(Usuario v){usuario=v;} public String getTokenHash(){return tokenHash;} public void setTokenHash(String v){tokenHash=v;} public LocalDateTime getExpiraEm(){return expiraEm;} public void setExpiraEm(LocalDateTime v){expiraEm=v;} public LocalDateTime getUtilizadoEm(){return utilizadoEm;} public void setUtilizadoEm(LocalDateTime v){utilizadoEm=v;} public void setCriadoEm(LocalDateTime v){criadoEm=v;}
}
