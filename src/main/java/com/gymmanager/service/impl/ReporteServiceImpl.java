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

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
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

        // Membresías por estado
        long activas = membresiaRepository.findAll().stream()
                .filter(m -> m.getEstado() == Membresia.EstadoMembresia.ACTIVA).count();
        long porVencer = membresiaRepository.findAll().stream()
                .filter(m -> m.getEstado() == Membresia.EstadoMembresia.POR_VENCER).count();
        long expiradas = membresiaRepository.findAll().stream()
                .filter(m -> m.getEstado() == Membresia.EstadoMembresia.EXPIRADA).count();

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

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            // Título
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 16,
                    com.itextpdf.text.Font.BOLD, BaseColor.WHITE);
            PdfPTable header = new PdfPTable(1);
            header.setWidthPercentage(100);
            PdfPCell titleCell = new PdfPCell(new Phrase("OLYMPUS GYM — Reporte de Pagos", titleFont));
            titleCell.setBackgroundColor(new BaseColor(26, 26, 46));
            titleCell.setPadding(15);
            titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            titleCell.setBorder(Rectangle.NO_BORDER);
            header.addCell(titleCell);
            document.add(header);

            // Período
            com.itextpdf.text.Font periodoFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 11);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Período: " + inicio + " al " + fin, periodoFont));
            document.add(new Paragraph("Total registros: " + pagos.size(), periodoFont));
            document.add(new Paragraph(" "));

            // Tabla
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 2.5f, 2f, 1.5f, 1.5f, 1.5f, 2f});

            // Cabeceras
            String[] headers = {"ID", "Cliente", "Plan", "Monto", "Método", "Estado", "Fecha"};
            com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 10,
                    com.itextpdf.text.Font.BOLD, BaseColor.WHITE);

            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(new BaseColor(233, 69, 96));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(8);
                table.addCell(cell);
            }

            // Filas
            com.itextpdf.text.Font rowFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 9);
            boolean alternate = false;
            double total = 0;

            for (PagoResponse p : pagos) {
                BaseColor rowColor = alternate
                        ? new BaseColor(245, 245, 245)
                        : BaseColor.WHITE;

                String[] row = {
                        String.valueOf(p.getId()),
                        p.getClienteNombre() + " " + p.getClienteApellido(),
                        p.getPlanNombre(),
                        "S/. " + String.format("%.2f", p.getMonto()),
                        p.getMetodoPago(),
                        p.getEstado(),
                        p.getFechaPago().toLocalDate().toString()
                };

                for (String val : row) {
                    PdfPCell cell = new PdfPCell(new Phrase(val, rowFont));
                    cell.setBackgroundColor(rowColor);
                    cell.setPadding(6);
                    table.addCell(cell);
                }

                total += p.getMonto();
                alternate = !alternate;
            }

            document.add(table);

            // Total
            document.add(new Paragraph(" "));
            com.itextpdf.text.Font totalFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 12,
                    com.itextpdf.text.Font.BOLD);
            document.add(new Paragraph(
                    "TOTAL RECAUDADO: S/. " + String.format("%.2f", total), totalFont));

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportarPagosExcel(LocalDate inicio, LocalDate fin) {
        List<PagoResponse> pagos = reportePagosPorPeriodo(inicio, fin);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Pagos");

            // Estilo cabecera
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            // Cabeceras
            String[] headers = {
                    "ID", "Cliente", "DNI", "Plan", "Monto Original",
                    "Descuento", "Monto Final", "Método Pago",
                    "Estado", "Registrado Por", "Fecha Pago"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Estilo filas alternas
            CellStyle alternateStyle = workbook.createCellStyle();
            alternateStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
            alternateStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Datos
            double total = 0;
            for (int i = 0; i < pagos.size(); i++) {
                PagoResponse p = pagos.get(i);
                Row row = sheet.createRow(i + 1);

                if (i % 2 != 0) {
                    for (int j = 0; j < headers.length; j++) {
                        row.createCell(j).setCellStyle(alternateStyle);
                    }
                }

                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(
                        p.getClienteNombre() + " " + p.getClienteApellido());
                row.createCell(2).setCellValue(p.getClienteId());
                row.createCell(3).setCellValue(p.getPlanNombre());
                row.createCell(4).setCellValue(p.getMontoOriginal());
                row.createCell(5).setCellValue(p.getDescuento());
                row.createCell(6).setCellValue(p.getMonto());
                row.createCell(7).setCellValue(p.getMetodoPago());
                row.createCell(8).setCellValue(p.getEstado());
                row.createCell(9).setCellValue(p.getUsuarioRegistroNombre());
                row.createCell(10).setCellValue(
                        p.getFechaPago().toLocalDate().toString());

                total += p.getMonto();
            }

            // Fila total
            Row totalRow = sheet.createRow(pagos.size() + 2);
            CellStyle totalStyle = workbook.createCellStyle();
            Font totalFont = workbook.createFont();
            totalFont.setBold(true);
            totalStyle.setFont(totalFont);
            totalRow.createCell(5).setCellValue("TOTAL:");
            Cell totalCell = totalRow.createCell(6);
            totalCell.setCellValue(total);
            totalCell.setCellStyle(totalStyle);

            // Autoajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando Excel: " + e.getMessage());
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