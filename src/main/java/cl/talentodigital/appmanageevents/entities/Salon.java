package cl.talentodigital.appmanageevents.entities;

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
public class Salon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="id" ,nullable = false, length = 60 )
    private Long id ;

    @Column( name ="nombre",nullable = false, length = 60 )
    private String nombre ;

    @Column( name ="capacidad" )
    private Integer capacidad ;

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
