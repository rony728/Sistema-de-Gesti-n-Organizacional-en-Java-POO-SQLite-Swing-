package edu.university.system.controller;

import edu.university.system.dao.PaisDao;
import edu.university.system.model.Pais;

import java.util.List;
import java.util.Optional;

public class PaisController extends ControllerSupport {

    private final PaisDao paisDao;

    public PaisController(PaisDao paisDao) {
        this.paisDao = paisDao;
    }

    public Long insertar(Pais pais) {
        AuthorizationService.require(Permission.CREATE);
        validarPais(pais, false);
        return paisDao.insertar(pais);
    }

    public boolean actualizar(Pais pais) {
        AuthorizationService.require(Permission.UPDATE);
        validarPais(pais, true);
        return paisDao.actualizar(pais);
    }

    public boolean eliminar(Long id) {
        AuthorizationService.require(Permission.DELETE);
        requireId(id, "Pais");
        return paisDao.eliminar(id);
    }

    public Optional<Pais> buscarPorId(Long id) {
        requireId(id, "Pais");
        return paisDao.buscarPorId(id);
    }

    public List<Pais> listar() {
        return paisDao.listar();
    }

    public List<Pais> buscar(String criterio) {
        if (criterio == null || criterio.isBlank()) {
            return listar();
        }
        return paisDao.buscar(criterio);
    }

    private void validarPais(Pais pais, boolean requireEntityId) {
        requireObject(pais, "Pais");
        if (requireEntityId) {
            requireId(pais.getId(), "Pais");
        }
        requireText(pais.getNombre(), "Nombre del pais", 2);
        requireText(pais.getCodigoIso(), "Codigo ISO", 2);
        if (pais.getCodigoIso().trim().length() > 3) {
            throw new ValidationException("Codigo ISO no puede tener mas de 3 caracteres.");
        }
    }
}
