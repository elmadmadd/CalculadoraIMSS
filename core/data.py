import json
import logging
import datetime
import uuid
import time
from pathlib import Path
from core.config import CONSTANTES

logger = logging.getLogger(__name__)

class GestorSesion:
    def __init__(self):
        self.archivo = CONSTANTES["RUTAS"]["SESION"]
        CONSTANTES["RUTAS"]["DOCS"].mkdir(parents=True, exist_ok=True)

    def guardar_datos_cliente(self, form_data):
        try:
            with open(self.archivo, 'w', encoding='utf-8') as f:
                json.dump(form_data, f, indent=4)
            logger.info(f"Datos de cliente guardados: {self.archivo}")
            return True
        except IOError as e:
            logger.error(f"Error de I/O guardando datos de cliente: {e}")
            return False
        except Exception as e:
            logger.error(f"Error inesperado guardando datos de cliente: {e}", exc_info=True)
            return False

    def cargar_datos_cliente(self):
        try:
            if not self.archivo.exists():
                return {}
            with open(self.archivo, 'r', encoding='utf-8') as f:
                return json.load(f)
        except json.JSONDecodeError as e:
            logger.error(f"Error parseando JSON de sesión: {e}")
            return {}
        except IOError as e:
            logger.error(f"Error de I/O cargando datos de cliente: {e}")
            return {}
        except Exception as e:
            logger.error(f"Error inesperado cargando datos de cliente: {e}", exc_info=True)
            return {}

    def limpiar_datos_cliente(self):
        try:
            if self.archivo.exists():
                self.archivo.unlink()
            logger.info("Datos de cliente limpiados")
            return True
        except IOError as e:
            logger.error(f"Error eliminando archivo de sesión: {e}")
            return False
        except Exception as e:
            logger.error(f"Error inesperado limpiando datos de cliente: {e}", exc_info=True)
            return False

class GestorPerfil:
    def __init__(self):
        self.archivo = CONSTANTES["RUTAS"]["PERFIL"]
        CONSTANTES["RUTAS"]["DOCS"].mkdir(parents=True, exist_ok=True)

    def guardar(self, d):
        try:
            with open(self.archivo, 'w') as f:
                json.dump(d, f, indent=4)
            logger.info(f"Perfil guardado: {self.archivo}")
            return True
        except IOError as e:
            logger.error(f"Error de I/O guardando perfil: {e}")
            return False
        except Exception as e:
            logger.error(f"Error inesperado guardando perfil: {e}", exc_info=True)
            return False

    def cargar(self):
        try:
            with open(self.archivo, "r") as f:
                return json.load(f)
        except FileNotFoundError:
            logger.warning(f"Archivo de perfil no encontrado: {self.archivo}")
            return {}
        except json.JSONDecodeError as e:
            logger.error(f"Error parseando JSON de perfil: {e}")
            return {}
        except Exception as e:
            logger.error(f"Error inesperado cargando perfil: {e}", exc_info=True)
            return {}

class GestorOperaciones:
    def __init__(self):
        self.archivo = CONSTANTES["RUTAS"]["DOCS"] / "historial_operaciones.json"
        CONSTANTES["RUTAS"]["REPORTES"].mkdir(parents=True, exist_ok=True)
    def guardar_operacion(self, data):
        ops = self.obtener()
        if "id" not in data: data["id"] = str(uuid.uuid4())
        if "fecha" not in data:
            now = datetime.datetime.now()
            data["fecha"] = now.strftime("%Y-%m-%d %H:%M")
            data["fecha_solo"] = now.strftime("%Y-%m-%d")
        if "estatus" not in data: data["estatus"] = "CONSULTA"
        ops.insert(0, data)
        self._save(ops)
        return data["id"]
    def actualizar_estatus(self, oid, ns):
        ops = self.obtener()
        for o in ops:
            if o.get("id") == oid:
                o["estatus"] = ns
                if ns in ["EN PROCESO", "FINALIZADO", "PENDIENTE"]:
                    o["fecha_proceso"] = datetime.datetime.now().strftime("%Y-%m-%d %H:%M")
                break
        self._save(ops)
    def actualizar_operacion(self, oid, new_data):
        """Actualizar una operación existente con nuevos datos"""
        ops = self.obtener()
        for o in ops:
            if o.get("id") == oid:
                o.update(new_data)
                break
        self._save(ops)
    def actualizar_o_crear_proceso(self, data_operacion):
        if "id" in data_operacion:
            self.actualizar_estatus(data_operacion["id"], "EN PROCESO")
            return data_operacion["id"]
        else:
            data_operacion["estatus"] = "EN PROCESO"
            return self.guardar_operacion(data_operacion)
    def eliminar_operacion(self, oid): self._save([o for o in self.obtener() if o.get("id") != oid])
    def obtener(self):
        try:
            with open(self.archivo, "r") as f: return json.load(f)
        except: return []
    def _save(self, ops):
        try:
            with open(self.archivo, "w") as f: json.dump(ops, f, indent=4)
        except: pass
    def filtrar_por_dia(self, fecha_str=None):
        if fecha_str is None: fecha_str = datetime.datetime.now().strftime("%Y-%m-%d")
        ops = self.obtener()
        ops_del_dia = []
        for op in ops:
            fecha_op = None
            if "fecha_solo" in op: fecha_op = op["fecha_solo"]
            elif "fecha" in op:
                try: fecha_op = op["fecha"].split(" ")[0]
                except: continue
            if fecha_op == fecha_str: ops_del_dia.append(op)
        return ops_del_dia
