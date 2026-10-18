package cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "EmpresaLogistica")
@Getter
@Setter
public class EmpresaLogistica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "empresa_id")
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "cedula_juridica", nullable = false, unique = true, length = 20)
    private String cedulaJuridica;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "activa", nullable = false)
    private Boolean activa = true;
}
