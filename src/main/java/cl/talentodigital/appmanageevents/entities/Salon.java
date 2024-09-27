package cl.talentodigital.appmanageevents.entities;

import java.util.List;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
//Clase Salon que se relaciona con la clase evento
public class Salon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="id" ,nullable = false, length = 60 )
    private Long id ;

    @Column( name ="nombre",nullable = false, length = 60 )
    private String nombre ;

    @Column( name ="capacidad" )
    private Integer capacidad ;

    //Aca dejamos la relacion @OneToMany con el atributo eventos ya que un salon puede tener muchos eventos, y un evento solo puede tener un salon
    //la idea es colocar el evento como nulable para que un salon pueda existir sin eventos
    @OneToMany(mappedBy = "salon", cascade = CascadeType.ALL)
    @Nullable
    private List<Evento> eventos;

    @Column( name ="descripcion",nullable = false, length = 200 )
    private String descripcion ;

    @Override
    public String toString() {
        return "Salon{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", capacidad=" + capacidad +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}
