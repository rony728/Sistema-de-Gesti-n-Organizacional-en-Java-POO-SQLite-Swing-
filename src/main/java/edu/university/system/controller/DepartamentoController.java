package edu.university.system.controller;

import edu.university.system.dao.DepartamentoDao;
import edu.university.system.model.Departamento;

import java.util.List;
import java.util.Optional;

public class DepartamentoController extends ControllerSupport {

    private final DepartamentoDao departamentoDao;

    public DepartamentoController(DepartamentoDao departamentoDao) {
        this.departamentoDao = departamentoDao;
    }

    public Long insertar(Departamento departamento) {
        AuthorizationService.require(Permission.CREATE);
        validarDepartamento(departamento, false);
        return departamentoDao.insertar(departamento);
    }

    public boolean actualizar(Departamento departamento) {
        AuthorizationService.require(Permission.UPDATE);
        validarDepartamento(departamento, true);
        return departamentoDao.actualizar(departamento);
    }

    public boolean eliminar(Long id) {
        AuthorizationService.require(Permission.DELETE);
        requireId(id, "Departamento");
        return departamentoDao.eliminar(id);
    }

    public Optional<Departamento> buscarPorId(Long id) {
        requireId(id, "Departamento");
        return departamentoDao.buscarPorId(id);
    }

    public List<Departamento> listar() {
        return departamentoDao.listar();
    }

    public List<Departamento> buscar(String criterio) {
        if (criterio == null || criterio.isBlank()) {
            return listar();
        }
        return departamentoDao.buscar(criterio);
    }

    private void validarDepartamento(Departamento departamento, boolean requireEntityId) {
        requireObject(departamento, "Departamento");
        if (requireEntityId) {
            requireId(departamento.getId(), "Departamento");
        }
        requireText(departamento.getNombre(), "Nombre del departamento", 2);
        requireText(departamento.getCodigo(), "Codigo del departamento", 2);
        requireObject(departamento.getPais(), "Pais del departamento");
        requireId(departamento.getPais().getId(), "Pais del departamento");
    }
}
