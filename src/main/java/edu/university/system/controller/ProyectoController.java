package edu.university.system.controller;

import edu.university.system.dao.ProyectoDao;
import edu.university.system.model.Proyecto;

import java.util.List;
import java.util.Optional;

public class ProyectoController extends ControllerSupport {

    private final ProyectoDao proyectoDao;

    public ProyectoController(ProyectoDao proyectoDao) {
        this.proyectoDao = proyectoDao;
    }

    public Long insertar(Proyecto proyecto) {
        AuthorizationService.require(Permission.CREATE);
        validarProyecto(proyecto, false);
        return proyectoDao.insertar(proyecto);
    }

    public boolean actualizar(Proyecto proyecto) {
        AuthorizationService.require(Permission.UPDATE);
        validarProyecto(proyecto, true);
        return proyectoDao.actualizar(proyecto);
    }

    public boolean eliminar(Long id) {
        AuthorizationService.require(Permission.DELETE);
        requireId(id, "Proyecto");
        return proyectoDao.eliminar(id);
    }

    public Optional<Proyecto> buscarPorId(Long id) {
        requireId(id, "Proyecto");
        return proyectoDao.buscarPorId(id);
    }

    public List<Proyecto> listar() {
        return proyectoDao.listar();
    }

    public List<Proyecto> buscar(String criterio) {
        if (criterio == null || criterio.isBlank()) {
            return listar();
        }
        return proyectoDao.buscar(criterio);
    }

    private void validarProyecto(Proyecto proyecto, boolean requireEntityId) {
        requireObject(proyecto, "Proyecto");
        if (requireEntityId) {
            requireId(proyecto.getId(), "Proyecto");
        }
        requireText(proyecto.getCodigo(), "Codigo del proyecto", 3);
        requireText(proyecto.getNombre(), "Nombre del proyecto", 2);
        requireDate(proyecto.getFechaInicio(), "Fecha de inicio");
        validateDateRange(proyecto.getFechaInicio(), proyecto.getFechaFin(), "fecha de inicio", "fecha de fin");
        validateNonNegative(proyecto.getPresupuesto(), "Presupuesto");
        requireObject(proyecto.getEmpresa(), "Empresa del proyecto");
        requireId(proyecto.getEmpresa().getId(), "Empresa del proyecto");
    }
}
