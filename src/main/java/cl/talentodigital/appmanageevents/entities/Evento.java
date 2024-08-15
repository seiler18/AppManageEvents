package cl.talentodigital.appmanageevents.entities;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="id" ,nullable = false, length = 60 )
    private Long id ;

    @Column( name ="nombre",nullable = false, length = 60 )
    private String nombre ;

    @Column( name ="fecha")
    @NonNull
    private LocalDate fecha ;

    //Aca deberia ser un private Invitado invitados
    //pero lo dejaremos como un Integer para no hacer mas largo el proyecto
    //Modificamos a nullable = true , ya que un evento puede o no tener invitados
    @Column( name ="invitados")
    @Nullable
    private Integer invitados ;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH})
    @JoinColumn(name = "salon_id" , referencedColumnName = "id")
    private Salon salon;

    @Override
    public String toString() {
        return "Evento{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", fecha=" + fecha +
                ", invitados=" + invitados +
                ", salon=" + salon +
                '}';
    }
}
