package edu.university.system.controller;

import edu.university.system.dao.AsignacionDao;
import edu.university.system.model.Asignacion;

import java.util.List;
import java.util.Optional;

public class AsignacionController extends ControllerSupport {

    private final AsignacionDao asignacionDao;

    public AsignacionController(AsignacionDao asignacionDao) {
        this.asignacionDao = asignacionDao;
    }

    public Long insertar(Asignacion asignacion) {
        AuthorizationService.require(Permission.CREATE);
        validarAsignacion(asignacion, false);
        return asignacionDao.insertar(asignacion);
    }

    public boolean actualizar(Asignacion asignacion) {
        AuthorizationService.require(Permission.UPDATE);
        validarAsignacion(asignacion, true);
        return asignacionDao.actualizar(asignacion);
    }

    public boolean eliminar(Long id) {
        AuthorizationService.require(Permission.DELETE);
        requireId(id, "Asignacion");
        return asignacionDao.eliminar(id);
    }

    public Optional<Asignacion> buscarPorId(Long id) {
        requireId(id, "Asignacion");
        return asignacionDao.buscarPorId(id);
    }

    public List<Asignacion> listar() {
        return asignacionDao.listar();
    }

    public List<Asignacion> buscar(String criterio) {
        if (criterio == null || criterio.isBlank()) {
            return listar();
        }
        return asignacionDao.buscar(criterio);
    }

    private void validarAsignacion(Asignacion asignacion, boolean requireEntityId) {
        requireObject(asignacion, "Asignacion");
        if (requireEntityId) {
            requireId(asignacion.getId(), "Asignacion");
        }
        requireObject(asignacion.getEmpleado(), "Empleado de la asignacion");
        requireId(asignacion.getEmpleado().getId(), "Empleado de la asignacion");
        requireObject(asignacion.getProyecto(), "Proyecto de la asignacion");
        requireId(asignacion.getProyecto().getId(), "Proyecto de la asignacion");
        requireText(asignacion.getRol(), "Rol de la asignacion", 2);
        requireDate(asignacion.getFechaInicio(), "Fecha de inicio");
        validateDateRange(asignacion.getFechaInicio(), asignacion.getFechaFin(), "fecha de inicio", "fecha de fin");
        validateNonNegative(asignacion.getHorasAsignadas(), "Horas asignadas");
    }
}
