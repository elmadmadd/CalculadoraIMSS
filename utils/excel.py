import datetime
import subprocess
import os
import platform
from openpyxl.styles import Font, PatternFill, Alignment, Border, Side
from openpyxl.utils import get_column_letter
from core.config import CONSTANTES

try:
    import openpyxl
    HAVE_OPENPYXL = True
except ImportError:
    HAVE_OPENPYXL = False

def exportar_excel_diario(gestor_operaciones):
    if not HAVE_OPENPYXL: return False, "Librería openpyxl no instalada"
    try:
        ops_dia = gestor_operaciones.filtrar_por_dia()
        if not ops_dia: return False, "No hay operaciones registradas hoy"

        consultas = [op for op in ops_dia if op.get("estatus") == "CONSULTA"]
        procesos = [op for op in ops_dia if op.get("estatus") in ["EN PROCESO", "FINALIZADO", "PENDIENTE"]]

        wb = openpyxl.Workbook()
        # openpyxl crea una hoja por defecto llamada "Sheet"
        hoja_defecto = wb.active

        hojas_creadas = 0

        # 1. Manejo de Consultas
        if consultas:
            ws_consultas = hoja_defecto
            ws_consultas.title = "Consultas"
            _crear_hoja_excel(ws_consultas, consultas, "CONSULTAS DEL DÍA")
            hojas_creadas += 1

        # 2. Manejo de Procesos
        if procesos:
            if hojas_creadas > 0:
                # Si ya usamos la hoja por defecto para consultas, creamos una nueva
                ws_procesos = wb.create_sheet("Procesos")
            else:
                # Si NO hubo consultas, usamos la hoja por defecto que aún está limpia
                ws_procesos = hoja_defecto
                ws_procesos.title = "Procesos"

            _crear_hoja_excel(ws_procesos, procesos, "PROCESOS DEL DÍA")
            hojas_creadas += 1

        # Caso borde: Si por alguna razón extraña no hubo ni lo uno ni lo otro pero ops_dia no estaba vacío
        # (ej. estatus desconocidos), evitamos guardar un excel vacío o con hoja default sucia.
        if hojas_creadas == 0:
            return False, "No hay operaciones con estatus válido para exportar."

        fecha_actual = datetime.datetime.now().strftime("%Y-%m-%d")
        nombre_archivo = f"Reporte_Diario_{fecha_actual}.xlsx"
        ruta_reporte = CONSTANTES["RUTAS"]["REPORTES"] / nombre_archivo

        contador = 1
        while ruta_reporte.exists():
            nombre_alterno = f"Reporte_Diario_{fecha_actual}_{contador}.xlsx"
            ruta_reporte = CONSTANTES["RUTAS"]["REPORTES"] / nombre_alterno
            contador += 1

        wb.save(ruta_reporte)

        try:
            if platform.system() == "Windows": os.startfile(CONSTANTES["RUTAS"]["REPORTES"])
            elif platform.system() == "Darwin": subprocess.run(["open", CONSTANTES["RUTAS"]["REPORTES"]])
            else: subprocess.run(["xdg-open", CONSTANTES["RUTAS"]["REPORTES"]])
        except: pass

        return True, str(ruta_reporte)

    except Exception as e: return False, str(e)

def _crear_hoja_excel(worksheet, datos, titulo):
    header_fill = PatternFill(start_color="1E293B", end_color="1E293B", fill_type="solid")
    header_font = Font(color="FFFFFF", bold=True, size=11)
    border = Border(left=Side(style='thin'), right=Side(style='thin'), top=Side(style='thin'), bottom=Side(style='thin'))
    center_alignment = Alignment(horizontal="center", vertical="center")
    left_alignment = Alignment(horizontal="left", vertical="center")
    worksheet.merge_cells('A1:H1')
    title_cell = worksheet['A1']
    title_cell.value = f"{titulo} - {datetime.datetime.now().strftime('%d/%m/%Y')}"
    title_cell.font = Font(size=14, bold=True, color="1E293B")
    title_cell.alignment = Alignment(horizontal="center", vertical="center")
    worksheet.row_dimensions[1].height = 30
    worksheet.merge_cells('A2:H2')
    stats_cell = worksheet['A2']
    stats_cell.value = f"Total registros: {len(datos)}"
    stats_cell.font = Font(size=10, italic=True, color="64748B")
    stats_cell.alignment = Alignment(horizontal="center", vertical="center")
    worksheet.row_dimensions[2].height = 20
    headers = ["ID", "Fecha", "Hora", "Cliente", "Producto", "Monto", "Plazo", "Estatus", "Tipo"]
    for col_num, header in enumerate(headers, 1):
        cell = worksheet.cell(row=3, column=col_num)
        cell.value = header
        cell.fill = header_fill
        cell.font = header_font
        cell.border = border
        cell.alignment = center_alignment
        worksheet.column_dimensions[get_column_letter(col_num)].width = max(len(header) + 4, 12)
    for row_num, op in enumerate(datos, 4):
        fecha_completa = op.get("fecha", "")
        fecha = hora = ""
        if " " in fecha_completa: fecha, hora = fecha_completa.split(" ", 1)
        else: fecha = fecha_completa
        monto = op.get("monto", 0)
        monto_formateado = f"${monto:,.2f}" if isinstance(monto, (int, float)) else str(monto)
        estatus = op.get("estatus", "")
        estatus_color = {"CONSULTA": "10B981", "EN PROCESO": "3B82F6", "FINALIZADO": "10B981", "PENDIENTE": "F59E0B"}.get(estatus, "94A3B8")
        row_data = [op.get("id", "")[:8] + "...", fecha, hora, op.get("cliente", "N/A"), op.get("producto", "N/A"), monto_formateado, op.get("plazo", "N/A"), estatus, op.get("tipo", "N/A")]
        for col_num, value in enumerate(row_data, 1):
            cell = worksheet.cell(row=row_num, column=col_num)
            cell.value = value
            cell.border = border
            if col_num in [4, 5, 8, 9]: cell.alignment = left_alignment
            else: cell.alignment = center_alignment
            if col_num == 8: cell.font = Font(bold=True, color=estatus_color)
    worksheet.freeze_panes = "A4"
    worksheet.auto_filter.ref = f"A3:{get_column_letter(len(headers))}3"
