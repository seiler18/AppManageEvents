package cl.talentodigital.appmanageevents.entities;


import io.micrometer.common.lang.Nullable;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//Clase invitado que se relaciona con la clase evento
public class Invitado {
    //Atributos de la clase invitado > id puede ser rut
    @Id
    private Long id;

    @NotEmpty
    private String nombre;
    @NotEmpty
    private String apellido;
    @Nullable
    private String email;

}
