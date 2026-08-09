package br.com.serviceflow.api.dto.domain;
import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.time.*;
public final class DomainDtos { private DomainDtos(){}
 public record ClienteIn(@NotBlank String name,@NotBlank String phone,String whatsapp,String notes){} public record ClienteOut(Long id,String name,String phone,String whatsapp,String notes,LocalDateTime createdAt){}
 public record VeiculoIn(@NotNull Long clientId,@NotBlank String brand,@NotBlank String model,String plate,String color){} public record VeiculoOut(Long id,Long clientId,String brand,String model,String plate,String color){}
 public record ServicoIn(@NotBlank String name,@NotNull @PositiveOrZero BigDecimal price,@NotNull @Positive Integer durationMin,Boolean active){} public record ServicoOut(Long id,String name,BigDecimal price,Integer durationMin,Boolean active){}
 public record OrdemIn(@NotNull Long clientId,@NotNull Long vehicleId,@NotNull Long serviceId,String description,@NotNull LocalDate date,@NotNull LocalTime time,@NotNull @PositiveOrZero BigDecimal price,String notes){} public record OrdemOut(Long id,String code,Long clientId,Long vehicleId,Long serviceId,String description,LocalDate date,LocalTime time,BigDecimal price,String notes,String status){} public record StatusIn(@NotBlank String status){}
 public record DespesaIn(@NotBlank String description,@NotBlank String category,@NotNull @Positive BigDecimal amount,@NotNull LocalDate date,String notes){} public record DespesaOut(Long id,String description,String category,BigDecimal amount,LocalDate date,String notes){}
 public record FinanceiroOut(BigDecimal realized,BigDecimal forecast,BigDecimal expenses,BigDecimal profit,long finished,long canceled,BigDecimal averageTicket){}
 public record EmpresaIn(@NotBlank String name,@NotBlank String segment,@NotBlank String ownerName,@NotBlank @Email String email,@NotBlank String plan){}
}
