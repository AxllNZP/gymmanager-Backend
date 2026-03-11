package com.gymmanager.service.impl;

import com.gymmanager.dto.DashboardResponse;
import com.gymmanager.dto.Membresia.MembresiaResponse;
import com.gymmanager.dto.Pago.PagoResponse;
import com.gymmanager.entity.Membresia;
import com.gymmanager.entity.Pago;
import com.gymmanager.repository.*;
import com.gymmanager.service.MembresiaService;
import com.gymmanager.service.PagoService;
import com.gymmanager.service.ReporteService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import com.gymmanager.exception.InvalidOperationException;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {

    private final ClienteRepository clienteRepository;
    private final MembresiaRepository membresiaRepository;
    private final PagoRepository pagoRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final PagoService pagoService;
    private final MembresiaService membresiaService;

    @Override
    public DashboardResponse obtenerDashboard() {
        DashboardResponse dashboard = new DashboardResponse();

        // Total clientes activos
        dashboard.setTotalClientes((long) clienteRepository.findByActivoTrue().size());

        List<Membresia> membresias = membresiaRepository.findAll();

        long activas = membresias.stream()
                .filter(m -> m.getEstado() == Membresia.EstadoMembresia.ACTIVA)
                .count();

        long porVencer = membresias.stream()
                .filter(m -> m.getEstado() == Membresia.EstadoMembresia.POR_VENCER)
                .count();

        long expiradas = membresias.stream()
                .filter(m -> m.getEstado() == Membresia.EstadoMembresia.EXPIRADA)
                .count();

        dashboard.setMembresiasActivas(activas);
        dashboard.setMembresiasPorVencer(porVencer);
        dashboard.setMembresiasExpiradas(expiradas);

        // Asistencias de hoy
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia = inicioDia.plusDays(1);
        dashboard.setAsistenciasHoy(
                (long) asistenciaRepository.findByFechaEntradaBetween(inicioDia, finDia).size());

        // Recaudación mes actual
        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        Double recaudacionMes = pagoRepository.sumMontoByPeriodo(inicioMes, LocalDateTime.now());
        dashboard.setRecaudacionMesActual(recaudacionMes != null ? recaudacionMes : 0.0);

        // Recaudación total
        Double recaudacionTotal = pagoRepository.sumMontoByPeriodo(
                LocalDateTime.of(2000, 1, 1, 0, 0), LocalDateTime.now());
        dashboard.setRecaudacionTotal(recaudacionTotal != null ? recaudacionTotal : 0.0);

        return dashboard;
    }

    @Override
    public List<PagoResponse> reportePagosPorPeriodo(LocalDate inicio, LocalDate fin) {

        if (inicio == null || fin == null) {
            throw new InvalidOperationException("Las fechas inicio y fin son obligatorias");
        }

        if (fin.isBefore(inicio)) {
            throw new InvalidOperationException("La fecha fin no puede ser menor que la fecha inicio");
        }

        LocalDateTime desde = inicio.atStartOfDay();
        LocalDateTime hasta = fin.plusDays(1).atStartOfDay();

        return pagoRepository.findByFechaPagoBetween(desde, hasta)
                .stream()
                .map(this::pagoToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MembresiaResponse> reporteMembresiasExpiradas() {
        return membresiaRepository.findExpiradas(LocalDate.now())
                .stream()
                .map(this::membresiaToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MembresiaResponse> reporteMembresiasPorVencer() {
        LocalDate hoy = LocalDate.now();
        return membresiaRepository.findPorVencer(hoy, hoy.plusDays(5))
                .stream()
                .map(this::membresiaToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public byte[] exportarPagosPdf(LocalDate inicio, LocalDate fin) {

        List<PagoResponse> pagos = reportePagosPorPeriodo(inicio, fin);

        if (pagos.isEmpty()) {
            throw new InvalidOperationException(
                    "No existen pagos registrados en el periodo seleccionado"
            );
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            // contenido del PDF igual que tu implementación...

            document.close();

            return baos.toByteArray();

        } catch (Exception e) {

            log.error("Error generando reporte PDF de pagos", e);

            throw new InvalidOperationException(
                    "Error generando el reporte PDF"
            );
        }
    }

    @Override
    public byte[] exportarPagosExcel(LocalDate inicio, LocalDate fin) {

        List<PagoResponse> pagos = reportePagosPorPeriodo(inicio, fin);

        if (pagos.isEmpty()) {
            throw new InvalidOperationException(
                    "No existen pagos registrados en el periodo seleccionado"
            );
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Pagos");

            // todo tu código actual del Excel...

            workbook.write(baos);

            return baos.toByteArray();

        } catch (Exception e) {

            log.error("Error generando reporte Excel de pagos", e);

            throw new InvalidOperationException(
                    "Error generando el archivo Excel"
            );
        }
    }

    private PagoResponse pagoToResponse(Pago p) {
        PagoResponse response = new PagoResponse();
        response.setId(p.getId());
        response.setMembresiaId(p.getMembresia().getId());
        response.setClienteId(p.getCliente().getId());
        response.setClienteNombre(p.getCliente().getNombre());
        response.setClienteApellido(p.getCliente().getApellido());
        response.setPlanNombre(p.getMembresia().getPlan().getNombre());
        response.setMontoOriginal(p.getMontoOriginal());
        response.setDescuento(p.getDescuento());
        response.setMonto(p.getMonto());
        response.setMotivoDescuento(p.getMotivoDescuento());
        response.setMetodoPago(p.getMetodoPago().name());
        response.setEstado(p.getEstado().name());
        response.setCorreoEnviado(p.getCorreoEnviado());
        response.setUsuarioRegistroNombre(p.getUsuarioRegistro().getNombre());
        response.setFechaPago(p.getFechaPago());
        return response;
    }

    private MembresiaResponse membresiaToResponse(Membresia m) {
        MembresiaResponse response = new MembresiaResponse();
        response.setId(m.getId());
        response.setClienteId(m.getCliente().getId());
        response.setClienteNombre(m.getCliente().getNombre());
        response.setClienteApellido(m.getCliente().getApellido());
        response.setClienteDni(m.getCliente().getDni());
        response.setPlanId(m.getPlan().getId());
        response.setPlanNombre(m.getPlan().getNombre());
        response.setPlanNumeroPersonas(m.getPlan().getNumeroPersonas());
        response.setPlanPrecio(m.getPlan().getPrecio());
        response.setFechaInicio(m.getFechaInicio());
        response.setFechaFin(m.getFechaFin());
        response.setEstado(m.getEstado().name());
        response.setCreatedAt(m.getCreatedAt());
        return response;
    }
}