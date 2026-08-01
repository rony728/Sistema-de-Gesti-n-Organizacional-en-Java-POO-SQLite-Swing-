package edu.university.system.controller;

import edu.university.system.dao.PersonaDao;
import edu.university.system.model.Persona;

import java.util.List;
import java.util.Optional;

public class PersonaController extends ControllerSupport {

    private final PersonaDao personaDao;

    public PersonaController(PersonaDao personaDao) {
        this.personaDao = personaDao;
    }

    public Long insertar(Persona persona) {
        validarPersona(persona, false);
        return personaDao.insertar(persona);
    }

    public boolean actualizar(Persona persona) {
        validarPersona(persona, true);
        return personaDao.actualizar(persona);
    }

    public boolean eliminar(Long id) {
        requireId(id, "Persona");
        return personaDao.eliminar(id);
    }

    public Optional<Persona> buscarPorId(Long id) {
        requireId(id, "Persona");
        return personaDao.buscarPorId(id);
    }

    public List<Persona> listar() {
        return personaDao.listar();
    }

    protected void validarPersona(Persona persona, boolean requireEntityId) {
        requireObject(persona, "Persona");
        if (requireEntityId) {
            requireId(persona.getId(), "Persona");
        }
        requireText(persona.getIdentidad(), "Identidad", 5);
        requireText(persona.getNombres(), "Nombres", 2);
        requireText(persona.getApellidos(), "Apellidos", 2);
        validateOptionalEmail(persona.getCorreoElectronico(), "Correo electronico");
    }
}
