import datetime
import time
from core.config import CONSTANTES
from utils.formatting import to_money

try:
    from fpdf import FPDF
    HAVE_FPDF = True
except ImportError:
    HAVE_FPDF = False

class GeneradorPDF:
    def generar(self, form_data, credito_data, perfil, tipo_tramite):
        if not HAVE_FPDF: return False, "Librería fpdf no instalada."
        try:
            pdf = FPDF()
            pdf.add_page()

            # --- ENCABEZADO ---
            pdf.set_font("Helvetica", "B", 14)
            pdf.set_text_color(30, 41, 59) # Color Slate oscuro
            pdf.cell(0, 10, "RESUMEN DE OPERACIÓN FINANCIERA", 0, 1, "C")

            pdf.set_font("Helvetica", "", 8)
            pdf.set_text_color(100, 100, 100)
            pdf.cell(0, 5, f"FOLIO: {int(time.time())} | FECHA: {datetime.datetime.now().strftime('%d/%m/%Y %H:%M')}", 0, 1, "C")
            pdf.ln(10)

            # --- SECCIÓN 1: PERFIL DEL CLIENTE (Completo) ---
            datos_personales = [
                ("Nombre Completo", str(form_data.get('nombre') or '').upper()),
                ("RFC", str(form_data.get('rfc') or '').upper()),
                ("CURP", str(form_data.get('curp') or '').upper()),
                ("NSS", str(form_data.get('nss') or '')),
                ("Ocupación", str(form_data.get('ocupacion') or '').upper()),
                ("Teléfono", str(form_data.get('telefono') or '')),
                ("Domicilio", str(form_data.get('domicilio') or '').upper()),
            ]
            self._sec(pdf, "DATOS PERSONALES", datos_personales)

            # --- SECCIÓN 2: DATOS BANCARIOS ---
            datos_bancarios = [
                ("Banco", str(form_data.get('banco') or '').upper()),
                ("CLABE Interbancaria", str(form_data.get('clabe') or '')),
                ("Convenio", str(form_data.get('convenio', 'IMSS') or 'IMSS').upper())
            ]
            self._sec(pdf, "INFORMACIÓN BANCARIA", datos_bancarios)

            # --- SECCIÓN 3: DETALLE DEL CRÉDITO ---
            datos_credito = [
                ("Producto Financiero", credito_data.get('producto', '')),
                ("Monto Autorizado", to_money(credito_data.get('monto', 0))),
                ("Plazo Seleccionado", f"{credito_data.get('plazo', 0)} Meses"),
                ("Descuento Periódico", to_money(credito_data.get('pago_visual', 0))),
                ("CAT Informativo", f"{credito_data.get('cat', '0.0')}%"),
                ("Tipo de Tasa", credito_data.get('tipo_tasa', 'TRADICIONAL'))
            ]
            self._sec(pdf, "ESTRUCTURA DE CRÉDITO APROBADA", datos_credito)

            # --- RESALTADO DE REMANENTE (Si aplica) ---
            if tipo_tramite == "CNCA":
                rem = credito_data.get('remanente', 0)
                pdf.ln(5)
                pdf.set_fill_color(220, 252, 231) # Verde claro suave
                pdf.set_draw_color(22, 163, 74)   # Borde verde
                pdf.set_font("Helvetica", "B", 11)
                pdf.cell(0, 12, f"  REMANENTE NETO ESTIMADO: {to_money(rem)}", 1, 1, "C", True)
                pdf.ln(5)

            # --- PIE DE PÁGINA / DISCLAIMER ---
            pdf.ln(15)
            pdf.set_draw_color(200, 200, 200)
            pdf.line(10, pdf.get_y(), 200, pdf.get_y()) # Línea separadora
            pdf.ln(5)

            pdf.set_font("Helvetica", "B", 8)
            pdf.cell(0, 4, f"ASESOR: {str(perfil.get('nombre') or '').upper()}", 0, 1, "L")
            pdf.cell(0, 4, f"SUPERVISOR: {str(perfil.get('supervisor') or '').upper()}", 0, 1, "L")

            pdf.ln(5)
            pdf.set_font("Helvetica", "", 6)
            pdf.set_text_color(128, 128, 128)
            pdf.multi_cell(0, 3, "AVISO LEGAL: Este documento es una proyección financiera preliminar basada en los datos proporcionados. La autorización final está sujeta a validación documental, capacidad de pago y políticas vigentes de la entidad financiera seleccionada. Las tasas y factores pueden variar sin previo aviso.")

            # Guardar
            fname = f"Cotizacion_{form_data.get('rfc', 'CLIENTE')}_{int(time.time())}.pdf"
            path = CONSTANTES["RUTAS"]["DOCS"] / fname
            pdf.output(str(path))
            return True, str(path)

        except Exception as e: return False, str(e)

    def _sec(self, pdf, titulo, datos):
        # Título de Sección con fondo
        pdf.set_font("Helvetica", "B", 10)
        pdf.set_fill_color(241, 245, 249) # Gris azulado muy claro
        pdf.set_text_color(15, 23, 42)
        pdf.cell(0, 8, f"  {titulo}", 0, 1, "L", True)
        pdf.ln(2)

        # Contenido
        pdf.set_font("Helvetica", "", 9)
        pdf.set_text_color(51, 65, 85)

        for etiqueta, valor in datos:
            # Usamos celdas invisibles para alinear: Etiqueta (45mm) | Valor (Resto)
            pdf.set_font("Helvetica", "B", 9)
            pdf.cell(50, 6, f"{etiqueta}:", 0, 0)

            pdf.set_font("Helvetica", "", 9)
            # Multi_cell para el valor por si es muy largo (ej. Domicilio)
            # Guardamos la posición X, Y actual
            x_val = pdf.get_x()
            y_val = pdf.get_y()

            pdf.multi_cell(0, 6, str(valor))

            # Si multi_cell saltó de línea, necesitamos ajustar el cursor para la siguiente fila visualmente si fuera tabla,
            # pero como es lista simple, solo aseguramos espacio.
            # En este layout simple lista vertical, multi_cell ya baja el cursor automáticamente.

        pdf.ln(3)
